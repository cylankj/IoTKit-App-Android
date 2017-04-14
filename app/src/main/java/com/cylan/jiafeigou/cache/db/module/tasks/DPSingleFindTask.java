package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/24.
 */

public class DPSingleFindTask extends BaseDPTask<BaseDPTaskResult> {
    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        if (sourceManager.isOnline()) {//如果在线则直接返回
            return Observable.just(BaseDPTaskResult.SUCCESS);
        }
        return null;
    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                ArrayList<JFGDPMsg> params = new ArrayList<>(1);
                JFGDPMsg msg = new JFGDPMsg(entity.getMsgId(), entity.getVersion());
                params.add(msg);
                long seq = appCmd.robotGetDataByTime(entity.getUuid(), params, 0);
                AppLogger.d("正在检查DP消息, uuid为:" + entity.getUuid() + ",msgId为:" + entity.getMsgId() + ",version 为:" + entity.getVersion() + ",seq 为:" + seq);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                AppLogger.e(e.getMessage());
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(this::makeGetDataRspResponse)
                .map(rsp -> {
                    AppLogger.d("收到从服务器返回数据!!!");
                    DataPoint dataPoint = sourceManager.getValue(entity.getUuid(), entity.getMsgId(), null);
                    Object result = null;
                    if (dataPoint == null) {
                        result = null;
                    } else if (dataPoint instanceof DpMsgDefine.DPPrimary) {
                        result = dataPoint;
                    }
                    return new BaseDPTaskResult().setResultResponse(result).setResultCode(0);
                });
        return null;
    }


}
