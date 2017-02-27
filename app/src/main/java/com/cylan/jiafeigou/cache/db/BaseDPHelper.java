package com.cylan.jiafeigou.cache.db;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
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
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(ContextUtils.getContext(), "dp_cache.db");
        DaoMaster master = new DaoMaster(helper.getEncryptedWritableDb("123456"));
        DaoSession daoSession = master.newSession();
        cacheDao = daoSession.getDPCacheDao();
    }

    @Override
    public Observable saveDPByte(String uuid, long version, int msgId, byte[] bytes) {
        cacheDao.queryBuilder()
                .where(DPCacheDao.Properties.Uuid.eq(uuid), DPCacheDao.Properties.Version.eq(version), DPCacheDao.Properties.MsgId.eq(msgId))
                .rx()
                .unique()
                .map(result -> {
                    if (result == null) {
                        result = new DPCache(null, null, null, uuid, version, msgId, null);
                    }
                    cacheDao.save(result);
                    return result;
                });
        return null;
    }

    @Override
    public Observable queryDPMsg(String uuid, long version, int msgId, int limit) {
        return cacheDao.queryBuilder().where(
                DPCacheDao.Properties.Uuid.eq(uuid),
                DPCacheDao.Properties.Version.le(version),
                DPCacheDao.Properties.MsgId.eq(msgId))
                .rx()
                .list()
                .mergeWith(sendDPRequest(uuid, version, msgId, limit))
                .map(result -> {
                    Collections.sort(result, (lhs, rhs) -> (int) (lhs.getVersion() - rhs.getVersion()));
                    return result;
                });
    }

    private Observable<List<DPCache>> sendDPRequest(String uuid, long version, int msgId, int limit) {
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
                        item = new DPCache(null, null, null, uuid, msg.version, (int) msg.id, msg.packValue);
                        result.add(item);
                    }
                    cacheDao.insertInTx(result);
                    return result;
                });
    }
}
