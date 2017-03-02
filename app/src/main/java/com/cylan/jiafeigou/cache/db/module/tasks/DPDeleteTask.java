package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDPHelper;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public class DPDeleteTask extends BaseDPTask<BaseDPTaskResult> {

    @Override
    public Observable<BaseDPTaskResult> execute() {
        return BaseDPHelper.getInstance().deleteDPMsgNotConfirm(entity.getUuid(), entity.getVersion(), entity.getMsgId())
                .observeOn(Schedulers.io())
                .filter(cache -> DataSourceManager.getInstance().isOnline())

                .map(cache -> {
                    ArrayList<JFGDPMsg> params = new ArrayList<>();
                    JFGDPMsg msg = new JFGDPMsg(entity.getMsgId(), entity.getVersion());
                    params.add(msg);
                    long seq = -1;
                    try {
                        seq = JfgCmdInsurance.getCmd().robotDelData(entity.getUuid(), params, 0);
                        AppLogger.d("正在执行删除任务,uuid:" + entity.getUuid() + ",msgId:" + entity.getMsgId() + ",version:" + entity.getVersion() + ",tag:" + entity.getTag() + ",state:" + entity.getState());
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.d("执行 task 出错了 ,错误信息为:" + e.getMessage());
                    }
                    return seq;
                })
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class)
                        .filter(rsp -> rsp.seq == seq)
                        .first().timeout(15, TimeUnit.SECONDS)
                )
                .filter(rsp -> rsp.resultCode == 0)
                .flatMap(rsp -> BaseDPHelper.getInstance().deleteDPMsgWithConfirm(entity.getUuid(), entity.getVersion(), entity.getMsgId())
                        .map(cache -> new BaseDPTaskResult().setResultCode(0).setResultResponse(cache)));
    }
}
