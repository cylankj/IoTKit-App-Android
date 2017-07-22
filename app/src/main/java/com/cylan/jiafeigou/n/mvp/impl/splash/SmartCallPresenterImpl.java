package com.cylan.jiafeigou.n.mvp.impl.splash;


import android.util.Log;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.ads.AdsStrategy;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hunt on 16-5-14.
 */
public class SmartCallPresenterImpl extends AbstractPresenter<SplashContract.View>
        implements SplashContract.Presenter {

    public SmartCallPresenterImpl(SplashContract.View splashView) {
        super(splashView);
    }

    @Override
    public void start() {
        super.start();
    }

    public void autoLogin() {
        AppLogger.d("before autoLogin");
        PerformanceUtils.startTrace("autoLogin");
        Subscription subscribe = RxBus.getCacheInstance().toObservableSticky(RxEvent.GlobalInitFinishEvent.class).map(event -> true)
                .first()
//                .observeOn(Schedulers.io())
                .subscribe(event -> AutoSignIn.getInstance().autoLogin(), AppLogger::e);
        addSubscription(subscribe);
        BaseApplication.getAppComponent().getInitializationManager().observeInitFinish();
    }

    public void selectNext(boolean showSplash) {
        PerformanceUtils.startTrace("selectNext");
        Subscription subscribe = Observable.just(showSplash)
                .timeout(4, TimeUnit.SECONDS)
                .flatMap(show -> show ? Observable.just("正在显示 splash 页面,请等待2秒钟...").delay(1, TimeUnit.SECONDS) : Observable.just("不显示 splash 页面"))
                .flatMap(msg -> RxBus.getCacheInstance().toObservableSticky(RxEvent.ResultLogin.class).first())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(resultLogin -> {
                    PerformanceUtils.startTrace("ResultLogin");
                    boolean loginSuccess = false;
                    if (resultLogin.code == JError.ErrorOK || resultLogin.code == JError.ERROR_OFFLINE_LOGIN) {//登录失败
                        loginSuccess = true;
                    } else {
                        getView().loginError(resultLogin.code);
                        PerformanceUtils.stopTrace("selectNext");
                        PerformanceUtils.stopTrace("ResultLogin");
                    }
                    AppLogger.d("login result: " + resultLogin);
                    return loginSuccess;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class).first())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountArrived -> {
                    getView().loginSuccess();
                    PerformanceUtils.stopTrace("ResultLogin");
                    PerformanceUtils.stopTrace("selectNext");
                    throw new RxEvent.HelperBreaker("主动结束");
                }, throwable -> {
                    if (throwable instanceof TimeoutException) {
                        AppLogger.d("超时");
                        getView().loginSuccess();
                    }
                });
        addSubscription(subscribe);
    }

    @Override
    public Observable<AdsStrategy.AdsDescription> showAds() {
        return AdsStrategy.getStrategy().needShowAds();
    }

    @Override
    public void reEnableSmartcallLog() {
        Subscription subscribe = RxBus.getCacheInstance().toObservableSticky(RxEvent.GlobalInitFinishEvent.class).map(event -> true)
                .first()
                .observeOn(Schedulers.io())
                .subscribe(event -> {
                    try {
                        Log.d("initAppCmd", "reEnableSmartcallLog start");
                        BaseApplication.getAppComponent()
                                .getCmd().enableLog(false, BaseApplication.getAppComponent().getLogPath());
                        BaseApplication.getAppComponent()
                                .getCmd().enableLog(true, BaseApplication.getAppComponent().getLogPath());
                        throw new RxEvent.HelperBreaker("reEnableLog");
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
        addSubscription(subscribe);
    }
}

