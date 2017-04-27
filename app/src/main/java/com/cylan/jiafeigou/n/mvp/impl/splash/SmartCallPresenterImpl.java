package com.cylan.jiafeigou.n.mvp.impl.splash;


import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hunt on 16-5-14.
 */
public class SmartCallPresenterImpl extends AbstractPresenter<SplashContract.View>
        implements SplashContract.Presenter {

    private Subscription subscription;

    public SmartCallPresenterImpl(SplashContract.View splashView) {
        super(splashView);
    }

    @Override
    public void start() {
        super.start();
        selectNext();
    }

    private void selectNext() {
        if (getView().hasSplashView()) {
            Observable.just("delay").delay(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(ret -> {
                if (getView().hasSplashView()) getView().splashOver();
            }, AppLogger::e);
        } else {
            getView().splashOver();
        }
        subscription = RxBus.getCacheInstance().toObservableSticky(RxEvent.ResultLogin.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(resultLogin -> {
                    if (resultLogin != null && getView() != null)
                        getView().loginResult(resultLogin.code);
                    AppLogger.d("login result: " + resultLogin);
                    return null;
                })
                .subscribe(ret -> {
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }


    @Override
    public void stop() {
        super.stop();
        unSubscribe(subscription);
    }

    @Override
    public void finishAppDelay() {
        AppLogger.w("deny sdcard permission");
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}

