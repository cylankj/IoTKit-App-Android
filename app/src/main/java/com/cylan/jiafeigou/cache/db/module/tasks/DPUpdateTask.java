package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPMultiTask;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult.SUCCESS;

/**
 * Created by hds on 2017/3/2.
 */

public class DPUpdateTask extends BaseDPTask<BaseDPTaskResult> {
    private DBOption.UpdateOption option;

    public DPUpdateTask() {
    }

    @Override
    public <R extends IDPMultiTask<BaseDPTaskResult>> R init(List<IDPEntity> cache) throws Exception {
        this.option = cache.get(0).option(DBOption.UpdateOption.class);
        return super.init(cache);
    }

    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        return Observable.from(multiEntity)
                .filter(ret -> ListUtils.getSize(multiEntity) > 0)
                .subscribeOn(Schedulers.io())
                .flatMap(idpEntity -> {
                    BaseDBHelper.getInstance().saveOrUpdate(idpEntity.getAccount(), null, idpEntity.getUuid(),
                            idpEntity.getVersion(), idpEntity.getMsgId(), idpEntity.getBytes(), null, null, null);
                    return Observable.just(idpEntity.getVersion());
                })
                .buffer(multiEntity.size())
                .flatMap(longs -> {
                    AppLogger.d("更新server dp: " + longs);
                    BaseDPTaskResult result = new BaseDPTaskResult();
                    result.setResultCode(0);
                    result.setResultResponse(longs);
                    return Observable.just(result);
                })
                .flatMap(baseDPTaskResult -> {
                    if (DataSourceManager.getInstance().isOnline()) {
                        performServer().doOnError(throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage())).subscribe();
                    }
                    return Observable.just(baseDPTaskResult);
                })
                .doOnError(throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()));
    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                String uuid = multiEntity.get(0).getUuid();
                ArrayList<JFGDPMsg> list = new ArrayList<>();
                for (IDPEntity entity : multiEntity) {
                    JFGDPMsg jfgdpMsg = new JFGDPMsg(entity.getMsgId(), entity.getVersion());
                    jfgdpMsg.packValue = entity.getBytes();
                    list.add(jfgdpMsg);
                }
                long seq = JfgCmdInsurance.getCmd().robotSetData(uuid, list);
                subscriber.onNext(seq);
                subscriber.onCompleted();
                AppLogger.d("更新server dp,seq:" + seq);
            } catch (Exception e) {
                AppLogger.e(e.getMessage());
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .flatMap(this::makeSetDataRspResponse)
                .doOnError(throwable -> {
                    if (throwable != null && throwable instanceof TimeoutException) {
                        AppLogger.d("更新server dp超时:");
                    }
                })
                .flatMap(ret -> {
                    AppLogger.d("更新server dp seq:" + ret.seq);
                    return Observable.just(SUCCESS);
                });
    }
}
