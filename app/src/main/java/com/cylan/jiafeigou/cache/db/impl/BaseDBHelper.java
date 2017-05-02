package com.cylan.jiafeigou.cache.db.impl;

import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.cache.db.module.AccountDao;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.DPEntityDao;
import com.cylan.jiafeigou.cache.db.module.DaoMaster;
import com.cylan.jiafeigou.cache.db.module.DaoSession;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.module.DeviceDao;
import com.cylan.jiafeigou.cache.db.module.HistoryFile;
import com.cylan.jiafeigou.cache.db.module.HistoryFileDao;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;
import org.greenrobot.greendao.rx.RxQuery;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.schedulers.Schedulers;


/**
 * Created by yanzhendong on 2017/2/27.
 */

public class BaseDBHelper implements IDBHelper {
    private DPEntityDao mEntityDao;
    private AccountDao accountDao;
    private DeviceDao deviceDao;
    private HistoryFileDao historyFileDao;
    private IPropertyParser propertyParser;
    private Account dpAccount;
    private DaoSession daoSession;
    private JFGSourceManager sourceManager;

    public BaseDBHelper() {
//        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(ContextUtils.getContext(), "dp_cache.db");
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(new GreenDaoContext(), "dp_cache.db");
        DaoMaster master = new DaoMaster(helper.getWritableDb());
        daoSession = master.newSession();
        mEntityDao = daoSession.getDPEntityDao();
        accountDao = daoSession.getAccountDao();
        deviceDao = daoSession.getDeviceDao();
        historyFileDao = daoSession.getHistoryFileDao();
    }


    private DPEntity unique(QueryBuilder<DPEntity> builder) {
        DPEntity result = null;
        try {
            result = builder.unique();
        } catch (Exception e) {
            List<DPEntity> list = builder.list();
            for (DPEntity entity : list) {
                if (result == null) {
                    result = entity;
                    continue;
                }
                if (result.getVersion() < entity.getVersion()) {
                    result.delete();
                    result = entity;
                } else {
                    entity.delete();
                }
            }
            AppLogger.d(e.getMessage());
        }
        return result;
    }

    @Override
    public Observable<Iterable<DPEntity>> saveDPByteInTx(String uuid, Iterable<JFGDPMsg> msgs) {
        AppLogger.d("saveDPByteInTx:uuid:msgs");
        return getActiveAccount().map(account -> {
            Set<DPEntity> result = new HashSet<>();
            DPEntity dpEntity = null;
            Device device = sourceManager.getDevice(uuid);
            for (JFGDPMsg msg : msgs) {
                if (device != null && device.available()) {
                    dpEntity = device.getProperty((int) msg.id);
                }
                if (dpEntity == null) {
                    QueryBuilder<DPEntity> builder = buildDPMsgQueryBuilder(account.getAccount(), getServer(), uuid, msg.version, (int) msg.id, null, null, null);
                    dpEntity = unique(builder);
                }
                if (dpEntity != null && DBAction.DELETED.action().equals(dpEntity.getAction())) {
                    continue;
                }
                if (dpEntity == null) {
                    dpEntity = new DPEntity(null, account.getAccount(), getServer(), uuid, msg.version, (int) msg.id, msg.packValue, DBAction.SAVED.action(), DBState.SUCCESS.state(), null);
                }
                dpEntity.setAction(DBAction.SAVED);
                dpEntity.setState(DBState.SUCCESS);
                if (propertyParser.isProperty((int) msg.id)) {
                    DataPoint dataPoint = propertyParser.parser((int) msg.id, msg.packValue, msg.version);
                    dpEntity.setValue(dataPoint, msg.packValue, msg.version);
                    if (device != null && device.available()) {
                        device.updateProperty((int) msg.id, dpEntity);
                    }
                }
                result.add(dpEntity);
                dpEntity = null;
            }
            mEntityDao.insertOrReplaceInTx(result);
            return result;
        });
    }

    @Override
    public Observable<Iterable<DPEntity>> saveDPByteInTx(RobotoGetDataRsp dataRsp) {
        AppLogger.d("saveDPByteInTx:RobotoGetDataRsp");
        return getActiveAccount().map(account -> {
            if (dataRsp.map == null) return null;
            Set<DPEntity> result = new HashSet<>();
            DPEntity dpEntity = null;
            Device device = sourceManager.getDevice(dataRsp.identity);
            for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : dataRsp.map.entrySet()) {
                for (JFGDPMsg msg : entry.getValue()) {
                    if (device != null && device.available()) {
                        dpEntity = device.getProperty((int) msg.id);
                    }
                    if (dpEntity == null) {
                        QueryBuilder<DPEntity> builder = buildDPMsgQueryBuilder(account.getAccount(), getServer(), dataRsp.identity, msg.version, (int) msg.id, null, null, null);
                        dpEntity = unique(builder);
                    }
                    if (dpEntity != null && DBAction.DELETED.action().equals(dpEntity.getAction())) {
                        continue;
                    }
                    if (dpEntity == null) {
                        dpEntity = new DPEntity(null, account.getAccount(), getServer(), dataRsp.identity, msg.version, (int) msg.id, msg.packValue, DBAction.SAVED.action(), DBState.SUCCESS.state(), null);
                    }
                    dpEntity.setAction(DBAction.SAVED);
                    dpEntity.setState(DBState.SUCCESS);
                    if (propertyParser.isProperty((int) msg.id)) {
                        DataPoint dataPoint = propertyParser.parser((int) msg.id, msg.packValue, msg.version);
                        dpEntity.setValue(dataPoint, msg.packValue, msg.version);
                        if (device != null && device.available()) {
                            device.updateProperty((int) msg.id, dpEntity);
                        }
                    }
                    result.add(dpEntity);
                    dpEntity = null;
                }
            }
            mEntityDao.insertOrReplaceInTx(result);
            return result;
        });
    }

    @Override
    public Observable<List<DPEntity>> queryMultiDpMsg(String account, String server,
                                                      String uuid, Long version,
                                                      Long versionMax,
                                                      List<Integer> msgIdList,
                                                      Integer limit, DBAction action,
                                                      DBState state, DBOption option) {
        QueryBuilder<DPEntity> builder = buildDPMsgQueryBuilder(account, server, uuid, version, versionMax,
                msgIdList, action, state, option);
        if (limit != null) {
            builder.limit(limit);
        }
        return builder.orderDesc(DPEntityDao.Properties.Version).rx().list().map(dpEntities -> dpEntities);
    }

    @Override
    public Observable<DPEntity> queryDpMsg(QueryBuilder<DPEntity> builder) {
        return applyUnique(builder.rx());
    }

    private Observable<DPEntity> applyUnique(RxQuery<DPEntity> query) {
        return query.list().map(dpEntities -> {
            if (dpEntities == null) return null;
            DPEntity result = null;
            for (DPEntity entity : dpEntities) {
                if (result == null) {
                    result = entity;
                    continue;
                }
                if (result.getVersion() < entity.getVersion()) {
                    result.delete();
                    result = entity;
                } else {
                    entity.delete();
                }
            }
            return result;
        });
    }

    @Override
    public Observable<List<DPEntity>> queryMultiDpMsg(QueryBuilder<DPEntity> builder) {
        return builder.rx().list();
    }

    @Override
    public QueryBuilder<DPEntity> getDpEntityQueryBuilder() {
        return mEntityDao.queryBuilder();
    }

    @Override
    public DPEntity getProperty(String uuid, int msgId) {
        QueryBuilder<DPEntity> queryBuilder = buildDPMsgQueryBuilder(dpAccount.getAccount(), getServer(), uuid, null, msgId, null, null, null);
        DPEntity unique = unique(queryBuilder);
        return (unique != null && unique.action() != DBAction.DELETED) ? unique : null;
    }

    @Override
    public Device getJFGDevice(String uuid) {
        QueryBuilder<Device> queryBuilder = buildDPDeviceQueryBuilder(dpAccount.getAccount(), getServer(), uuid, DBAction.SAVED, DBState.SUCCESS, null);
        Device device = queryBuilder.unique();
        device.setPropertyParser(propertyParser);
        return device;
    }

    @Override
    public Observable<List<HistoryFile>> loadHistoryFile(String uuid, long timeStart, long timeEnd) {
        return historyFileDao.queryBuilder()
                .where(HistoryFileDao.Properties.Uuid.eq(uuid))
                .where(HistoryFileDao.Properties.Time.ge(timeStart))//>=
                .where(HistoryFileDao.Properties.Time.le(timeEnd))//<=
                .orderDesc(HistoryFileDao.Properties.Time)
                .rx()
                .list().subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<HistoryFile> saveHistoryFile(HistoryFile historyFile) {
        return historyFileDao.rx().insertOrReplace(historyFile);
    }

    @Override
    public Observable<Iterable<HistoryFile>> saveHistoryFile(Iterable<HistoryFile> historyFile) {
        return historyFileDao.rx().insertOrReplaceInTx(historyFile);
    }

    @Override
    public Observable<Boolean> deleteHistoryFile(String uuid, long timeStart, long timeEnd) {
        return loadHistoryFile(uuid, timeStart, timeEnd)
                .flatMap(historyFiles -> {
                    if (historyFiles != null) {
                        historyFileDao.deleteInTx(historyFiles);
                    }
                    return Observable.just(true);
                });
    }

    @Override
    public Observable<Void> deleteAllHistoryFile(String uuid) {
        return historyFileDao.rx().deleteAll();
    }

    @Override
    public void clearMsg(String uuid, Integer msgId) {
        if (dpAccount == null || dpAccount.getAccount() == null) return;
        List<DPEntity> list = buildDPMsgQueryBuilder(dpAccount.getAccount(), getServer(), uuid, null, msgId, null, null, null).list();
        mEntityDao.deleteInTx(list);
    }

    @Override
    public void clearDevice() {
        if (dpAccount == null || dpAccount.getAccount() == null) return;
        String execSQL = "DELETE FROM DEVICE WHERE ACCOUNT = ? AND SERVER = ? ";
        Database database = daoSession.getDatabase();
        database.beginTransaction();
        AppLogger.d("正在清除数据:" + execSQL);
        database.execSQL(execSQL, new Object[]{dpAccount.getAccount(), getServer()});
        database.endTransaction();
    }

    @Override
    public void setDataSourceManager(JFGSourceManager manager) {
        this.sourceManager = manager;
    }

    @Override
    public void setPropertyParser(IPropertyParser parser) {
        this.propertyParser = parser;
    }

    @Override
    public void deleteDpSync(String account, String uuid, int msdId) {
        List<DPEntity> list = mEntityDao.queryBuilder()
                .where(DPEntityDao.Properties.Account.eq(account))
                .where(DPEntityDao.Properties.Uuid.eq(uuid))
                .where(DPEntityDao.Properties.MsgId.eq(msdId))
                .list();
        if (list != null) {
            mEntityDao.deleteInTx(list);
        }
    }

    @Override
    public void deleteDpSync(String account, String uuid, long msgId, long versionMax, long versionMin) {
        List<DPEntity> list = mEntityDao.queryBuilder()
                .where(DPEntityDao.Properties.Account.eq(account))
                .where(DPEntityDao.Properties.Uuid.eq(uuid))
                .where(DPEntityDao.Properties.MsgId.eq(msgId))
                .where(DPEntityDao.Properties.Version.le(versionMax))
                .where(DPEntityDao.Properties.Version.ge(versionMin))
                .list();
        if (list != null) {
            mEntityDao.deleteInTx(list);
        }
    }

    @Override
    public List<DPEntity> queryDPMsg(String uuid, Integer msgId, Long version, Integer limit) {
        QueryBuilder<DPEntity> builder = buildDPMsgQueryBuilder(dpAccount.getAccount(), getServer(), uuid, null, msgId, null, null, null);
//        builder.orderDesc(DPEntityDao.Properties.Version);
        builder.limit(limit);
        List<DPEntity> list = builder.list();
        for (DPEntity entity : list) {
            entity.setValue(propertyParser.parser(entity.getMsgId(), entity.getBytes(), entity.getVersion()), entity.getBytes(), entity.getVersion());
        }
        return list;
    }

    @Override
    public Observable<DPEntity> deleteDPMsgNotConfirm(String uuid, Long version, Integer msgId, DBOption option) {
        AppLogger.d("正在将本地数据标记为未确认的删除状态,deleteDPMsgNotConfirm,uuid:" + uuid + ",version:" + version + ",msgId:" + msgId);
        return getActiveAccount().flatMap(account -> markDPMsg(account.getAccount(), getServer(), uuid, version, msgId, DBAction.DELETED, DBState.NOT_CONFIRM, option)
                .map(items -> items == null || items.size() == 0 ? null : items.get(0)));
    }

    @Override
    public Observable<DPEntity> deleteDPMsgWithConfirm(String uuid, Long version, Integer msgId, DBOption option) {
        AppLogger.d("正在将本地数据标记为已确认的删除状态,deleteDPMsgWithConfirm,uuid:" + uuid + ",version:" + version + ",msgId:" + msgId);
        return getActiveAccount().flatMap(account -> markDPMsg(account.getAccount(), getServer(), uuid, version, msgId, DBAction.DELETED, DBState.SUCCESS, option)
                .map(items -> items == null || items.size() == 0 ? null : items.get(0)));
    }

    @Override
    public Observable<List<DPEntity>> deleteDPMsgWithConfirm(String uuid, Integer msgId, DBOption option) {
        AppLogger.d("正在将本地数据标记为已确认的删除状态,deleteDPMsgWithConfirm,uuid:" + uuid + ",msgId:" + msgId);
        return getActiveAccount().flatMap(account -> markDPMsg(account.getAccount(), getServer(), uuid, null, msgId, DBAction.DELETED, DBState.SUCCESS, option));
    }

    @Override
    public Observable<List<DPEntity>> deleteDPMsgNotConfirm(String uuid, Integer msgId, DBOption option) {
        return getActiveAccount().flatMap(account -> markDPMsg(account.getAccount(), getServer(), uuid, null, msgId, DBAction.DELETED, DBState.NOT_CONFIRM, option));
    }

    @Override
    public Observable<List<DPEntity>> queryUnConfirmDpMsgWithTag(String uuid, Integer msgId, DBAction action) {
        AppLogger.d("正在根据 option 查询未经确认的 DP 消息,uuid:" + uuid + ",msgId:" + msgId + ",option :" + action);
        return getActiveAccount().flatMap(account -> queryDPMsg(account.getAccount(), getServer(), uuid, null, msgId, null, null, action, DBState.NOT_CONFIRM, null));
    }

    @Override
    public Observable<List<DPEntity>> queryUnConfirmDpMsg(String uuid, Integer msgId) {
        return getActiveAccount().flatMap(account -> queryDPMsg(account.getAccount(), getServer(), uuid, null, msgId, null, null, null, DBState.NOT_CONFIRM, null));
    }

    @Override
    public Observable<List<DPEntity>> markDPMsgWithConfirm(String uuid, Long version, Integer msgId, DBAction action, DBOption option) {
        return getActiveAccount().flatMap(account -> markDPMsg(account.getAccount(), getServer(), uuid, version, msgId, action, DBState.SUCCESS, option));
    }

    @Override
    public Observable<List<DPEntity>> markDPMsgNotConfirm(String uuid, Long version, Integer msgId, DBAction action, DBOption option) {
        return getActiveAccount().flatMap(account -> markDPMsg(account.getAccount(), getServer(), uuid, version, msgId, action, DBState.NOT_CONFIRM, option));
    }

    @Override
    public Observable<List<DPEntity>> queryDPMsg(String uuid, Long version, Integer msgId, Boolean asc, Integer limit) {
        return getActiveAccount().flatMap(account -> queryDPMsg(account.getAccount(), getServer(), uuid, version, msgId, asc, limit, DBAction.AVAILABLE, DBState.SUCCESS, null));
    }

    @Override
    public Observable<List<DPEntity>> queryDPMsgByUuid(String uuid) {
        return queryDPMsg(uuid, null, null, null, null);
    }

    @Override
    public Observable<DPEntity> saveOrUpdate(String account, String server, String uuid, Long version, Integer msgId, byte[] bytes, DBAction action, DBState state, DBOption option) {
        return applyUnique(buildDPMsgQueryBuilder(account, server, uuid, version, msgId, null, null, null)
                .rx())
                .map(item -> {
                    if (item == null) {
                        item = new DPEntity(null, account, server, uuid, version, msgId, bytes, action == null ? null : action.action(), state == null ? null : state.state(), option == null ? null : option.option());
                        mEntityDao.insertOrReplace(item);
                    } else {
                        item.setAccount(account);
                        item.setServer(server);
                        item.setUuid(uuid);
                        item.setVersion(version);
                        item.setMsgId(msgId);
                        item.setBytes(bytes);
                        item.setAction(action);
                        item.setState(state);
                        mEntityDao.update(item);
                    }
                    return item;
                });
    }

    @Override
    public Observable<DPEntity> saveOrUpdate(String uuid, Long version, Integer msgId, byte[] bytes, DBAction action, DBState state, DBOption option) {
        return getActiveAccount().flatMap(account -> saveOrUpdate(account.getAccount(), getServer(), uuid, version, msgId, bytes, action, state, option));
    }

    @Override
    public Observable<List<DPEntity>> queryDPMsg(String account, String server, String uuid, Long version, Integer msgId, Boolean asc, Integer limit, DBAction action, DBState state, DBOption option) {
        QueryBuilder<DPEntity> builder = buildDPMsgQueryBuilder(account, server, uuid, null, msgId, action, state, option);
        if (asc != null) {
            builder = asc ? builder.where(DPEntityDao.Properties.Version.ge(version)) : builder.where(DPEntityDao.Properties.Version.le(version));
        }
        if (limit != null) {
            builder.limit(limit);
        }
        return builder.orderDesc(DPEntityDao.Properties.Version).rx().list();
    }

    private Observable<List<DPEntity>> markDPMsg(String account, String server, String uuid, Long version, Integer msgId, DBAction action, DBState state, DBOption option) {
        AppLogger.d("正在标记本地数据, dpAccount:" + account + ",server:" + server + ",uuid:" + uuid + ",version:" + version + ",msgId:" + msgId + ",action:" + action + ",state:" + state + ",option:" + option);
        return buildDPMsgQueryBuilder(account, server, uuid, version, msgId, null, null, null)
                .rx().list()
                .map(items -> {
                    if (items == null || items.size() == 0) return items;
                    for (DPEntity item : items) {
                        item.setAction(action);
                        item.setState(state);
                        item.setOption(option);
                    }
                    mEntityDao.updateInTx(items);
                    return items;
                });
    }


    /**
     * 一般不推荐使用这个方法,但有些情境下无法获取正确的 version 所以必须把那条记录删除,否则就是脏数据了
     */
    @Override
    public Observable<DPEntity> deleteDPMsgForce(String account, String server, String uuid, Long version, Integer msgId) {
        return applyUnique(buildDPMsgQueryBuilder(account, server, uuid, version, msgId, null, null, null)
                .rx()).map(result -> {
            mEntityDao.delete(result);
            return result;
        });
    }

    @Override
    public Observable<Account> updateAccount(JFGAccount account) {
        return accountDao.queryBuilder().where(AccountDao.Properties.Server.eq(getServer()))
                .rx().list().map(accounts -> {
                    List<Account> changed = new ArrayList<>();
                    if (accounts != null && accounts.size() > 0) {
                        for (Account account1 : accounts) {
                            if (TextUtils.equals(account1.getAccount(), account.getAccount())) {
                                this.dpAccount = account1;
                                dpAccount.setAccount(account);
                                dpAccount.setState(DBState.ACTIVE);
                                changed.add(dpAccount);
                                continue;
                            }
                            if (account1.state() != DBState.SUCCESS) {
                                account1.setState(DBState.SUCCESS.state());
                                changed.add(account1);
                            }
                        }
                    }
                    if (dpAccount == null) {
                        dpAccount = new Account(account);
                        dpAccount.setServer(getServer());
                        changed.add(dpAccount);
                    }
                    accountDao.insertOrReplaceInTx(changed);
                    return dpAccount;
                });
    }


    @Override
    public Observable<Account> getActiveAccount() {
        return Observable.just(this.dpAccount).filter(item -> item != null)
                .mergeWith(RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class).map(item -> this.dpAccount = item.account))
                .mergeWith(accountDao.queryBuilder().where(AccountDao.Properties.Server.eq(getServer()), AccountDao.Properties.State.eq(DBState.ACTIVE.state())).rx().unique().filter(item -> item != null).map(item -> this.dpAccount = item))
                .first();
    }

    @Override
    public Observable<Iterable<Device>> updateDevice(JFGDevice[] device) {
        return getActiveAccount()
                .map(account -> {
                    AppLogger.d("正在写入 device");
                    List<Device> result = new ArrayList<>(device.length);
                    QueryBuilder<Device> queryBuilder = null;
                    Device dpDevice = null;
                    JFGDevice dev;
                    List<Device> remove = buildDPDeviceQueryBuilder(account.getAccount(), getServer(), null, null, null, null).list();
                    if (remove == null) remove = new ArrayList<>();
                    for (int i = 0; i < device.length; i++) {
                        dev = device[i];
                        clearMsg(dev.uuid, null);
                        queryBuilder = deviceDao.queryBuilder().where(DeviceDao.Properties.Server.eq(getServer()), DeviceDao.Properties.Uuid.eq(dev.uuid), DeviceDao.Properties.Account.eq(account.getAccount()));
                        dpDevice = queryBuilder.unique();
                        if (dpDevice == null) {
                            dpDevice = new Device();
                            dpDevice.setServer(getServer());
                            dpDevice.setAccount(account.getAccount());
                        }
                        dpDevice.setDevice(dev);
                        DBOption.DeviceOption option = dpDevice.option(DBOption.DeviceOption.class);
                        if (option == null) {
                            option = new DBOption.DeviceOption(i);
                            dpDevice.setOption(option);
                        }
                        if (dpDevice.action() == DBAction.UNBIND) {
                            option.lastLowBatteryTime = 0;
                        }
                        dpDevice.setPropertyParser(propertyParser);
                        result.add(dpDevice);
                    }
                    remove.removeAll(result);
                    deviceDao.deleteInTx(remove);
                    deviceDao.insertOrReplaceInTx(result);
                    AppLogger.d("当前设备数量:" + result.size());
                    return result;
                });
    }

    @Override
    public Observable<Device> updateDevice(Device device) {
        return deviceDao.rx().save(device).map(dev -> {
            dev.setPropertyParser(propertyParser);
            return dev;
        });
    }

    @Override
    public Observable<Device> unBindDeviceNotConfirm(String uuid) {
        return markDevice(getDpAccount(), getServer(), uuid, DBAction.UNBIND, DBState.NOT_CONFIRM, null).map(items -> {
            if (items == null || items.size() == 0) return null;
            Device device = items.get(0);
            device.setPropertyParser(propertyParser);
            return device;
        });
    }

    @Override
    public Observable<Device> unBindDeviceWithConfirm(String uuid) {
        return markDevice(getDpAccount(), getServer(), uuid, DBAction.UNBIND, DBState.SUCCESS, null).map(items -> {
            if (items == null || items.size() == 0) return null;
            clearMsg(uuid, null);
            Device device = items.get(0);
            device.setPropertyParser(propertyParser);
            deviceDao.delete(device);
            return device;
        });
    }

    @Override
    public Observable<Iterable<Device>> unBindDeviceWithConfirm(Iterable<String> uuids) {
        return getActiveAccount().map(account -> {
            QueryBuilder<Device> queryBuilder;
            Set<Device> result = new HashSet<>();
            for (String uuid : uuids) {
                queryBuilder = buildDPDeviceQueryBuilder(account.getAccount(), getServer(), uuid, null, null, null);
                for (Device device : queryBuilder.list()) {
                    device.setAction(DBAction.UNBIND);
                    device.setState(DBState.SUCCESS);
                    result.add(device);
                    device.setPropertyParser(propertyParser);
                    clearMsg(device.getUuid(), null);
                }
            }
            deviceDao.deleteInTx(result);
            return result;
        });
    }

    @Override
    public Observable<Device> getDevice(String uuid) {
        return null;
    }

    @Override
    public Observable<List<Device>> markDevice(String account, String server, String uuid, DBAction action, DBState state, DBOption option) {
        return buildDPDeviceQueryBuilder(account, server, uuid, null, null, null)
                .rx()
                .list()
                .map(items -> {
                    if (items != null) {
                        for (Device item : items) {
                            item.setAction(action);
                            item.setState(state);
                            item.setOption(option);
                            item.setPropertyParser(propertyParser);
                        }
                    }
                    deviceDao.updateInTx(items);
                    return items;
                });
    }

    @Override
    public Observable<List<Device>> getAccountDevice(String account) {
        return deviceDao.queryBuilder()
                .where(DeviceDao.Properties.Server.eq(getServer()), DeviceDao.Properties.Account.eq(account), DeviceDao.Properties.Action.notEq(DBAction.UNBIND.action()))
                .rx().list().map(devices -> {
                            if (devices == null) return null;
                            for (Device device : devices) {
                                device.setPropertyParser(propertyParser);
                            }
                            return devices;
                        }
                );
    }

    @Override
    public Observable<List<DPEntity>> getAllSavedDPMsgByAccount(String account) {
        return mEntityDao.queryBuilder().where(DPEntityDao.Properties.Account.eq(account),
                DPEntityDao.Properties.State.eq(DBState.SUCCESS.state()),
                DPEntityDao.Properties.Action.notEq(DBAction.DELETED.action()))
                .rx()
                .list()
                .observeOn(Schedulers.io());
    }

    @Override
    public Observable<List<DPEntity>> getActiveAccountSavedDPMsg() {
        return getAllSavedDPMsgByAccount(getDpAccount());
    }

    @Override
    public Observable<DPEntity> findDPMsg(String uuid, Long version, Integer msgId) {
        return getActiveAccount().flatMap(account -> applyUnique(buildDPMsgQueryBuilder(account.getAccount(), getServer(), uuid, version, msgId, null, null, null).rx()));
    }

    @Override
    public Observable<Account> logout() {
        return getActiveAccount().map(account -> {
            this.dpAccount = null;
            if (account != null) {
                account.setAction(DBAction.SAVED);
                account.setState(DBState.SUCCESS);
                accountDao.update(account);
            }
            return account;
        });
    }

    private QueryBuilder<Device> buildDPDeviceQueryBuilder(String account, String server, String uuid, DBAction action, DBState state, DBOption option) {
        QueryBuilder<Device> builder = deviceDao.queryBuilder();
        if (!TextUtils.isEmpty(account)) {
            builder.where(DeviceDao.Properties.Account.eq(account));//设置 dpAccount 约束
        }

        if (!TextUtils.isEmpty(server)) {
            builder.where(DeviceDao.Properties.Server.eq(server));//设置 server 约束
        }

        if (!TextUtils.isEmpty(uuid)) {
            builder.where(DeviceDao.Properties.Uuid.eq(uuid));//设置 UUID 约束
        }
        if (action != null) {
            if (action.op() == DBAction.OP.EQ) {
                builder.where(DeviceDao.Properties.Action.eq(action.action()));
            } else if (action.op() == DBAction.OP.NOT_EQ) {
                builder.where(DeviceDao.Properties.Action.notEq(action.action()));
            } else if (action.op() == DBAction.OP.NOT_EQS) {
                String[] actions = action.action().split(",");
                for (String act : actions) {
                    builder.where(DeviceDao.Properties.Action.notEq(act));
                }
            }
        }

        if (state != null) {
            builder.where(DeviceDao.Properties.State.eq(state.state()));
        }
        return builder;
    }

    private QueryBuilder<DPEntity> buildDPMsgQueryBuilder(String account, String server, String uuid, Long version, Integer msgId, DBAction action, DBState state, DBOption option) {
        QueryBuilder<DPEntity> builder = mEntityDao.queryBuilder();

        if (!TextUtils.isEmpty(account)) {
            builder.where(DPEntityDao.Properties.Account.eq(account));//设置 dpAccount 约束
        }

        if (!TextUtils.isEmpty(server)) {
            builder.where(DPEntityDao.Properties.Server.eq(server));//设置 server 约束
        }

        if (!TextUtils.isEmpty(uuid)) {
            builder.where(DPEntityDao.Properties.Uuid.eq(uuid));//设置 UUID 约束
        }

        if (version != null) {
            builder.where(DPEntityDao.Properties.Version.eq(version));
        }

        if (msgId != null) {
            builder.where(DPEntityDao.Properties.MsgId.eq(msgId));
        }

        if (action != null) {
            if (action.op() == DBAction.OP.EQ) {
                builder.where(DPEntityDao.Properties.Action.eq(action.action()));
            } else if (action.op() == DBAction.OP.NOT_EQ) {
                builder.where(DPEntityDao.Properties.Action.notEq(action.action()));
            } else if (action.op() == DBAction.OP.NOT_EQS) {
                String[] actions = action.action().split(",");
                for (String act : actions) {
                    builder.where(DPEntityDao.Properties.Action.notEq(act));
                }
            }
        }

        if (state != null) {
            builder.where(DPEntityDao.Properties.State.eq(state.state()));
        }

        return builder;
    }

    public QueryBuilder<DPEntity> buildDPMsgQueryBuilder(String account,
                                                         String server,
                                                         String uuid,
                                                         Long version,
                                                         Long versionMax,
                                                         List<Integer> msgIdList,
                                                         DBAction action,
                                                         DBState state,
                                                         DBOption option) {

        QueryBuilder<DPEntity> builder = mEntityDao.queryBuilder();

        if (!TextUtils.isEmpty(account)) {
            builder.where(DPEntityDao.Properties.Account.eq(account));//设置 dpAccount 约束
        }

        if (!TextUtils.isEmpty(server)) {
            builder.where(DPEntityDao.Properties.Server.eq(server));//设置 server 约束
        }

        if (!TextUtils.isEmpty(uuid)) {
            builder.where(DPEntityDao.Properties.Uuid.eq(uuid));//设置 UUID 约束
        }
        //取哪一个？
        if (version != null && versionMax != null) {
            builder.where(DPEntityDao.Properties.Version.ge(version));
            builder.where(DPEntityDao.Properties.Version.le(versionMax));
        }

        if (msgIdList != null && msgIdList.size() > 0) {
            if (msgIdList.size() == 1) {
                builder.where(DPEntityDao.Properties.MsgId.eq(msgIdList.remove(0)));
            } else {//size >=2
                WhereCondition condition1 = DPEntityDao.Properties.MsgId.eq(msgIdList.remove(0));
                WhereCondition condition2 = DPEntityDao.Properties.MsgId.eq(msgIdList.remove(0));
                if (msgIdList.size() > 0) {
                    WhereCondition[] whereConditions = new WhereCondition[msgIdList.size()];
                    for (int i = 0; i < msgIdList.size(); i++) {
                        whereConditions[i] = DPEntityDao.Properties.MsgId.eq(msgIdList.get(i));
                    }
                    builder.whereOr(condition1, condition2, whereConditions);
                } else {
                    builder.whereOr(condition1, condition2);
                }
            }
        }

        if (action != null) {
            if (action.op() == DBAction.OP.EQ) {
                builder.where(DPEntityDao.Properties.Action.eq(action.action()));
            } else if (action.op() == DBAction.OP.NOT_EQ) {
                builder.where(DPEntityDao.Properties.Action.notEq(action.action()));
            } else if (action.op() == DBAction.OP.NOT_EQS) {
                String[] actions = action.action().split(",");
                for (String act : actions) {
                    builder.where(DPEntityDao.Properties.Action.notEq(act));
                }
            }
        }

        if (state != null) {
            builder.where(DPEntityDao.Properties.State.eq(state.state()));
        }

        return builder;
    }

    private String getDpAccount() {
        Account account = accountDao.queryBuilder().where(AccountDao.Properties.State.eq(DBState.ACTIVE.state())).unique();
        if (account == null) {
            AppLogger.e("dpAccount is null");
            return null;
        }
        return account.getAccount();
    }

    private String getServer() {
        return OptionsImpl.getServer();
    }

    public class GreenDaoContext extends ContextWrapper {

        public GreenDaoContext() {
            super(ContextUtils.getContext());
        }

        /**
         * 获得数据库路径，如果不存在，则创建对象
         *
         * @param dbName
         */
        @Override
        public File getDatabasePath(String dbName) {
            File baseFile = new File(JConstant.ROOT_DIR + File.separator + "db", dbName);
            File parentFile = baseFile.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            return baseFile;
        }

        /**
         * 重载这个方法，是用来打开SD卡上的数据库的，android 2.3及以下会调用这个方法。
         *
         * @param name
         * @param mode
         * @param factory
         */
        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                                                   SQLiteDatabase.CursorFactory factory) {
            return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
        }

        /**
         * Android 4.0会调用此方法获取数据库。
         *
         * @param name
         * @param mode
         * @param factory
         * @param errorHandler
         * @see ContextWrapper#openOrCreateDatabase(String, int,
         * SQLiteDatabase.CursorFactory,
         * DatabaseErrorHandler)
         */
        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory,
                                                   DatabaseErrorHandler errorHandler) {
            return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
        }
    }
}