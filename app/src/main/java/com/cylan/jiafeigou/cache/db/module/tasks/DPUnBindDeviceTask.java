package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.misc.JResultEvent;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/9.
 */

public class DPUnBindDeviceTask extends BaseDPTask<BaseDPTaskResult> {
    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        return mDPHelper.unBindDeviceNotConfirm(entity.getUuid()).map(device -> new BaseDPTaskResult().setResultCode(0).setResultResponse(device));
    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                int seq = JfgCmdInsurance.getCmd().unBindDevice(entity.getUuid());
                subscriber.onNext((long) seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                subscriber.onError(e);
                AppLogger.e(e.getMessage());
                e.printStackTrace();
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(this::makeGetJFGResultResponse)
                .filter(rsp -> rsp.event == JResultEvent.JFG_RESULT_UNBINDDEV)
                .first()
                .timeout(10, TimeUnit.SECONDS)
                .flatMap(rsp -> {
                    if (rsp.code == 0) {//成功
                        return mDPHelper.unBindDeviceWithConfirm(entity.getUuid()).map(device -> new BaseDPTaskResult().setResultCode(rsp.code).setResultResponse(device));
                    } else {
                        return Observable.just(new BaseDPTaskResult().setResultCode(rsp.code).setResultResponse(null));
                    }
                });
    }
}
