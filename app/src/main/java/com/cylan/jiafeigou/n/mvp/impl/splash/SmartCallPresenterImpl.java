package com.cylan.jiafeigou.n.mvp.impl.splash;


import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.FileUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
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
    }


    @Override
    public void finishAppDelay() {
        AppLogger.w("deny sdcard permission");
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
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
        if (TextUtils.isEmpty(dataFromFile)) {
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
    public void start() {
        super.start();
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .delay(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        getView().splashOver();
                    }
                });

    }
}

