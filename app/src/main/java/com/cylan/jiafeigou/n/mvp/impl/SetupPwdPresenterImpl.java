package com.cylan.jiafeigou.n.mvp.impl;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.login.SetupPwdContract;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

;

/**
 * Created by cylan-hunt on 16-6-30.
 */
public class SetupPwdPresenterImpl extends AbstractPresenter<SetupPwdContract.View>
        implements SetupPwdContract.Presenter {

    public SetupPwdPresenterImpl(SetupPwdContract.View view) {
        super(view);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                resultLoginSub(),
                registerBack()
        };
    }

    @Override
    public void register(final String account, final String pwd, final int type, final String token) {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe(s -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().register(JFGRules.getLanguageType(ContextUtils.getContext()), account, pwd, type, token);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, throwable -> AppLogger.e("god..." + throwable.getLocalizedMessage()));
    }

    @Override
    public void executeLogin(final LoginAccountBean login) {
        Observable.just(login)
                .subscribeOn(Schedulers.io())
                .map(o -> {
                    try {
                        AutoSignIn.getInstance().autoSave(o.userName, 1, o.pwd, true);
                        AutoSignIn.getInstance().autoLogin();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return o;
                })
                .subscribe(ret -> {
                }, AppLogger::e);
    }

    @Override
    public Subscription registerBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultRegister.class)
                .observeOn(AndroidSchedulers.mainThread())
                .throttleFirst(1000L, TimeUnit.MICROSECONDS)
                .subscribe(register -> {
                    //注册成功
                    PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN, "");
                    getView().submitResult(register);
                }, e -> AppLogger.d(e.getMessage()));
    }

    private Subscription resultLoginSub() {
        //sdk中，登陆失败的话，自动一分钟登录一次。
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultLogin.class)
                .delay(500, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultLogin -> {
                    if (getView() != null) {
                        getView().loginResult(resultLogin.code);
                    }
                }, throwable -> AppLogger.e("" + throwable));
    }
}
