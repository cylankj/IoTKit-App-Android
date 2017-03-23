package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/7.
 */

public class DPSingleClearTask extends BaseDPTask<BaseDPTaskResult> {
    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        return mDPHelper.deleteDPMsgWithConfirm(entity.getUuid(), entity.getMsgId(), null)
                .map(items -> {
                    DPEntity entity = null;
                    if (items != null && items.size() > 0) {
                        entity = items.get(0);
                        entity.setAction(DBAction.CLEARED);
                        entity.setState(DBState.NOT_CONFIRM);
                        this.entity = entity;
                    }
                    return new BaseDPTaskResult().setResultCode(0).setResultResponse(entity);
                });
    }

    @Override
    public Observable<BaseDPTaskResult> performServer(BaseDPTaskResult local) {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            JFGDPMsg request = new JFGDPMsg(DpMsgMap.ID_401_BELL_CALL_STATE, -1);
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            params.add(request);
            try {
                long seq = JfgCmdInsurance.getCmd().robotDelData(entity.getUuid(), params, 0);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                AppLogger.e(e.getMessage());
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(this::makeDeleteDataRspResponse)
                .flatMap(rsp -> {
                    if (rsp.resultCode == 0) {
                        return mDPHelper.deleteDPMsgWithConfirm(entity.getUuid(), entity.getVersion(), entity.getMsgId(), null)
                                .map(cache -> new BaseDPTaskResult().setResultCode(rsp.resultCode).setResultResponse(rsp));
                    } else {
                        return Observable.just(new BaseDPTaskResult().setResultCode(rsp.resultCode).setResultResponse(rsp));
                    }
                });
    }
}
