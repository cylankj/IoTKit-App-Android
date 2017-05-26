package com.cylan.jiafeigou.n.mvp.impl.splash;


import android.text.TextUtils;

import com.cylan.jiafeigou.ads.AdsStrategy;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

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
        AppLogger.d("before autoLogin");
        Subscription subscribe = RxBus.getCacheInstance().toObservableSticky(RxEvent.GlobalInitFinishEvent.class).map(event -> true)
                .first()
                .observeOn(Schedulers.io())
                .subscribe(event -> AutoSignIn.getInstance().autoLogin(), AppLogger::e);
        addSubscription(subscribe);
        BaseApplication.getAppComponent().getInitializationManager().observeInitFinish();
    }

    public void selectNext(boolean showSplash) {
        Subscription subscribe = Observable.just(showSplash)
                .flatMap(show -> show ? Observable.just("正在显示 splash 页面,请等待2秒钟...").delay(2, TimeUnit.SECONDS) : Observable.just("不显示 splash 页面"))
                .flatMap(msg -> RxBus.getCacheInstance().toObservableSticky(RxEvent.ResultLogin.class).first())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(resultLogin -> {
                    boolean loginSuccess = false;
                    if (resultLogin.code == JError.ErrorOK || resultLogin.code == JError.ERROR_OFFLINE_LOGIN) {//登录失败
                        loginSuccess = true;
                    } else {
                        getView().loginError(resultLogin.code);
                    }
                    AppLogger.d("login result: " + resultLogin);
                    return loginSuccess;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class).first())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountArrived -> getView().loginSuccess(), AppLogger::e);
        addSubscription(subscribe);
    }

    @Override
    public Observable<AdsStrategy.AdsDescription> showAds() {
        return Observable.just("check and get ads")
                .subscribeOn(Schedulers.newThread())
                .map(s -> {
                    //1.广告页仅在加菲狗版本、中国大陆地区(简体中文)版本显示，其余版本屏蔽。
                    //2.在广告投放时间期限内，每个用户看到的广告展示次数最多为三次，广告展示次数满三次后不再显示。
                    int l = JFGRules.getLanguageType(ContextUtils.getContext());
                    if (l == JFGRules.LANGUAGE_TYPE_SIMPLE_CHINESE) {
                        //非简体中文
                        AdsStrategy.AdsDescription description = new AdsStrategy.AdsDescription();
                        description.tagUrl = "www.baidu.com";
                        description.url = "http://cdn.duitang.com/uploads/item/201208/19/20120819131358_2KR2S.thumb.600_0.png";
                        return description;
                    }
                    String content = PreferencesUtils.getString(JConstant.KEY_ADD_DESC, "");
                    if (TextUtils.isEmpty(content)) {
                        AdsStrategy.getStrategy().fetchAds();
                        return null;
                    }
                    try {
                        AdsStrategy.AdsDescription description = new Gson().fromJson(content, AdsStrategy.AdsDescription.class);
                        if (description != null) {
                            //展示两次
                            if (description.showCount > 2) {
                                PreferencesUtils.remove(JConstant.KEY_ADD_DESC);
                                AdsStrategy.getStrategy().fetchAds();
                                return null;
                            }
                            //过期了
                            if (System.currentTimeMillis() / 1000 > description.expireTime) {
                                PreferencesUtils.remove(JConstant.KEY_ADD_DESC);
                                AdsStrategy.getStrategy().fetchAds();
                                return null;
                            }
                            return description;
                        } else return null;
                    } catch (Exception e) {
                        AdsStrategy.getStrategy().fetchAds();
                        return null;
                    }
                });
    }
}

