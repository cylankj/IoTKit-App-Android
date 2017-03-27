package com.cylan.jiafeigou.cache.db.impl;

import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.base.module.BasePropertyParser;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.cache.db.module.AccountDao;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.DPEntityDao;
import com.cylan.jiafeigou.cache.db.module.DaoMaster;
import com.cylan.jiafeigou.cache.db.module.DaoSession;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.module.DeviceDao;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

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
    private static BaseDBHelper instance;
    private IPropertyParser propertyParser;
    private Account dpAccount;

    public static BaseDBHelper getInstance() {
        if (instance == null) {
            synchronized (BaseDBHelper.class) {
                if (instance == null) {
                    instance = new BaseDBHelper();
                }
            }
        }
        return instance;
    }

    private BaseDBHelper() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(new GreenDaoContext(), "dp_cache.db");
        DaoMaster master = new DaoMaster(helper.getWritableDb());
        DaoSession daoSession = master.newSession();
        mEntityDao = daoSession.getDPEntityDao();
        accountDao = daoSession.getAccountDao();
        deviceDao = daoSession.getDeviceDao();
        propertyParser = BasePropertyParser.getInstance();
    }

    @Override
    @Deprecated
    public Observable<DPEntity> saveDPByte(String uuid, Long version, Integer msgId, byte[] bytes) {
        return getActiveAccount().flatMap(account -> saveDpMsg(account.getAccount(), getServer(), uuid, version, msgId, bytes, DBAction.SAVED, DBState.SUCCESS, null));
    }

    @Override
    public Observable<Iterable<DPEntity>> saveDPByteInTx(String uuid, Iterable<JFGDPMsg> msgs) {
        return getActiveAccount().map(account -> {
            Set<DPEntity> result = new HashSet<>();
            QueryBuilder<DPEntity> queryBuilder;
            DPEntity dpEntity;
            for (JFGDPMsg msg : msgs) {
                if (propertyParser.isProperty((int) msg.id)) {
                    dpEntity = getProperty(uuid, (int) msg.id);
                } else {
                    queryBuilder = buildDPMsgQueryBuilder(account.getAccount(), getServer(), uuid, msg.version, (int) msg.id, null, null, null);
                    dpEntity = queryBuilder.unique();
                }
                if (dpEntity != null && DBAction.DELETED.action().equals(dpEntity.getAction())) {
                    continue;
                }
                if (dpEntity != null && dpEntity.getVersion() == msg.version) {
                    continue;
                }
                if (dpEntity == null) {
                    dpEntity = new DPEntity(null, account.getAccount(), getServer(), uuid, msg.version, (int) msg.id, msg.packValue, DBAction.SAVED.action(), DBState.SUCCESS.state(), null);
                } else {
                    dpEntity.setValue(msg.packValue, msg.version);
                }
                result.add(dpEntity);
            }
            return result;
        })
                .flatMap(dpEntities -> mEntityDao.rx().saveInTx(dpEntities));
    }

    @Override
    public Observable<Iterable<DPEntity>> saveDPByteInTx(RobotoGetDataRsp dataRsp) {
        return getActiveAccount().map(account -> {
            if (dataRsp.map == null) return null;
            Set<DPEntity> result = new HashSet<>();
            QueryBuilder<DPEntity> queryBuilder;
            DPEntity dpEntity;
            for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : dataRsp.map.entrySet()) {
                for (JFGDPMsg msg : entry.getValue()) {
                    if (propertyParser.isProperty((int) msg.id)) {
                        dpEntity = getProperty(dataRsp.identity, (int) msg.id);
                    } else {
                        queryBuilder = buildDPMsgQueryBuilder(account.getAccount(), getServer(), dataRsp.identity, msg.version, (int) msg.id, null, null, null);
                        dpEntity = queryBuilder.unique();
                    }
                    if (dpEntity != null && DBAction.DELETED.action().equals(dpEntity.getAction())) {
                        continue;
                    }
                    if (dpEntity != null && dpEntity.getVersion() == msg.version) {
                        continue;
                    }
                    if (dpEntity == null) {
                        dpEntity = new DPEntity(null, account.getAccount(), getServer(), dataRsp.identity, msg.version, (int) msg.id, msg.packValue, DBAction.SAVED.action(), DBState.SUCCESS.state(), null);
                    } else {
                        dpEntity.setValue(msg.packValue, msg.version);
                    }
                    result.add(dpEntity);
                }
            }
            return result;
        })
                .flatMap(dpEntities -> mEntityDao.rx().saveInTx(dpEntities));
    }

    @Override
    public Observable<List<DPEntity>> queryMultiDpMsg(String account, String server,
                                                      String uuid, Long version,
                                                      Long versionMax,
                                                      List<Integer> msgIdList, Boolean asc,
                                                      Integer limit, DBAction action,
                                                      DBState state, DBOption option) {
        QueryBuilder<DPEntity> builder = buildDPMsgQueryBuilder(account, server, uuid, version, versionMax,
                msgIdList, asc, action, state, option);
        if (limit != null) {
            builder.limit(limit);
        }
        return builder.orderDesc(DPEntityDao.Properties.Version).rx().list().map(dpEntities -> {
            if (dpEntities != null) {
            }
            return dpEntities;
        });
    }

    @Override
    public Observable<DPEntity> queryDpMsg(QueryBuilder<DPEntity> builder) {
        return builder.rx().unique();
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
        queryBuilder.where(DPEntityDao.Properties.Version.le(Long.MAX_VALUE));
        queryBuilder.limit(1);
        DPEntity unique = queryBuilder.unique();
        return (unique != null && unique.action() != DBAction.DELETED) ? unique : null;
    }

    @Override
    public Device getJFGDevice(String uuid) {
        QueryBuilder<Device> queryBuilder = buildDPDeviceQueryBuilder(dpAccount.getAccount(), getServer(), uuid, DBAction.SAVED, DBState.SUCCESS, null);
        queryBuilder.limit(1);
        return queryBuilder.unique();
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
    public Observable<DPEntity> saveDpMsg(String account, String server, String uuid, Long version, Integer msgId, byte[] bytes, DBAction action, DBState state, DBOption option) {
        return buildDPMsgQueryBuilder(account, server, uuid, version, msgId, null, null, null)
                .rx()
                .unique()
                .doOnError(throwable -> Log.e("throwable: ", "throwable:" + throwable.getLocalizedMessage()))
                .filter(item -> {
                    if (item != null && DBAction.DELETED.action().equals(item.getAction())) {
                        return false;
                    }
                    return true;
                })
                .map(item -> {
                    if (item == null) {
                        item = new DPEntity(null, account, server, uuid, version, msgId, bytes, action == null ? null : action.action(), state == null ? null : state.state(), option == null ? null : option.option());
                        Log.d("throwable", "throwable: " + item);
                        Log.d("throwable", "throwable: " + Thread.currentThread());
                        mEntityDao.insertOrReplace(item);
                    }
                    return item;
                });
    }

    @Override
    public Observable<DPEntity> saveOrUpdate(String account, String server, String uuid, Long version, Integer msgId, byte[] bytes, DBAction action, DBState state, DBOption option) {
        return buildDPMsgQueryBuilder(account, server, uuid, version, msgId, null, null, null)
                .rx().unique()
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

    @Override
    public Observable<List<DPEntity>> markDPMsg(String account, String server, String uuid, Long version, Integer msgId, DBAction action, DBState state, DBOption option) {
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
        return buildDPMsgQueryBuilder(account, server, uuid, version, msgId, null, null, null)
                .rx().unique().map(result -> {
                    mEntityDao.delete(result);
                    return result;
                });
    }

    @Override
    public Observable<Account> updateAccount(JFGAccount account) {
        return accountDao.queryBuilder().where(AccountDao.Properties.Account.notEq(account.getAccount()))
                .rx().list().flatMap(accounts -> {
                    if (accounts != null) {
                        for (Account account1 : accounts) {
                            account1.setState(DBState.SUCCESS.state());
                            accountDao.update(account1);
                        }
                    }
                    return accountDao.queryBuilder().where(AccountDao.Properties.Account.eq(account.getAccount()))
                            .rx().unique().map(account1 -> {
                                if (account1 == null) {
                                    account1 = new Account(account);
                                }
                                account1.setState(DBState.ACTIVE.state());
                                accountDao.save(account1);
                                return this.dpAccount = account1;
                            });
                });
    }


    @Override
    public Observable<Account> getActiveAccount() {
        return Observable.just(this.dpAccount).filter(item -> item != null)
                .mergeWith(RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class).map(item -> this.dpAccount = item.account))
                .mergeWith(accountDao.queryBuilder().where(AccountDao.Properties.State.eq(DBState.ACTIVE.state())).rx().unique().filter(item -> item != null).map(item -> this.dpAccount = item))
                .first();
    }

    @Override
    public Observable<Iterable<Device>> updateDevice(JFGDevice[] device) {
        return getActiveAccount().map(account -> {
            List<Device> result = new ArrayList<>(device.length);
            QueryBuilder<Device> queryBuilder = null;
            Device dpDevice = null;
            JFGDevice dev;
            for (int i = 0; i < device.length; i++) {
                dev = device[i];
                queryBuilder = deviceDao.queryBuilder().where(DeviceDao.Properties.Uuid.eq(dev.uuid), DeviceDao.Properties.Account.eq(account.getAccount()));
                dpDevice = queryBuilder.unique();
                if (dpDevice == null) {
                    dpDevice = new Device();
                    dpDevice.setDevice(dev);
                }
                dpDevice.setAccount(account.getAccount());
                dpDevice.setOption(new DBOption.RawDeviceOrderOption(i));
                result.add(dpDevice);
            }
            return result;
        }).flatMap(devices -> deviceDao.rx().saveInTx(devices));
    }

    @Override
    public Observable<Device> updateDevice(Device device) {
        return deviceDao.rx().save(device);
    }

    @Override
    public Observable<Device> unBindDeviceNotConfirm(String uuid) {
        return markDevice(getDpAccount(), getServer(), uuid, DBAction.UNBIND, DBState.NOT_CONFIRM, null).map(items -> {
            if (items == null || items.size() == 0) return null;
            return items.get(0);
        });
    }

    @Override
    public Observable<Device> unBindDeviceWithConfirm(String uuid) {
        return markDevice(getDpAccount(), getServer(), uuid, DBAction.UNBIND, DBState.SUCCESS, null).map(items -> {
            if (items == null || items.size() == 0) return null;
            return items.get(0);
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
                }
            }
            return result;
        }).flatMap(devices -> deviceDao.rx().updateInTx(devices));
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
                        }
                    }
                    deviceDao.updateInTx(items);
                    return items;
                });
    }

    @Override
    public Observable<List<Device>> getAccountDevice(String account) {
        return deviceDao.queryBuilder().where(DeviceDao.Properties.Account.eq(account), DeviceDao.Properties.Action.notEq(DBAction.UNBIND.action())).rx().list();
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
        return getActiveAccount().flatMap(account -> buildDPMsgQueryBuilder(account.getAccount(), getServer(), uuid, version, msgId, null, null, null).rx().unique());
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

    @Override
    public Observable<DPEntity> update(DPEntity entity) {
        return mEntityDao.rx().update(entity);
    }

    @Override
    public Observable<Void> delete(DPEntity entity) {
        return mEntityDao.rx().delete(entity);
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

    private QueryBuilder<DPEntity> buildDPMsgQueryBuilder(String account,
                                                          String server,
                                                          String uuid,
                                                          Long version,
                                                          Long versionMax,
                                                          List<Integer> msgIdList,
                                                          Boolean asc,
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

        if (version != null && asc != null) {
            if (asc) {
                builder.where(DPEntityDao.Properties.Version.ge(version));
                builder.where(DPEntityDao.Properties.Version.lt(versionMax));
            } else builder.where(DPEntityDao.Properties.Version.lt(version));
        }

        if (msgIdList != null && msgIdList.size() > 0) {
            if (msgIdList.size() == 1) {
                builder.where(DPEntityDao.Properties.MsgId.eq(0));
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
        AppLogger.e("需要填server");
        return null;
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
            SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
            return result;
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
            SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
            return result;
        }
    }
}