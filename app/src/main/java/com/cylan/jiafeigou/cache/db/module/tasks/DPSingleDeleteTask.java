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
        return mDPHelper.deleteDPMsgNotConfirm(singleEntity.getUuid(), singleEntity.getVersion(), singleEntity.getMsgId())
                .map(dpEntity -> new BaseDPTaskResult().setResultCode(0).setResultResponse(dpEntity));
    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            JFGDPMsg msg = new JFGDPMsg(singleEntity.getMsgId(), singleEntity.getVersion());
            params.add(msg);
            try {
                long seq = JfgCmdInsurance.getCmd().robotDelData(singleEntity.getUuid() == null ? "" : singleEntity.getUuid(), params, 0);
                subscriber.onNext(seq);
                subscriber.onCompleted();
                AppLogger.d("正在执行删除任务,uuid:" + singleEntity.getUuid() + ",msgId:" + singleEntity.getMsgId() + ",version:" + singleEntity.getVersion() + ",action:" + singleEntity.getAction() + ",state:" + singleEntity.getState());
            } catch (JfgException e) {
                e.printStackTrace();
                subscriber.onCompleted();
                AppLogger.d("执行 task 出错了 ,错误信息为:" + e.getMessage());
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(this::makeDeleteDataRspResponse)
                .flatMap(rsp -> mDPHelper.deleteDPMsgWithConfirm(singleEntity.getUuid(), singleEntity.getVersion(), singleEntity.getMsgId())
                        .map(cache -> new BaseDPTaskResult().setResultCode(rsp.resultCode).setResultResponse(cache)));
    }
}
