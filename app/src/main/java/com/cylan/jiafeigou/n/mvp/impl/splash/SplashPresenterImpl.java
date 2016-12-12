package com.cylan.jiafeigou.n.mvp.impl.splash;


import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by hunt on 16-5-14.
 */
public class SplashPresenterImpl extends AbstractPresenter<SplashContract.View>
        implements SplashContract.Presenter {
    private Subscription splashSubscription;

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
    public void resumeLogin() {
        Observable.just(JCache.isOnline())
                .subscribeOn(Schedulers.newThread())
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return !JCache.isOnline();
                    }
                })
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        String a = PreferencesUtils.getString("wth_a");
                        String p = PreferencesUtils.getString("wth_p");
                        if (TextUtils.isEmpty(a) || TextUtils.isEmpty(p))
                            return;
                        AppLogger.i("auto login");
                        try {
                            JfgCmdInsurance.getCmd().login(a, p);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                });
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

