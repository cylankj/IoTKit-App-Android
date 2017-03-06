package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.os.Bundle;
import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bind.ScanContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-7.
 */
public class ScanPresenterImpl extends AbstractPresenter<ScanContract.View> implements ScanContract.Presenter {

    private Subscription subscription;

    public ScanPresenterImpl(ScanContract.View v) {
        super(v);
        v.setPresenter(this);
    }

    @Override
    public void submit(Bundle bundle) {
        unSubscribe(subscription);
        subscription = Observable.just("bind")
                .subscribeOn(Schedulers.newThread())
                .map(s -> {
                    String code = DataSourceManager.getInstance().getJFGAccount().getAccount() + System.currentTimeMillis();
                    try {
                        return JfgCmdInsurance.getCmd().bindDevice(bundle.getString("sn"), code);
                    } catch (JfgException e) {
                        AppLogger.e("scan Bind Err: " + e.getLocalizedMessage());
                        return -1;
                    }
                })
                .flatMap(new Func1<Integer, Observable<RxEvent.BindDeviceEvent>>() {
                    @Override
                    public Observable<RxEvent.BindDeviceEvent> call(Integer integer) {
                        return RxBus.getCacheInstance().toObservable(RxEvent.BindDeviceEvent.class)
                                .observeOn(Schedulers.newThread())
                                .filter((RxEvent.BindDeviceEvent bindDeviceEvent) -> getView() != null && TextUtils.equals(getView().getUuid(), bindDeviceEvent.uuid))
                                .timeout(90, TimeUnit.SECONDS, Observable.just("timeout")
                                        .subscribeOn(AndroidSchedulers.mainThread())
                                        .filter(s -> getView() != null)
                                        .map(s -> {
                                            getView().onScanRsp(-1);
                                            AppLogger.e("timeout: " + s);
                                            return null;
                                        }));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(s -> getView() != null)
                .map(new Func1<RxEvent.BindDeviceEvent, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.BindDeviceEvent bindDeviceEvent) {
                        getView().onScanRsp(bindDeviceEvent.bindResult);
                        return null;
                    }
                })
                .doOnError(throwable -> AppLogger.e("scan Bind err:" + throwable.getLocalizedMessage()))
                .subscribe();
    }

    @Override
    public void stop() {
        super.stop();
        unSubscribe(subscription);
    }
}
