package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGResult;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.misc.JResultEvent;
import com.cylan.jiafeigou.rx.RxBus;
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
        if (!sourceManager.isOnline()) {
            return Observable.just(new BaseDPTaskResult().setResultCode(-1).setMessage("当前网络无法发生请求到服务器"));
        }
        return dpHelper.unBindDeviceNotConfirm(entity.getUuid()).map(device -> new BaseDPTaskResult().setResultCode(0).setResultResponse(device));
    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                int seq = appCmd.unBindDevice(entity.getUuid());
                AppLogger.d("正在删除设备+" + entity.getUuid() + "seq:" + seq);
                subscriber.onNext((long) seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                subscriber.onError(e);
                AppLogger.e(e.getMessage());
                e.printStackTrace();
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(JFGResult.class))//无法获取 seq 只能根据类型来判断了
                .filter(rsp -> rsp.event == JResultEvent.JFG_RESULT_UNBINDDEV)
                .first()
                .timeout(GLOBAL_NET_OPERATION_TIME_OUT, TimeUnit.SECONDS)
                .flatMap(rsp -> {
                    if (rsp.code == 0) {//成功
                        return sourceManager.unBindDevice(entity.getUuid())
                                .map(device -> new BaseDPTaskResult().setResultCode(rsp.code).setResultResponse(device));
                    } else {
                        return Observable.just(new BaseDPTaskResult().setResultCode(rsp.code).setResultResponse(null));
                    }
                });
    }
}
