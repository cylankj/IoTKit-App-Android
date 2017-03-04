package com.cylan.jiafeigou.n.mvp.impl;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.login.SetupPwdContract;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
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

;

/**
 * Created by cylan-hunt on 16-6-30.
 */
public class SetupPwdPresenterImpl extends AbstractPresenter<SetupPwdContract.View>
        implements SetupPwdContract.Presenter {

    public SetupPwdPresenterImpl(SetupPwdContract.View view) {
        super(view);
        view.setPresenter(this);
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
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object s) {
                        try {
                            JfgCmdInsurance.getCmd().register(account, pwd, type, token);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("god..." + throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public void executeLogin(final LoginAccountBean login) {
        Observable.just(login)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<LoginAccountBean, LoginAccountBean>() {
                    @Override
                    public LoginAccountBean call(LoginAccountBean o) {
                        try {
                            JfgCmdInsurance.getCmd().login(o.userName, o.pwd);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                        AppLogger.i("LoginAccountBean: " + new Gson().toJson(login));
                        //非三方登录的标记
                        RxBus.getCacheInstance().postSticky(false);
                        return o;
                    }
                })
                .subscribe();
    }

    @Override
    public Subscription registerBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultRegister.class)
                .observeOn(AndroidSchedulers.mainThread())
                .throttleFirst(1000L, TimeUnit.MICROSECONDS)
                .subscribe(new Action1<RxEvent.ResultRegister>() {
                    @Override
                    public void call(RxEvent.ResultRegister register) {
                        //注册成功
                        PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN, "");
                        getView().submitResult(register);
                    }
                });
    }

    private Subscription resultLoginSub() {
        //sdk中，登陆失败的话，自动一分钟登录一次。
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultLogin.class)
                .delay(500, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ResultLogin>() {
                    @Override
                    public void call(RxEvent.ResultLogin resultLogin) {
                        if (getView() != null) {
                            getView().loginResult(resultLogin.code);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("" + throwable);
                    }
                });
    }
}
