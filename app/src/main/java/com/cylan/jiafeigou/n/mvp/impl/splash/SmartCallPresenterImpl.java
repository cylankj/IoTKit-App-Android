package com.cylan.jiafeigou.n.mvp.impl.splash;


import com.cylan.jiafeigou.ads.AdsStrategy;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.module.LoginHelper;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
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
    public void deciderShowAdvert() {
        Subscription subscribe = AdsStrategy.getStrategy().needShowAds()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adsDescription -> {
                    if (adsDescription != null) {
                        mView.onShowAdvert(adsDescription);
                    } else {
                        mView.onAdvertOver();
                    }
                }, error -> {
                });
        addDestroySubscription(subscribe);
    }

    @Override
    public void performAutoLogin() {
        Subscription subscribe = LoginHelper.performAutoLogin()
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountArrived -> {
                    if (accountArrived != null) {
                        getView().onAutoLoginSuccess();
                    } else {
                        getView().onAutoLoginFailed();
                    }
                }, error -> {
                    error.printStackTrace();
                    AppLogger.e(error);
                    if (error instanceof RxEvent.HelperBreaker) {
                        int code = ((RxEvent.HelperBreaker) error).breakerCode;
                        switch (code) {
                            case JError.ErrorLoginInvalidPass: {
                                getView().onPasswordChanged();
                            }
                            break;
                            default: {
                                getView().onAutoLoginFailed();
                            }
                        }
                    } else {
                        getView().onAutoLoginFailed();
                    }
                });
        addStopSubscription(subscribe);
    }
}

