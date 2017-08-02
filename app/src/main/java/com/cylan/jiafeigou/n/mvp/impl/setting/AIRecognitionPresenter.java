package com.cylan.jiafeigou.n.mvp.impl.setting;

import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.setting.AIRecognitionContact;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/8/2.
 */

public class AIRecognitionPresenter extends BasePresenter<AIRecognitionContact.View> implements AIRecognitionContact.Presenter {


    @Override
    public void getObjectDetect() {
        Subscription subscribe = Observable.just(new DPEntity(uuid, DpMsgMap.ID_515_CAM_ObjectDetect, 0, DBAction.QUERY, null))
                .observeOn(Schedulers.io())
                .flatMap(this::perform)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    DpMsgDefine.DPCameraObjectDetect response = result.getResultResponse();
                    mView.onObjectDetectRsp(response.objects);
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
        registerSubscription(subscribe);
    }

    @Override
    public <T extends DataPoint> void setObjectDetect(T value) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        BaseApplication.getAppComponent().getSourceManager().updateValue(uuid, value, DpMsgMap.ID_515_CAM_ObjectDetect);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }
}
