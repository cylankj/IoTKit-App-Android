package com.cylan.jiafeigou.n.mvp.impl;

import android.util.Log;

import com.cylan.entity.JfgEnum;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JResultEvent;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.login.ForgetPwdContract;
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
 * Created by cylan-hunt on 16-6-29.
 */
public class ForgetPwdPresenterImpl extends AbstractPresenter<ForgetPwdContract.View>
        implements ForgetPwdContract.Presenter {

    public static String account;

    public ForgetPwdPresenterImpl(ForgetPwdContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    //计数10分钟3次
    @Override
    public boolean checkOverCount(String account) {
        int count = PreferencesUtils.getInt(account + JConstant.KEY_FORGET_PWD_GET_SMS_COUNT, 0);
        long first_time = PreferencesUtils.getLong(account + JConstant.KEY_FORGET_PWD_FRIST_GET_SMS, 0);

        if (count == 0) {
            PreferencesUtils.putLong(account + JConstant.KEY_FORGET_PWD_FRIST_GET_SMS, System.currentTimeMillis());
            PreferencesUtils.putInt(account + JConstant.KEY_FORGET_PWD_GET_SMS_COUNT, count + 1);
            return false;
        }

        if (count < 3) {
            if (System.currentTimeMillis() - first_time < 10 * 60 * 1000) {
                PreferencesUtils.putInt(account + JConstant.KEY_FORGET_PWD_GET_SMS_COUNT, count + 1);
            } else {
                PreferencesUtils.putInt(account + JConstant.KEY_FORGET_PWD_GET_SMS_COUNT, 0);
                PreferencesUtils.putLong(account + JConstant.KEY_FORGET_PWD_FRIST_GET_SMS, System.currentTimeMillis());
            }
            return false;
        } else {
            if (System.currentTimeMillis() - first_time < 10 * 60 * 1000) {
                return true;
            } else {
                PreferencesUtils.putInt(account + JConstant.KEY_FORGET_PWD_GET_SMS_COUNT, 0);
                PreferencesUtils.putLong(account + JConstant.KEY_FORGET_PWD_FRIST_GET_SMS, System.currentTimeMillis());
                return false;
            }
        }
    }


    /**
     * 重置密码
     *
     * @param newPassword
     */
    public void submitNewPass(String newPassword) {
        Subscription sub = rx.Observable.just(newPassword)
                .subscribeOn(Schedulers.newThread())
                .subscribe(s -> {
                    Subscription subscription = RxBus.getCacheInstance().toObservable(RxEvent.ResetPwdBack.class)
                            .first()
                            .timeout(10, TimeUnit.SECONDS)
                            .filter(ret -> mView != null)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(back -> {
                                mView.onResult(JResultEvent.JFG_RESULT_CHANGE_PASS, back.jfgResult.code);
                                RxBus.getCacheInstance().post(new RxEvent.LoginPopBack(account));
                                unSubscribe("resetPwdResult");
                            }, AppLogger::e);
                    addSubscription(subscription, "resetPwdResult");
                    try {
                        BaseApplication.getAppComponent()
                                .getCmd().resetPassword(account, s,
                                PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN));
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
        addSubscription(sub, "submitNewPass");
    }

    @Override
    public void getVerifyCode(String phone) {
        unSubscribeAllTag();
        //获取验证码,1.校验手机号码,2.根据错误号显示
        account = phone;
        Subscription subscription = rx.Observable.just(phone)
                .subscribeOn(Schedulers.newThread())
                .delay(1, TimeUnit.SECONDS)
                .flatMap(s -> {
                    try {
                        long req = BaseApplication.getAppComponent().getCmd().checkAccountRegState(s);
                        Log.d(TAG, "校验手机号码: " + req);
                        return Observable.just(req);
                    } catch (JfgException e) {
                        return Observable.just(-1);
                    }
                })
                .flatMap(number -> RxBus.getCacheInstance().toObservable(RxEvent.CheckRegisterBack.class)
                        .subscribeOn(Schedulers.newThread())
                        .timeout(10, TimeUnit.SECONDS)
                        .delay(100, TimeUnit.MILLISECONDS))
                .flatMap(ret -> {
                    //手机号注册 情况
                    if (ret.jfgResult.code == JError.ErrorOK) {
                        Subscription s = RxBus.getCacheInstance()
                                .toObservable(RxEvent.SmsCodeResult.class)
                                .timeout(10, TimeUnit.SECONDS)
                                .filter(r -> mView != null)
                                .subscribe(result -> {
                                    mView.onResult(JConstant.GET_SMS_BACK, result.error);
                                    unSubscribe("ResultVerifyCode");
                                    unSubscribe("getVerifyCode");
                                }, AppLogger::e);
                        addSubscription(s, "ResultVerifyCode");
                        try {
                            //获取验证码
                            int seq = BaseApplication.getAppComponent().getCmd()
                                    .sendCheckCode(phone, JFGRules.getLanguageType(ContextUtils.getContext()), JfgEnum.SMS_TYPE.JFG_SMS_FORGOTPASS);
                            if (seq != 0) s.unsubscribe();
                            else AppLogger.d("手机号码 有效,开始获取验证码");
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //返回错误码
                        mView.onResult(ret.jfgResult.event, ret.jfgResult.code);
                        unSubscribeAllTag();
                    }
                    return Observable.just(ret.jfgResult.code);
                })
                .doOnError(throwable -> mView.onResult(JConstant.CHECK_TIMEOUT, 0))
                .subscribe(ret -> {
                }, AppLogger::e);
        addSubscription(subscription, "getVerifyCode");
    }

    @Override
    public void submitPhoneAndCode(String phone, String code) {
        unSubscribeAllTag();
        //1.验证手机号码
        account = phone;
        Observable.just(phone)
                .subscribeOn(Schedulers.io())
                .flatMap(s -> {
                    try {
                        long req = BaseApplication.getAppComponent().getCmd()
                                .checkAccountRegState(s);
                        Log.d(TAG, "校验手机号码: " + req);
                        return Observable.just(req);
                    } catch (JfgException e) {
                        return Observable.just(-1);
                    }
                })
                .flatMap(number -> RxBus.getCacheInstance().toObservable(RxEvent.CheckRegisterBack.class)
                        .subscribeOn(Schedulers.newThread())
                        .delay(100, TimeUnit.MILLISECONDS))
                //有效的手机号
                .filter(ret -> ret.jfgResult.code == JError.ErrorOK)
                .flatMap(s -> {
                    Subscription codeResultSub = RxBus.getCacheInstance().toObservable(RxEvent.ResultVerifyCode.class)
                            .first()
                            .timeout(10, TimeUnit.SECONDS)
                            .filter(ret -> mView != null)
                            .doOnError(throwable -> mView.onResult(JConstant.CHECK_TIMEOUT, 0))
                            .subscribe(ret -> mView.onResult(JConstant.AUTHORIZE_PHONE, ret.result.code), AppLogger::e);
                    addSubscription(codeResultSub, "codeResultSub");
                    try {
                        String token = PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN, "");
                        BaseApplication.getAppComponent().getCmd().verifySMS(phone, code, token);
                        AppLogger.d("验证 短信:" + token);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .timeout(10, TimeUnit.SECONDS)
                .subscribe(ret -> {
                }, AppLogger::e);

    }

    @Override
    public void checkMailByAccount(String mail) {
        //获取验证码,1.校验邮箱,2.根据错误号显示
        unSubscribeAllTag();
        account = mail;
        Subscription subscription = rx.Observable.just(mail)
                .subscribeOn(Schedulers.newThread())
                .delay(1, TimeUnit.SECONDS)
                .flatMap(s -> {
                    try {
                        long req = BaseApplication.getAppComponent()
                                .getCmd().checkAccountRegState(s);
                        Log.d(TAG, "校验邮箱: " + req);
                        return Observable.just(req);
                    } catch (JfgException e) {
                        return Observable.just(-1);
                    }
                })
                .flatMap(number -> RxBus.getCacheInstance().toObservable(RxEvent.CheckRegisterBack.class)
                        .subscribeOn(Schedulers.newThread())
                        .delay(100, TimeUnit.MILLISECONDS))
                .flatMap(ret -> {
                    if (ret.jfgResult.code == JError.ErrorOK) {
                        Subscription s = RxBus.getCacheInstance()
                                .toObservable(RxEvent.ForgetPwdByMail.class)
                                .timeout(10, TimeUnit.SECONDS)
                                .subscribe(code -> {
                                    getView().onResult(JConstant.AUTHORIZE_MAIL, code.ret);
                                    unSubscribe("MailChecker");
                                }, AppLogger::e);
                        addSubscription(s, "MailChecker");
                        try {
                            int seq = BaseApplication.getAppComponent().getCmd()
                                    .forgetPassByEmail(JFGRules.getLanguageType(ContextUtils.getContext()), mail);
                            if (seq != 0) s.unsubscribe();
                            AppLogger.d("邮箱 忘记密码 ");
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //返回错误码
                        mView.onResult(ret.jfgResult.event, ret.jfgResult.code);
                    }
                    return Observable.just(ret.jfgResult.code);
                })
                .subscribe();
        addSubscription(subscription, "checkMailByAccount");
    }

}
