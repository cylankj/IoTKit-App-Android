package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;

import java.util.ArrayList;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public class DPSingleDeleteTask extends BaseDPTask<BaseDPTaskResult> {

    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        if (!sourceManager.isOnline()) {
            return Observable.just(new BaseDPTaskResult().setResultCode(-1).setMessage("当前网络无法发生请求到服务器"));
        }
        return dpHelper.deleteDPMsgNotConfirm(entity.getUuid(), entity.getVersion(), entity.getMsgId(), null)
                .map(dpEntity -> new BaseDPTaskResult().setResultCode(0).setResultResponse(dpEntity));
    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            ArrayList<JFGDPMsg> params = new ArrayList<>(1);
            JFGDPMsg msg = new JFGDPMsg(entity.getMsgId(), entity.getVersion());
            params.add(msg);
            try {
                long seq = appCmd.robotDelData(entity.getUuid() == null ? "" : entity.getUuid(), params, 0);
                if (seq <= 0) {
                    throw new JfgException("内部错误!");
                }
                AppLogger.w("正在执行删除任务,seq:" + seq + ", uuid:" + entity.getUuid() + ",msgId:" + entity.getMsgId() + ",version:" + entity.getVersion() + ",option:" + entity.action());
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                subscriber.onError(e);
                AppLogger.e("执行 task 出错了 ,错误信息为:" + MiscUtils.getErr(e));
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(this::makeDeleteDataRspResponse)
                .flatMap(rsp -> {
                            if (rsp.resultCode == 0) {
                                return dpHelper.deleteDPMsgWithConfirm(entity.getUuid(), entity.getVersion(), entity.getMsgId(), null)
                                        .map(cache -> new BaseDPTaskResult().setResultCode(rsp.resultCode).setResultResponse(rsp));
                            } else {
                                return Observable.just(new BaseDPTaskResult().setResultCode(rsp.resultCode).setResultResponse(rsp));
                            }
                        }
                );
    }
}
