package com.cylan.jiafeigou.n.mvp.impl.splash;


import android.app.ActivityManager;
import android.content.Context;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.util.List;
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
public class SmartCallPresenterImpl extends AbstractPresenter<SplashContract.View>
        implements SplashContract.Presenter {

    private Subscription subscription;
    private Subscription delaySub;
    private Subscription isServiceStartSub;

    public SmartCallPresenterImpl(SplashContract.View splashView) {
        super(splashView);
    }

    @Override
    public void start() {
        serviceIsStart();
        super.start();
    }

    private void selectNext() {
        if (RxBus.getCacheInstance().hasStickyEvent(RxEvent.ResultLogin.class) && !PreferencesUtils.getBoolean(JConstant.UPDATAE_AUTO_LOGIN, false)) {
            AppLogger.d("has sticky");
            subscription = RxBus.getCacheInstance().toObservableSticky(RxEvent.ResultLogin.class)
                    .subscribeOn(Schedulers.newThread())
//                    .delay(200, TimeUnit.MILLISECONDS)
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
            getView().splashOver();
        }
    }

    @Override
    public void stop() {
        super.stop();
        unSubscribe(subscription);
        unSubscribe(delaySub);
        unSubscribe(isServiceStartSub);
    }

    @Override
    public void finishAppDelay() {
        AppLogger.w("deny sdcard permission");
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public void serviceIsStart() {
        isServiceStartSub = Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Func1<Object, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Object o) {
                        boolean isRunning = false;
                        ActivityManager activityManager = (ActivityManager) getView().getContext()
                                .getSystemService(Context.ACTIVITY_SERVICE);
                        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                                .getRunningServices(50);

                        if (!(serviceList.size() > 0)) {
                            return Observable.just(false);
                        }

                        for (int i = 0; i < serviceList.size(); i++) {
                            if (serviceList.get(i).service.getClassName().equals("DataSourceService")) {
                                isRunning = true;
                                break;
                            }
                        }
                        return Observable.just(isRunning);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(b -> {
                    if (b) {
                        selectNext();
                    } else {
                        delay1s();
                    }
                });
    }

    public void delay1s() {
        delaySub = Observable.just(null)
                .subscribeOn(AndroidSchedulers.mainThread())
                .delay(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    selectNext();
                });
    }


}

