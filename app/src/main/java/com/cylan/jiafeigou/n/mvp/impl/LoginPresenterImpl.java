package com.cylan.jiafeigou.n.mvp.impl;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.JfgEnum;
import com.cylan.entity.jniCall.JFGResult;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.module.LoginHelper;
import com.cylan.jiafeigou.module.User;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.MD5Util;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.google.gson.Gson;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by lxh on 16-6-24.
 */
public class LoginPresenterImpl extends AbstractPresenter<LoginContract.View>
        implements LoginContract.Presenter {

    private boolean isRegSms;

    public LoginPresenterImpl(LoginContract.View view) {
        super(view);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                resultVerifyCodeSub(),
                switchBoxSub(),
                loginPopBackSub()
        };
    }

    private Subscription resultVerifyCodeSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultVerifyCode.class)
                .subscribeOn(Schedulers.io())
                .delay(500, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.ResultVerifyCode resultVerifyCode) -> {
                    if (isRegSms) {
                        getView().verifyCodeResult(resultVerifyCode.code);
                        isRegSms = false;
                    }

                }, (Throwable throwable) -> {
                    AppLogger.e("" + throwable.getLocalizedMessage());
                }, () -> {
                    AppLogger.d("complete?");
                });
    }


    private Subscription switchBoxSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SwitchBox.class)
                .delay(100, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(switchBox -> {
                    if (PreferencesUtils.getBoolean(JConstant.REG_SWITCH_BOX, true)) {
                        getView().switchBox("");
                        PreferencesUtils.putBoolean(JConstant.REG_SWITCH_BOX, false);
                    }
                }, throwable -> AppLogger.e("" + throwable.getLocalizedMessage()));
    }

    private Subscription loginPopBackSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LoginPopBack.class)
                .delay(1000, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loginPopBack -> getView().updateAccount(loginPopBack.account),
                        throwable -> AppLogger.e("" + throwable.getLocalizedMessage()));
    }

    @Override
    public void registerByPhone(String phone, String verificationCode) {
        AppLogger.d("just send phone ");
    }

    @Override
    public void getCodeByPhone(final String phone) {
        Subscription subscribe = Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe(o -> {
                    try {
                        countUp(phone);
                        int ret = Command.getInstance().sendCheckCode(phone, JFGRules.getLanguageType(ContextUtils.getContext()), JfgEnum.SMS_TYPE.JFG_SMS_REGISTER);
                        AppLogger.d("getCodeByPhone?" + ret);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    AppLogger.d("phone:" + phone);
                    Subscription subscription = RxBus.getCacheInstance().toObservable(JFGResult.class)
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe(ret -> {
                                try {
                                    ToastUtil.showToast(mView.getContext().getResources().getString(R.string.GetCode_FrequentlyTips));
                                } catch (Throwable throwable) {
                                }
                            }, AppLogger::e);
                    addSubscription(subscription, "getCodeByPhoneResult");
                }, throwable -> AppLogger.e("" + throwable.getLocalizedMessage()));
        addSubscription(subscribe, "getCodeByPhone");
    }

    @Override
    public void verifyCode(final String phone, final String code, final String token) {
        Subscription subscribe = Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe(o -> {
                    try {
                        Command.getInstance().verifySMS(phone, code, token);
                        isRegSms = true;
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, throwable -> AppLogger.i("throw:" + throwable.getLocalizedMessage()));
        addSubscription(subscribe);
    }


    @Override
    public void checkAccountIsReg(String account) {
//        mView.showLoading();
        Subscription subscribe = Observable.just(account)
                .subscribeOn(Schedulers.io())
                .timeout(30, TimeUnit.SECONDS)
                .delay(500, TimeUnit.MILLISECONDS)
                .map(ret -> {
                    try {
                        return Command.getInstance().checkAccountRegState(ret);
                    } catch (JfgException e) {
                        return -1;
                    }
                }).flatMap(result -> RxBus.getCacheInstance()
                        .toObservable(RxEvent.CheckRegisterBack.class).first())
                .filter(ret -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    getView().checkAccountResult(ret);
                    mView.hideLoading();
                }, throwable -> mView.hideLoading());
        addSubscription(subscribe, "checkAccountIsReg");
    }

    @Override
    public String getTempAccPwd() {
        String path = getView().getContext().getFilesDir().getAbsolutePath();
        Log.d("path", "path: " + path);
        StringBuilder dataFromFile = FileUtils.readFile(path + File.separator + "dat", "UTF-8");
        if (TextUtils.isEmpty(dataFromFile)) {
            return "";
        }
        try {
            return AESUtil.decrypt(dataFromFile.toString());
        } catch (Exception e) {
            return "";
        }
    }

    //计数10分钟3次
    @Override
    public boolean checkOverCount(String account) {
        int count = PreferencesUtils.getInt(account + JConstant.KEY_REG_GET_SMS_COUNT, 0);
        long first_time = PreferencesUtils.getLong(account + JConstant.KEY_REG_FRIST_GET_SMS, 0);
        boolean over10 = System.currentTimeMillis() - first_time > 10 * 60 * 1000;
        boolean result = count <= 3 || over10;
        return !result;
    }

    private void countUp(String account) {
        int count = PreferencesUtils.getInt(account + JConstant.KEY_REG_GET_SMS_COUNT, 0);
        if (count == 0) {
            PreferencesUtils.putLong(account + JConstant.KEY_REG_FRIST_GET_SMS, System.currentTimeMillis());
        }
        long first_time = PreferencesUtils.getLong(account + JConstant.KEY_REG_FRIST_GET_SMS, 0);
        boolean over10 = System.currentTimeMillis() - first_time > 10 * 60 * 1000;
        count = over10 ? 0 : ++count;
        PreferencesUtils.putInt(account + JConstant.KEY_REG_GET_SMS_COUNT, count);
    }

    @Override
    public void reShowAccount() {
        User user = LoginHelper.getUser();
        if (user != null) {
            getView().reShowAccount(user.getDisplay());
        }
    }

    @Override
    public void performLogin(String account, String password) {
//        if (mView != null) mView.showLoading();
        performLoginInternal(1, account, password);
    }

    private int parseLoginType(SHARE_MEDIA share_media) {
        int loginType = 0;
        switch (share_media) {
            case QQ:
                loginType = 3;
                break;
            case SINA:
                loginType = 4;
                break;
            case TWITTER:
                loginType = 6;
                break;
            case FACEBOOK:
                loginType = 7;
                break;
        }
        return loginType;
    }

    private UMAuthListener listener = new UMAuthListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {
            AppLogger.e("授权开始");
            if (mView != null) {
                mView.showLoading();
            }
        }

        @Override
        public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
            AppLogger.e("授权完成,返回码为:" + i + ",返回信息为:" + new Gson().toJson(map));
            PreferencesUtils.putString(JConstant.OPEN_LOGIN_MAP + share_media.toString(), new Gson().toJson(map));
            String account = map.get("uid");
            String token = map.get("accessToken");
            if (TextUtils.isEmpty(token)) {
                token = map.get("access_token");//Twitter 有 bug
            }

            String iconUrl = map.get("iconurl");
            String nickname = map.get("name");
            if (!TextUtils.isEmpty(iconUrl)) {
                PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ICON, iconUrl);
            }
            if (!TextUtils.isEmpty(nickname)) {
                PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ALIAS, nickname);//不需要,服务器会自己获取的
            }
            int loginType = parseLoginType(share_media);
            performLoginInternal(loginType, account, token);
            if (mView != null) {
                mView.onAuthenticationResult(0);
            }
        }

        @Override
        public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
            AppLogger.e("授权失败,错误码为:" + i + ",错误信息为:" + throwable.getMessage());
            if (mView != null) {
                mView.onAuthenticationResult(-1);
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media, int i) {
            AppLogger.e("授权取消,错误码为:" + i);
            if (mView != null) {
                mView.onAuthenticationResult(1);
            }
        }
    };

    @Override
    public void performAuthentication(int loginType) {
        if (mView != null) {
            if (loginType == 3) {
                UMShareAPI.get(mView.getContext()).getPlatformInfo(mView.getActivityContext(), SHARE_MEDIA.QQ, listener);
            } else if (loginType == 4) {
                UMShareAPI.get(mView.getContext()).getPlatformInfo(mView.getActivityContext(), SHARE_MEDIA.SINA, listener);
            } else if (loginType == 6) {
                UMShareAPI.get(mView.getContext()).getPlatformInfo(mView.getActivityContext(), SHARE_MEDIA.TWITTER, listener);
            } else if (loginType == 7) {
                UMShareAPI.get(mView.getContext()).getPlatformInfo(mView.getActivityContext(), SHARE_MEDIA.FACEBOOK, listener);
            }
        }
    }

    private void performLoginInternal(int loginType, String account, String password) {
        Subscription subscription = LoginHelper.performLogin(account, MD5Util.lowerCaseMD5(password), loginType)
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountArrived -> {
                    if (accountArrived != null) {
                        LoginHelper.saveUser(account, password, loginType);
                        getView().onLoginSuccess();
                    } else {
                        getView().onLoginTimeout();
                    }
                }, error -> {
                    error.printStackTrace();
                    AppLogger.e(error);
                    if (error instanceof RxEvent.HelperBreaker) {
                        int breakerCode = ((RxEvent.HelperBreaker) error).breakerCode;
//                        LoginHelper.saveUser(account, "", 1);
                        getView().resetView();
                        switch (breakerCode) {
                            case JError.ErrorAccountNotExist: {
                                getView().onAccountNotExist();
                            }
                            break;
                            case JError.ErrorLoginInvalidPass: {
                                getView().onInvalidPassword();
                            }
                            break;
                            case JError.ErrorOpenLoginInvalidToken: {
                                getView().onOpenLoginInvalidToken();
                            }
                            break;
                            case JError.ErrorConnect: {
                                getView().onConnectError();
                            }
                            break;
                            case JError.ErrorP2PSocket: {
                                getView().onConnectError();
                            }
                            break;
                            default: {
                                getView().onLoginFailed(breakerCode);
                            }
                        }
                    }
                });
        addDestroySubscription(subscription);
    }

}
