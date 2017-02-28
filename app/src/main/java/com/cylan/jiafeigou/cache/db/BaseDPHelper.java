package com.cylan.jiafeigou.cache.db;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.google.gson.Gson;

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

public class BaseDPHelper implements DPHelperInterface {
    private DPCacheDao cacheDao;
    private static BaseDPHelper instance;
    private Gson mGson = new Gson();

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
    public Observable saveDPByte(String uuid, long version, int msgId, byte[] bytes) {
        AppLogger.d("正在存数据,uuid:" + uuid + ",version:" + version + "msgId:" + msgId);
        return cacheDao.queryBuilder()
                .where(DPCacheDao.Properties.Uuid.eq(uuid), DPCacheDao.Properties.Version.eq(version), DPCacheDao.Properties.MsgId.eq(msgId))
                .rx()
                .unique()
                .map(result -> {
                    if (result != null) {
                        result.setTag("SAVED");
                        result.setState("SUCCESS");
                    } else {
                        result = new DPCache(null, getAccount(), getServer(), uuid, version, msgId, bytes, "SAVED", "SUCCESS");
                    }
                    cacheDao.save(result);
                    return result;
                });
    }

    @Override
    public Observable<DPCache> deleteDPMsgNotConfirm(String uuid, long version, int msgId) {
        AppLogger.d("正在删除本地数据,deleteDPMsgNotConfirm,uuid:" + uuid + ",version:" + version + ",msgId:" + msgId);
        QueryBuilder<DPCache> builder = cacheDao.queryBuilder();
        if (!TextUtils.isEmpty(uuid)) builder.where(DPCacheDao.Properties.Uuid.eq(uuid));
        return builder.where(DPCacheDao.Properties.Version.eq(version), DPCacheDao.Properties.MsgId.eq(msgId))
                .rx()
                .unique()
                .map(result -> {
                    if (result != null) {
                        result.setTag("DELETED");
                        result.setState("NOT_CONFIRM");
                        cacheDao.save(result);
                    }
                    return result;
                });
    }

    @Override
    public Observable<DPCache> deleteDPMsgWithConfirm(String uuid, long version, int msgId) {
        AppLogger.d("正在删除本地数据,deleteDPMsgWithConfirm,uuid:" + uuid + ",version:" + version + ",msgId:" + msgId);
        QueryBuilder<DPCache> builder = cacheDao.queryBuilder();
        if (!TextUtils.isEmpty(uuid)) builder.where(DPCacheDao.Properties.Uuid.eq(uuid));
        return builder.where(DPCacheDao.Properties.Version.eq(version), DPCacheDao.Properties.MsgId.eq(msgId))
                .rx()
                .unique()
                .filter(result -> result != null)
                .map(result -> {
                    result.setTag("DELETED");
                    result.setState("SUCCESS");
                    cacheDao.save(result);
                    return result;
                });
    }

    @Override
    public Observable<Boolean> deleteDPMsgWithConfirm(String uuid, int msgId) {
        AppLogger.d("正在删除本地数据,deleteDPMsgWithConfirm,uuid:" + uuid + ",msgId:" + msgId);
        QueryBuilder<DPCache> builder = cacheDao.queryBuilder();
        if (!TextUtils.isEmpty(uuid)) builder.where(DPCacheDao.Properties.Uuid.eq(uuid));
        return builder.where(DPCacheDao.Properties.MsgId.eq(msgId))
                .rx()
                .list()
                .filter(items -> items.size() > 0)
                .map(result -> {
                    for (DPCache cache : result) {
                        if (TextUtils.equals("NOT_CONFIRM", cache.getTag())) {
                            cache.setState("SUCCESS");
                        }
                    }
                    cacheDao.saveInTx(result);
                    return true;
                });
    }

    @Override
    public Observable<List<DPCache>> queryUnConfirmDpMsgWithTag(String uuid, int msgId, String tag) {
        AppLogger.d("正在查询本地未经确认的数据withTag:" + tag);
        QueryBuilder<DPCache> builder = cacheDao.queryBuilder();
        if (!TextUtils.isEmpty(uuid)) builder.where(DPCacheDao.Properties.Uuid.eq(uuid));
        return builder.where(DPCacheDao.Properties.MsgId.eq(msgId), DPCacheDao.Properties.Tag.eq("DELETED"), DPCacheDao.Properties.State.eq("NOT_CONFIRM"))
                .orderDesc(DPCacheDao.Properties.Version)
                .rx()
                .list();
    }

    @Override
    public Observable<List<DPCache>> queryDPMsg(String uuid, long version, int msgId, boolean asc, int limit) {
        //先从服务器上查询最新的 version
        QueryBuilder<DPCache> builder = cacheDao.queryBuilder();
        if (!TextUtils.isEmpty(uuid)) builder.where(DPCacheDao.Properties.Uuid.eq(uuid));
        builder.where(DPCacheDao.Properties.MsgId.eq(msgId),
                asc ? DPCacheDao.Properties.Version.ge(version) : DPCacheDao.Properties.Version.le(version), DPCacheDao.Properties.Tag.eq("SAVED")
        )
                .limit(limit);
        if (asc) {
            builder.orderAsc(DPCacheDao.Properties.Version);
        } else {
            builder.orderDesc(DPCacheDao.Properties.Version);
        }
        return builder.rx().list();

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
        private Context mContext;

        public GreenDaoContext() {
            super(ContextUtils.getContext());
            this.mContext = ContextUtils.getContext();
        }

        /**
         * 获得数据库路径，如果不存在，则创建对象
         *
         * @param dbName
         */
        @Override
        public File getDatabasePath(String dbName) {
            File baseFile = new File(JConstant.ROOT_DIR + "/db", dbName);
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
         * @see android.content.ContextWrapper#openOrCreateDatabase(java.lang.String, int,
         * android.database.sqlite.SQLiteDatabase.CursorFactory,
         * android.database.DatabaseErrorHandler)
         */
        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory,
                                                   DatabaseErrorHandler errorHandler) {
            SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);

            return result;
        }

    }
}
