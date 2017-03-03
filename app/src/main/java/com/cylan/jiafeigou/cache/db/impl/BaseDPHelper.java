package com.cylan.jiafeigou.cache.db.impl;

import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.DPEntityDao;
import com.cylan.jiafeigou.cache.db.module.DaoMaster;
import com.cylan.jiafeigou.cache.db.module.DaoSession;
import com.cylan.jiafeigou.cache.db.view.IDPAction;
import com.cylan.jiafeigou.cache.db.view.IDPHelper;
import com.cylan.jiafeigou.cache.db.view.IDPState;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.List;

import rx.Observable;


/**
 * Created by yanzhendong on 2017/2/27.
 */

public class BaseDPHelper implements IDPHelper {
    private DPEntityDao mEntityDao;
    private static BaseDPHelper instance;

    public static BaseDPHelper getInstance() {
        if (instance == null) {
            synchronized (BaseDPHelper.class) {
                if (instance == null) {
                    instance = new BaseDPHelper();
                }
            }
        }
        return instance;
    }

    private BaseDPHelper() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(new GreenDaoContext(), "dp_cache.db");
        DaoMaster master = new DaoMaster(helper.getWritableDb());
        DaoSession daoSession = master.newSession();
        mEntityDao = daoSession.getDPEntityDao();
    }

    @Override
    public Observable saveDPByte(String uuid, Long version, Integer msgId, byte[] bytes) {
        return saveDpMsg(getAccount(), getServer(), uuid, version, msgId, bytes, IDPAction.SAVED.action(), IDPState.SUCCESS.state());
    }

    @Override
    public Observable<DPEntity> deleteDPMsgNotConfirm(String uuid, Long version, Integer msgId) {
        AppLogger.d("正在将本地数据标记为未确认的删除状态,deleteDPMsgNotConfirm,uuid:" + uuid + ",version:" + version + ",msgId:" + msgId);
        return markDPMsg(getAccount(), getServer(), uuid, version, msgId, IDPAction.DELETED, IDPState.NOT_CONFIRM).filter(items -> items != null && items.size() == 1).map(items -> items.get(0));
    }

    @Override
    public Observable<DPEntity> deleteDPMsgWithConfirm(String uuid, Long version, Integer msgId) {
        AppLogger.d("正在将本地数据标记为已确认的删除状态,deleteDPMsgWithConfirm,uuid:" + uuid + ",version:" + version + ",msgId:" + msgId);
        return markDPMsg(getAccount(), getServer(), uuid, version, msgId, IDPAction.DELETED, IDPState.SUCCESS).filter(items -> items != null && items.size() == 1).map(items -> items.get(0));
    }

    @Override
    public Observable<Boolean> deleteDPMsgWithConfirm(String uuid, Integer msgId) {
        AppLogger.d("正在将本地数据标记为已确认的删除状态,deleteDPMsgWithConfirm,uuid:" + uuid + ",msgId:" + msgId);
        return markDPMsg(getAccount(), getServer(), uuid, null, msgId, IDPAction.DELETED, IDPState.SUCCESS).map(item -> true);
    }

    @Override
    public Observable<List<DPEntity>> queryUnConfirmDpMsgWithTag(String uuid, Integer msgId, IDPAction action) {
        AppLogger.d("正在根据 action 查询未经确认的 DP 消息,uuid:" + uuid + ",msgId:" + msgId + ",action :" + action);
        return queryDPMsg(getAccount(), getServer(), uuid, null, msgId, null, null, action, IDPState.NOT_CONFIRM);
    }

    @Override
    public Observable<List<DPEntity>> queryUnConfirmDpMsg(String uuid, Integer msgId) {
        return queryDPMsg(getAccount(), getServer(), uuid, null, msgId, null, null, null, IDPState.NOT_CONFIRM);
    }

    @Override
    public Observable<List<DPEntity>> markDPMsgWithConfirm(String uuid, Long version, Integer msgId, IDPAction action) {
        return markDPMsg(getAccount(), getServer(), uuid, version, msgId, action, IDPState.SUCCESS);
    }

    @Override
    public Observable<List<DPEntity>> markDPMsgNotConfirm(String uuid, Long version, Integer msgId, IDPAction action) {
        return markDPMsg(getAccount(), getServer(), uuid, version, msgId, action, IDPState.NOT_CONFIRM);
    }

    @Override
    public Observable<List<DPEntity>> queryDPMsg(String uuid, Long version, Integer msgId, Boolean asc, Integer limit) {
        return queryDPMsg(getAccount(), getServer(), uuid, version, msgId, asc, limit, IDPAction.SAVED, IDPState.SUCCESS);
    }

    @Override
    public Observable<DPEntity> saveDpMsg(String account, String server, String uuid, Long version, Integer msgId, byte[] bytes, String action, String state) {
        return buildQueryBuilder(account, server, uuid, version, msgId, null, null)
                .rx().unique().filter(item -> item == null)
                .map(item -> {
                    AppLogger.e("正在创建条目");
                    item = new DPEntity(null, account, server, uuid, version, msgId, bytes, action, state);
                    mEntityDao.save(item);
                    return item;
                });
    }

    @Override
    public Observable<DPEntity> saveOrUpdate(String account, String server, String uuid, Long version, Integer msgId, byte[] bytes, String action, String state) {
        return buildQueryBuilder(account, server, uuid, version, msgId, null, null)
                .rx().unique()
                .map(item -> {
                    if (item == null) {
                        item = new DPEntity(null, account, server, uuid, version, msgId, bytes, action, state);
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
    public Observable<List<DPEntity>> queryDPMsg(String account, String server, String uuid, Long version, Integer msgId, Boolean asc, Integer limit, IDPAction action, IDPState state) {
        QueryBuilder<DPEntity> builder = buildQueryBuilder(account, server, uuid, null, msgId, action, state);
        if (asc != null) {
            builder = asc ? builder.where(DPEntityDao.Properties.Version.ge(version)) : builder.where(DPEntityDao.Properties.Version.le(version));
        }
        if (limit != null) {
            builder.limit(limit);
        }
        return builder.orderDesc(DPEntityDao.Properties.Version).rx().list();
    }

    @Override
    public Observable<List<DPEntity>> markDPMsg(String account, String server, String uuid, Long version, Integer msgId, IDPAction action, IDPState state) {
        AppLogger.d("正在标记本地数据, account:" + account + ",server:" + server + ",uuid:" + uuid + ",version:" + version + ",msgId:" + msgId + ",action:" + action + ",state:" + state);
        return buildQueryBuilder(account, server, uuid, version, msgId, null, null)
                .rx().list()
                .map(items -> {
                    if (items == null || items.size() == 0) return items;
                    for (DPEntity item : items) {
                        item.setAction(action == null ? null : action.action());
                        item.setState(action == null ? null : state.state());
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
        AppLogger.e("收藏成功,正在删除本地数据");
        return buildQueryBuilder(account, server, uuid, version, msgId, null, null)
                .rx().unique().map(result -> {
                    result.delete();
                    return result;
                });
    }

    private QueryBuilder<DPEntity> buildQueryBuilder(String account, String server, String uuid, Long version, Integer msgId, IDPAction action, IDPState state) {
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
            builder.where(DPEntityDao.Properties.Action.like(action.action()));
        }

        if (state != null) {
            builder.where(DPEntityDao.Properties.State.eq(state.state()));
        }

        return builder;
    }

    private String getAccount() {
        return null;
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
