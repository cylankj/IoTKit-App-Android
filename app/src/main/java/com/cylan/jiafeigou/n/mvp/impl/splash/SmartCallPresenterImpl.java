package com.cylan.jiafeigou.n.mvp.impl.splash;


import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
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
public class SmartCallPresenterImpl extends AbstractPresenter<SplashContract.View>
        implements SplashContract.Presenter {

    public SmartCallPresenterImpl(SplashContract.View splashView) {
        super(splashView);
        splashView.setPresenter(this);
//        RxBus.getCacheInstance().toObservableSticky(RxEvent.ResultLogin.class)
//                .subscribeOn(Schedulers.newThread())
//                .filter(resultLogin -> resultLogin.code == 0)
//                .timeout(2, TimeUnit.SECONDS, Observable.just("autoLogTimeout")
//                        .subscribeOn(AndroidSchedulers.mainThread())
//                        .map(s -> {
//                            AppLogger.e("" + s);
//                            getView().splashOver();
//                            return null;
//                        }))
//                .delay(3000, TimeUnit.MILLISECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
////                .doOnCompleted(() -> getView().loginResult(0))
//                .subscribe(resultLogin1 -> {
//                    getView().loginResult(0);
//                });

        RxBus.getCacheInstance().toObservableSticky(RxEvent.ResultLogin.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultLogin -> {
                    getView().loginResult(resultLogin.code);
                });
    }


    @Override
    public void finishAppDelay() {
        AppLogger.w("deny sdcard permission");
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

}

