package com.cylan.jiafeigou.cache.db.impl;

import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDevice;
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
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.List;

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


    }

    @Override
    public Observable<DPEntity> saveDPByte(String uuid, Long version, Integer msgId, byte[] bytes) {
        return getActiveAccount().flatMap(account -> saveDpMsg(account.getAccount(), getServer(), uuid, version, msgId, bytes, DBAction.SAVED, DBState.SUCCESS, null));
    }

    @Override
    public Observable<DPEntity> deleteDPMsgNotConfirm(String uuid, Long version, Integer msgId, DBOption option) {
        AppLogger.d("正在将本地数据标记为未确认的删除状态,deleteDPMsgNotConfirm,uuid:" + uuid + ",version:" + version + ",msgId:" + msgId);
        return getActiveAccount().flatMap(account -> markDPMsg(account.getAccount(), getServer(), uuid, version, msgId, DBAction.DELETED, DBState.NOT_CONFIRM, option)
                .map(items -> items == null ? null : items.get(0)));
    }

    @Override
    public Observable<DPEntity> deleteDPMsgWithConfirm(String uuid, Long version, Integer msgId, DBOption option) {
        AppLogger.d("正在将本地数据标记为已确认的删除状态,deleteDPMsgWithConfirm,uuid:" + uuid + ",version:" + version + ",msgId:" + msgId);
        return getActiveAccount().flatMap(account -> markDPMsg(account.getAccount(), getServer(), uuid, version, msgId, DBAction.DELETED, DBState.SUCCESS, option)
                .map(items -> items == null ? null : items.get(0)));
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
                .rx().unique().filter(item -> {
                    if (item != null && DBAction.DELETED.action().equals(item.getAction())) {
                        return false;
                    }
                    return true;
                })
                .map(item -> {
                    if (item == null) {
                        item = new DPEntity(null, account, server, uuid, version, msgId, bytes, action == null ? null : action.action(), state == null ? null : state.state(), option == null ? null : option.option());
                        mEntityDao.save(item);
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
                        mEntityDao.save(item);
                    } else {
                        item.setAccount(account);
                        item.setServer(server);
                        item.setUuid(uuid);
                        item.setVersion(version);
                        item.setMsgId(msgId);
                        item.setBytes(bytes);
                        item.setAction(action);
                        item.setState(state);
                        item.update();
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
        AppLogger.d("正在标记本地数据, account:" + account + ",server:" + server + ",uuid:" + uuid + ",version:" + version + ",msgId:" + msgId + ",action:" + action + ",state:" + state + ",option:" + option);
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
                    result.delete();
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
                        }
                    }
                    return accountDao.rx().updateInTx(accounts).flatMap(ret ->
                            accountDao.queryBuilder().where(AccountDao.Properties.Account.eq(account.getAccount()))
                                    .rx().unique().map(account1 -> {
                                if (account1 == null) {
                                    account1 = new Account(account);
                                }
                                account1.setState(DBState.ACTIVE.state());
                                accountDao.save(account1);
                                return account1;
                            }));
                });
    }


    @Override
    public Observable<Account> getActiveAccount() {
        return Observable.just(accountDao.queryBuilder().where(AccountDao.Properties.State.eq(DBState.ACTIVE.state())))
                .observeOn(Schedulers.io())
                .flatMap(build -> build.rx().unique().filter(account -> account != null)
                        .mergeWith(RxBus.getCacheInstance().toObservable(JFGAccount.class)
                                .flatMap(s -> build.rx().unique().filter(account -> account != null))))
                .first();
    }

    @Override
    public Observable<Device> updateDevice(JFGDevice[] device) {
        return Observable.from(device)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(dev -> getActiveAccount().map(account -> dev))//延迟写入,等到有账号了才写入数据库
                .map(Device::new)
                .flatMap(device1 -> deviceDao.queryBuilder().where(DeviceDao.Properties.Uuid.eq(device1.getUuid()), DeviceDao.Properties.Account.eq(getAccount())).rx().unique()
                        .flatMap(device2 -> {
                            AppLogger.d("正在更新 Device 条目");
                            if (device2 != null) {
                                deviceDao.delete(device2);
                            }
                            device1.setAccount(getAccount());
                            return deviceDao.rx().save(device1);
                        }))
                .doOnError(throwable -> {
                    AppLogger.e("err: " + throwable.getLocalizedMessage());
                    throwable.printStackTrace();
                });
    }

    @Override
    public Observable<Device> unBindDeviceNotConfirm(String uuid) {
        return markDevice(getAccount(), getServer(), uuid, DBAction.UNBIND, DBState.NOT_CONFIRM, null).map(items -> {
            if (items == null || items.size() == 0) return null;
            return items.get(0);
        });
    }

    @Override
    public Observable<Device> unBindDeviceWithConfirm(String uuid) {
        return markDevice(getAccount(), getServer(), uuid, DBAction.UNBIND, DBState.SUCCESS, null).map(items -> {
            if (items == null || items.size() == 0) return null;
            return items.get(0);
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
        return getAllSavedDPMsgByAccount(getAccount());
    }

    @Override
    public Observable<DPEntity> findDPMsg(String uuid, Long version, Integer msgId) {
        return getActiveAccount().flatMap(account -> buildDPMsgQueryBuilder(account.getAccount(), getServer(), uuid, version, msgId, null, null, null).rx().unique());
    }

    private QueryBuilder<Device> buildDPDeviceQueryBuilder(String account, String server, String uuid, DBAction action, DBState state, DBOption option) {
        QueryBuilder<Device> builder = deviceDao.queryBuilder();
        if (!TextUtils.isEmpty(account)) {
            builder.where(DeviceDao.Properties.Account.eq(account));//设置 account 约束
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
            builder.where(DPEntityDao.Properties.Account.eq(account));//设置 account 约束
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

    private String getAccount() {
        Account account = accountDao.queryBuilder().where(AccountDao.Properties.State.eq(DBState.ACTIVE.state())).unique();
        if (account == null) return null;
        return account.getAccount();
    }

    private String getServer() {
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
