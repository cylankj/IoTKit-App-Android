package com.cylan.jiafeigou.n.mvp.impl.splash;


import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by hunt on 16-5-14.
 */
public class SplashPresenterImpl extends AbstractPresenter<SplashContract.View>
        implements SplashContract.Presenter {
    private Subscription splashSubscription;
    private CompositeSubscription compositeSubscription;

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
        Observable.just(GlobalDataProxy.getInstance().isOnline()
                && GlobalDataProxy.getInstance().getJfgAccount() != null)
                .subscribeOn(Schedulers.newThread())
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return !GlobalDataProxy.getInstance().isOnline()
                                && GlobalDataProxy.getInstance().getJfgAccount() != null;
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

    /**
     * 自动登录
     */
    @Override
    public void autoLogin(LoginAccountBean login) {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        try {
                            JfgCmdInsurance.getCmd().login(login.userName, login.pwd);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e("autoLogin" + throwable.getLocalizedMessage());
                });
    }

    @Override
    public String getTempAccPwd() {
        String decrypt = "";
        String dataFromFile = FileUtils.getDataFromFile(getView().getContext());
        if (TextUtils.isEmpty(dataFromFile)){
            return "";
        }
        try {
            decrypt = AESUtil.decrypt(dataFromFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypt;
    }

    @Override
    public Subscription resultLoginSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultLogin.class)
                .delay(500, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.ResultLogin resultLogin) -> {
                    if (getView() != null)
                    getView().loginResult(resultLogin.code);
                }, (Throwable throwable) -> {
                    AppLogger.e("" + throwable);
                });
    }


    @Override
    public void start() {
        super.start();
        splashSubscription = Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .delay(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        getView().splashOver();
                    }
                });

        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(resultLoginSub());
        }
    }

    @Override
    public void stop() {
        super.stop();
        unSubscribe(splashSubscription,compositeSubscription);
    }
}

