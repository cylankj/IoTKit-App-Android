package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public class DPSingleDeleteTask extends BaseDPTask<BaseDPTaskResult> {

    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        return mDPHelper.deleteDPMsgNotConfirm(entity.getUuid(), entity.getVersion(), entity.getMsgId(), null)
                .map(dpEntity -> new BaseDPTaskResult().setResultCode(0).setResultResponse(dpEntity));
    }

    @Override
    public Observable<BaseDPTaskResult> performServer(BaseDPTaskResult local) {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            ArrayList<JFGDPMsg> params = new ArrayList<>(1);
            JFGDPMsg msg = new JFGDPMsg(entity.getMsgId(), entity.getVersion());
            params.add(msg);
            try {
                long seq = JfgCmdInsurance.getCmd().robotDelData(entity.getUuid() == null ? "" : entity.getUuid(), params, 0);
                AppLogger.d("正在执行删除任务,seq:" + seq + ", uuid:" + entity.getUuid() + ",msgId:" + entity.getMsgId() + ",dpMsgVersion:" + entity.getVersion() + ",option:" + entity.action());
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                subscriber.onCompleted();
                AppLogger.d("执行 task 出错了 ,错误信息为:" + e.getMessage());
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(this::makeDeleteDataRspResponse)
                .flatMap(rsp -> {
                            if (rsp.resultCode == 0) {
                                return mDPHelper.deleteDPMsgWithConfirm(entity.getUuid(), entity.getVersion(), entity.getMsgId(), null)
                                        .map(cache -> new BaseDPTaskResult().setResultCode(rsp.resultCode).setResultResponse(rsp));
                            } else {
                                return Observable.just(new BaseDPTaskResult().setResultCode(rsp.resultCode).setResultResponse(rsp));
                            }
                        }
                );
    }
}
