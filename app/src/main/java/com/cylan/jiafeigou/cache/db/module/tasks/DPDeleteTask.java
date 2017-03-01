package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPHelper;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPTask;
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

public class DPDeleteTask implements IDPTask {
    private IDPEntity entity;

    @Override
    public IDPTask init(IDPEntity entity) {
        this.entity = entity;
        return this;
    }

    @Override
    public Observable execute() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            JFGDPMsg msg = new JFGDPMsg(entity.getMsgId(), entity.getVersion());
            params.add(msg);
            try {
                long seq = JfgCmdInsurance.getCmd().robotDelData(entity.getUuid(), params, 0);
                AppLogger.d("正在执行删除任务,uuid:" + entity.getUuid() + ",msgId:" + entity.getMsgId() + ",version:" + entity.getVersion() + ",tag:" + entity.getTag() + ",state:" + entity.getState());
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                AppLogger.d("执行 task 出错了 ,错误信息为:" + e.getMessage());
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class)
                        .filter(rsp -> rsp.seq == seq)
                        .first().timeout(15, TimeUnit.SECONDS)
                )
                .filter(rsp -> rsp.resultCode == 0)
                .flatMap(rsp -> BaseDPHelper.getInstance().deleteDPMsgWithConfirm(entity.getUuid(), entity.getVersion(), entity.getMsgId()));
    }
}
