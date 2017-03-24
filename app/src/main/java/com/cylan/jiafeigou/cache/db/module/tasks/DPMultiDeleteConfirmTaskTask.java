package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/3.
 */

public class DPMultiDeleteConfirmTaskTask extends BaseDPTask<BaseDPTaskResult> {
    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        return Observable.from(multiEntity)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(entity -> mDPHelper.deleteDPMsgNotConfirm(entity.getUuid(), entity.getVersion(), entity.getMsgId(), null))
                .buffer(multiEntity.size())
                .map(items -> new BaseDPTaskResult().setResultCode(0).setResultResponse(items));
    }

    @Override
    public Observable<BaseDPTaskResult> performServer(BaseDPTaskResult local) {
        AppLogger.d("no need to invoke");
        return null;
    }
}
