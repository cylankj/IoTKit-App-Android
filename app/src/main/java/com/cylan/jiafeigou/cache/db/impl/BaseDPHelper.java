package com.cylan.jiafeigou.cache.db.impl;

import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.module.DBAction;
import com.cylan.jiafeigou.cache.db.module.DPCache;
import com.cylan.jiafeigou.cache.db.module.DPCacheDao;
import com.cylan.jiafeigou.cache.db.module.DaoMaster;
import com.cylan.jiafeigou.cache.db.module.DaoSession;
import com.cylan.jiafeigou.cache.db.view.IDPHelper;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;


/**
 * Created by yanzhendong on 2017/2/27.
 */

public class BaseDPHelper implements IDPHelper {
    private DPCacheDao cacheDao;
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
        cacheDao = daoSession.getDPCacheDao();
    }

    @Override
    public Observable saveDPByte(String uuid, Long version, Integer msgId, byte[] bytes) {
        AppLogger.d("正在存数据,uuid:" + uuid + ",version:" + version + "msgId:" + msgId);
        return saveDpMsg(getAccount(), getServer(), uuid, version, msgId, bytes, DBAction.SAVED, DBAction.SUCCESS);
    }

    @Override
    public Observable<DPCache> deleteDPMsgNotConfirm(String uuid, Long version, Integer msgId) {
        AppLogger.d("正在删除本地数据,deleteDPMsgNotConfirm,uuid:" + uuid + ",version:" + version + ",msgId:" + msgId);
        return markDPMsg(getAccount(), getServer(), uuid, version, msgId, DBAction.DELETED, DBAction.NOT_CONFIRM).filter(items -> items != null && items.size() == 1).map(items -> items.get(0));
    }

    @Override
    public Observable<DPCache> deleteDPMsgWithConfirm(String uuid, Long version, Integer msgId) {
        AppLogger.d("正在删除本地数据,deleteDPMsgWithConfirm,uuid:" + uuid + ",version:" + version + ",msgId:" + msgId);
        return markDPMsg(getAccount(), getServer(), uuid, version, msgId, DBAction.DELETED, DBAction.SUCCESS).filter(items -> items != null && items.size() == 1).map(items -> items.get(0));
    }

    @Override
    public Observable<Boolean> deleteDPMsgWithConfirm(String uuid, Integer msgId) {
        AppLogger.d("正在删除本地数据,deleteDPMsgWithConfirm,uuid:" + uuid + ",msgId:" + msgId);
        return markDPMsg(getAccount(), getServer(), uuid, null, msgId, DBAction.DELETED, DBAction.SUCCESS).map(item -> true);
    }

    @Override
    public Observable<List<DPCache>> queryUnConfirmDpMsgWithTag(String uuid, Integer msgId, DBAction tag) {
        AppLogger.d("正在根据 tag查询未经确认的 DP 消息");
        return queryDPMsg(getAccount(), getServer(), uuid, null, msgId, null, null, tag, DBAction.NOT_CONFIRM);
    }

    @Override
    public Observable<List<DPCache>> queryUnConfirmDpMsg(String uuid, Integer msgId) {
        return queryDPMsg(getAccount(), getServer(), uuid, null, msgId, null, null, DBAction.NOT_CONFIRM);
    }

    @Override
    public Observable<List<DPCache>> markDPMsgWithConfirm(String uuid, Long version, Integer msgId, DBAction tag) {
        return markDPMsg(getAccount(), getServer(), uuid, version, msgId, tag, DBAction.SUCCESS);
    }

    @Override
    public Observable<List<DPCache>> markDPMsgNotConfirm(String uuid, Long version, Integer msgId, DBAction tag) {
        return markDPMsg(getAccount(), getServer(), uuid, version, msgId, tag, DBAction.NOT_CONFIRM);
    }

    @Override
    public Observable<List<DPCache>> queryDPMsg(String uuid, Long version, Integer msgId, Boolean asc, Integer limit) {
        return queryDPMsg(getAccount(), getServer(), uuid, version, msgId, asc, limit, DBAction.SAVED, DBAction.SUCCESS);
    }

    @Override
    public Observable<DPCache> saveDpMsg(String account, String server, String uuid, Long version, Integer msgId, byte[] bytes, DBAction... dbActions) {
        return buildQueryBuilder(account, server, uuid, version, msgId)
                .rx().unique().filter(item -> item == null)
                .map(item -> {
                    item = new DPCache(null, account, server, uuid, version, msgId, bytes, null, null);
                    if (dbActions != null) {
                        for (DBAction dbAction : dbActions) {
                            markDBAction(item, dbAction);
                        }
                    }
                    cacheDao.save(item);
                    return item;
                });
    }

    @Override
    public Observable<List<DPCache>> queryDPMsg(String account, String server, String uuid, Long version, Integer msgId, Boolean asc, Integer limit, DBAction... dbActions) {
        QueryBuilder<DPCache> builder = buildQueryBuilder(account, server, uuid, null, msgId, dbActions);
        if (asc != null) {
            builder = asc ? builder.orderAsc(DPCacheDao.Properties.Version) : builder.orderDesc(DPCacheDao.Properties.Version);
            builder = asc ? builder.where(DPCacheDao.Properties.Version.ge(version)) : builder.where(DPCacheDao.Properties.Version.le(version));
        }
        if (limit != null) {
            builder.limit(limit);
        }
        return builder.rx().list();
    }

    @Override
    public Observable<List<DPCache>> markDPMsg(String account, String server, String uuid, Long version, Integer msgId, DBAction... markedAction) {
        return buildQueryBuilder(account, server, uuid, version, msgId)
                .rx().list()
                .map(items -> {
                    if (items == null || items.size() == 0) return items;
                    AppLogger.d("查询到 mark 数据" + items.size());
                    if (markedAction != null) {
                        for (DPCache item : items) {
                            for (DBAction dbAction : markedAction) {
                                markDBAction(item, dbAction);
                            }
                        }
                    }
                    cacheDao.updateInTx(items);
                    return items;
                });
    }

    private void markDBAction(DPCache cache, DBAction action) {
        switch (action.kind()) {
            case KIND_TAG:
                cache.setTag(action.action());
                break;
            case KIND_STATE:
                cache.setState(action.action());
                break;
        }
    }

    private void buildDBActionQuery(QueryBuilder<DPCache> builder, DBAction... dbActions) {
        for (DBAction dbAction : dbActions) {
            switch (dbAction.kind()) {
                case KIND_STATE:
                    builder.where(DPCacheDao.Properties.State.eq(dbAction.action()));
                    break;
                case KIND_TAG:
                    builder.where(DPCacheDao.Properties.Tag.eq(dbAction.action()));
                    break;
            }
        }

    }

    private QueryBuilder<DPCache> buildQueryBuilder(String account, String server, String uuid, Long version, Integer msgId, DBAction... dbActions) {
        QueryBuilder<DPCache> builder = cacheDao.queryBuilder();

        if (!TextUtils.isEmpty(account)) {
            builder.where(DPCacheDao.Properties.Account.eq(account));//设置 account 约束
        }

        if (!TextUtils.isEmpty(server)) {
            builder.where(DPCacheDao.Properties.Server.eq(server));//设置 server 约束
        }

        if (!TextUtils.isEmpty(uuid)) {
            builder.where(DPCacheDao.Properties.Uuid.eq(uuid));//设置 UUID 约束
        }

        if (version != null) {
            builder.where(DPCacheDao.Properties.Version.eq(version));
        }

        if (msgId != null) {
            builder.where(DPCacheDao.Properties.MsgId.eq(msgId));
        }
        if (dbActions != null) {
            buildDBActionQuery(builder, dbActions);
        }
        AppLogger.d("查询参数构建结果, account:" + account + ",server:" + server + ",uuid:" + uuid + ",version:" + version + ",msgId:" + msgId);
        return builder;
    }

    private Observable<List<DPCache>> sendDPRequest(String uuid, long version, int msgId, boolean asc, int limit) {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            JFGDPMsg msg = new JFGDPMsg(msgId, version);
            params.add(msg);
            long seq = -1;
            try {
                seq = JfgCmdInsurance.getCmd().robotGetData(uuid, params, limit, false, 0);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                subscriber.onError(e);
                AppLogger.e(e.getMessage());
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                        .filter(rsp -> rsp.seq == seq)
                        .first()
                        .timeout(10, TimeUnit.SECONDS, Observable.empty())
                )
                .map(rsp -> {
                    List<DPCache> result = new ArrayList<>(limit);
                    DPCache item;
                    for (JFGDPMsg msg : rsp.map.get(msgId)) {
//                        item = new DPCache(null, getAccount(), getServer(), uuid, msg.version, (int) msg.id, msg.packValue);
//                        result.add(item);
                    }
                    cacheDao.insertInTx(result);
                    return result;
                });
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
