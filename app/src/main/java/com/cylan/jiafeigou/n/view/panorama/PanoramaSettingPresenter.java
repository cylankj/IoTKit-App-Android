package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/11.
 */

public class PanoramaSettingPresenter extends BasePresenter<PanoramaSettingContact.View> implements PanoramaSettingContact.Presenter {
    @Override
    public void unBindDevice() {
        Subscription subscribe = Observable.just(new DPEntity()
                .setUuid(mUUID)
                .setAction(DBAction.UNBIND))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(this::perform)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> mView.unbindDeviceRsp(rsp.getResultCode()), e -> {
                    if (e instanceof TimeoutException) {
                        mView.unbindDeviceRsp(-1);
                    }
                    AppLogger.d(e.getMessage());
                    e.printStackTrace();
                }, () -> {
                });
        registerSubscription(subscribe);
    }
}
