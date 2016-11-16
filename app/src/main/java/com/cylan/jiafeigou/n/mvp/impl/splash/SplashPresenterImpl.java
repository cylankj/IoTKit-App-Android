package com.cylan.jiafeigou.n.mvp.impl.splash;


import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by hunt on 16-5-14.
 */
public class SplashPresenterImpl extends AbstractPresenter<SplashContract.View>
        implements SplashContract.Presenter {
    Subscription splashSubscription;

    public SplashPresenterImpl(SplashContract.View splashView) {
        super(splashView);
        splashView.setPresenter(this);
    }


    @Override
    public void finishAppDelay() {
        AppLogger.w("deny sdcard permission");
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }


    @Override
    public void start() {
        splashSubscription = Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .delay(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        getView().splashOver();
                    }
                });
    }

    @Override
    public void stop() {
        unSubscribe(splashSubscription);
    }
}

