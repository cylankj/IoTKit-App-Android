package com.cylan.jiafeigou.n.mvp.impl.splash;


import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

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

    public SmartCallPresenterImpl(SplashContract.View splashView) {
        super(splashView);
    }

    @Override
    public void start() {
        super.start();
    }

    public void autoLogin() {
        BaseApplication.getAppComponent().getInitializationManager().observeInitFinish();
        Subscription subscribe = RxBus.getCacheInstance().toObservableSticky(RxEvent.GlobalInitFinishEvent.class)
                .first()
                .observeOn(Schedulers.io())
                .subscribe(event -> AutoSignIn.getInstance().autoLogin(), AppLogger::e);
        addSubscription(subscribe);
    }

    public void selectNext(boolean showSplash) {
        Subscription subscribe = Observable.just(showSplash)
                .flatMap(show -> show ? Observable.just("正在显示 splash 页面,请等待2秒钟...").delay(2, TimeUnit.SECONDS) : Observable.just("不显示 splash 页面"))
                .flatMap(msg -> RxBus.getCacheInstance().toObservableSticky(RxEvent.ResultLogin.class).first())
                .observeOn(AndroidSchedulers.mainThread())
                .map(resultLogin -> {
                    if (resultLogin.code != JError.ErrorOK && resultLogin.code != JError.LoginTimeOut) {//登录失败
                        getView().loginError(resultLogin.code);
                    }
                    AppLogger.d("login result: " + resultLogin);
                    return resultLogin;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class).first())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountArrived -> getView().loginSuccess(), AppLogger::e);
        addSubscription(subscribe);
    }
}

