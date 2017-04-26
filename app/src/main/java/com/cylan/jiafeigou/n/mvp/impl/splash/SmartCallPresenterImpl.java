package com.cylan.jiafeigou.n.mvp.impl.splash;


import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

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
        if (RxBus.getCacheInstance().hasStickyEvent(RxEvent.ResultLogin.class) && !PreferencesUtils.getBoolean(JConstant.UPDATAE_AUTO_LOGIN, false)) {
            AppLogger.d("has sticky");
            subscription = RxBus.getCacheInstance().toObservableSticky(RxEvent.ResultLogin.class)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                    .map(resultLogin -> {
                        if (resultLogin != null && getView() != null)
                            getView().loginResult(resultLogin.code);
                        AppLogger.d("login result: " + resultLogin);
                        return null;
                    })
                    .subscribe(ret -> {
                    }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
        } else {
            AppLogger.d("has no sticky");
            Observable.just("delay").delay(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(ret -> getView().splashOver(), AppLogger::e);
        }
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

