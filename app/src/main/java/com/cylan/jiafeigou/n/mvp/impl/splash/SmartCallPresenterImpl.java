package com.cylan.jiafeigou.n.mvp.impl.splash;


import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.concurrent.TimeUnit;

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
        if (RxBus.getCacheInstance().hasStickyEvent(RxEvent.ResultLogin.class)) {
            subscription = RxBus.getCacheInstance().toObservableSticky(RxEvent.ResultLogin.class)
                    .subscribeOn(Schedulers.newThread())
                    .delay(200, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                    .map(resultLogin -> {
                        if (resultLogin != null)
                            getView().loginResult(resultLogin.code);
                        return null;
                    })
                    .subscribe();
        } else {
            AppLogger.d("zhixing zhege");
            getView().splashOver();
        }
        super.start();
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

