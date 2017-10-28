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

/**
 * Created by cylan-hunt on 16-6-29.
 */
public class ForgetPwdPresenterImpl extends AbstractPresenter<ForgetPwdContract.View>
        implements ForgetPwdContract.Presenter {

    public static String account;

    public ForgetPwdPresenterImpl(ForgetPwdContract.View view) {
        super(view);
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
    @Override
    public void submitNewPass(String newPassword) {
        Subscription subscribe = Observable.just("submitNewPass")
                .subscribeOn(AndroidSchedulers.mainThread())
                .delay(200, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().resetPassword(account, newPassword, PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN));
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return cmd;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.ResetPwdBack.class).first())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoading())
                .doOnTerminate(() -> getView().hideLoading())
                .subscribe(result -> {
                    mView.onResult(JResultEvent.JFG_RESULT_CHANGE_PASS, result == null ? -1 : result.jfgResult.code);
                    RxBus.getCacheInstance().post(new RxEvent.LoginPopBack(account));
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }

    @Override
    public void getVerifyCode(String phone) {
        //获取验证码,1.校验手机号码,2.根据错误号显示
        account = phone;
        Subscription subscription = rx.Observable.just("getVerifyCode")
                .subscribeOn(AndroidSchedulers.mainThread())
                .delay(200, TimeUnit.MILLISECONDS)
                .map(cmd -> {
                    try {
                        long req = BaseApplication.getAppComponent().getCmd().checkAccountRegState(phone);
                        Log.d(TAG, "校验手机号码: " + req);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(e.getMessage());
                    }
                    return cmd;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.CheckRegisterBack.class).first())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(result -> {
                    if (result.jfgResult.code != JError.ErrorOK) {
                        mView.onResult(result.jfgResult.event, result.jfgResult.code);
                    }
                    return result.jfgResult.code == JError.ErrorOK;
                })
                .observeOn(Schedulers.io())
                .map(ret -> {
                    try {
                        //获取验证码
                        BaseApplication.getAppComponent().getCmd().sendCheckCode(phone, JFGRules.getLanguageType(ContextUtils.getContext()), JfgEnum.SMS_TYPE.JFG_SMS_FORGOTPASS);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(e.getMessage());
                    }
                    return ret;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.SmsCodeResult.class).first())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoading())
                .doOnTerminate(() -> getView().hideLoading())
                .subscribe(result -> {
                    mView.onResult(JConstant.GET_SMS_BACK, result == null ? -1 : result.error);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                    mView.onResult(JConstant.CHECK_TIMEOUT, 0);
                });
        addSubscription(subscription);
    }

    @Override
    public void submitPhoneAndCode(String phone, String code) {
//        unSubscribeAllTag();
        //1.验证手机号码
        account = phone;
        Subscription subscribe = Observable.just("submitPhoneAndCode")
                .subscribeOn(AndroidSchedulers.mainThread())
                .delay(200, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        long req = BaseApplication.getAppComponent().getCmd()
                                .checkAccountRegState(phone);
                        AppLogger.d("校验手机号码: " + req);
                        return Observable.just(req);
                    } catch (JfgException e) {
                        AppLogger.e(e.getMessage());
                        e.printStackTrace();
                    }
                    return cmd;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.CheckRegisterBack.class).first())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> {
                    if (ret.jfgResult.code != JError.ErrorOK) {
                        mView.onResult(ret.jfgResult.event, ret.jfgResult.code);
                    }
                    AppLogger.d("检查用户注册状态:" + ret.jfgResult.code);
                    return ret.jfgResult.code == JError.ErrorOK;
                })
                .observeOn(Schedulers.io())
                .map(ret -> {
                    try {
                        String token = PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN, "");
                        BaseApplication.getAppComponent().getCmd().verifySMS(phone, code, token);
                        AppLogger.d("验证 短信:" + token);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return ret;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.ResultVerifyCode.class).first())
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoading())
                .doOnTerminate(() -> getView().hideLoading())
                .subscribe(ret -> {
                    if (ret == null) {
                        //超时了
                        // TODO: 2017/7/17 超时处理
                        mView.onResult(JConstant.CHECK_TIMEOUT, 0);
                        AppLogger.d("检测验证码结果超时了!");
                    } else {
                        mView.onResult(JConstant.AUTHORIZE_PHONE_SMS, ret.result.code);
//                        mView.onResult(JConstant.AUTHORIZE_PHONE_SMS, JError.ErrorSMSCodeTimeout);//just for test
                        AppLogger.d("检测验证码结果为:" + ret.result.code);
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                    mView.onResult(JConstant.AUTHORIZE_PHONE_SMS, -1);
                });
        addSubscription(subscribe);
    }

    @Override
    public void checkMailByAccount(String mail) {
        //获取验证码,1.校验邮箱,2.根据错误号显示
//        unSubscribeAllTag();
        account = mail;

        Subscription subscribe = Observable.just("checkMailByAccount")
                .subscribeOn(AndroidSchedulers.mainThread())
                .delay(200, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        long req = BaseApplication.getAppComponent().getCmd().checkAccountRegState(mail);
                        AppLogger.d("校验邮箱: " + req);
                    } catch (JfgException e) {
                    }
                    return cmd;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.CheckRegisterBack.class).first())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> {
                    if (ret.jfgResult.code != JError.ErrorOK) {
                        // TODO: 2017/7/17 错误处理
                        //返回错误码
                        mView.onResult(ret.jfgResult.event, ret.jfgResult.code);
                    }

                    return ret.jfgResult.code == JError.ErrorOK;
                })
                .observeOn(Schedulers.io())
                .map(ret -> {
                    try {
                        int seq = BaseApplication.getAppComponent().getCmd().forgetPassByEmail(JFGRules.getLanguageType(ContextUtils.getContext()), mail);
                        AppLogger.d("邮箱 忘记密码 :" + seq);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.d(e.getMessage());
                    }
                    return ret;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.ForgetPwdByMail.class).first())
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoading())
                .doOnTerminate(() -> getView().hideLoading())
                .subscribe(ret -> {
                    if (ret != null) {
                        getView().onResult(JConstant.AUTHORIZE_MAIL, ret.ret);
                    } else {
                        // TODO: 2017/7/17  超时了
                    }
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }

}
