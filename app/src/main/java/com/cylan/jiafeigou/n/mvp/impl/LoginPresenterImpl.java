package com.cylan.jiafeigou.n.mvp.impl;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.JfgEnum;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
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
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by lxh on 16-6-24.
 */
public class LoginPresenterImpl extends AbstractPresenter<LoginContract.View>
        implements LoginContract.Presenter {

    private boolean isRegSms;

    public LoginPresenterImpl(LoginContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                resultVerifyCodeSub(),
                switchBoxSub(),
                loginPopBackSub(),
                reShowAccount()
        };
    }

    private Subscription resultVerifyCodeSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultVerifyCode.class)
                .subscribeOn(Schedulers.newThread())
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
                .subscribeOn(Schedulers.newThread())
                .subscribe(o -> {
                    try {
                        countUp(phone);
                        BaseApplication.getAppComponent().getCmd().sendCheckCode(phone, JFGRules.getLanguageType(ContextUtils.getContext()), JfgEnum.SMS_TYPE.JFG_SMS_REGISTER);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    AppLogger.d("phone:" + phone);
                }, throwable -> AppLogger.e("" + throwable.getLocalizedMessage()));
        addSubscription(subscribe);
    }

    @Override
    public void verifyCode(final String phone, final String code, final String token) {
        Subscription subscribe = Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(o -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().verifySMS(phone, code, token);
                        isRegSms = true;
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, throwable -> AppLogger.i("throw:" + throwable.getLocalizedMessage()));
        addSubscription(subscribe);
    }


    @Override
    public void checkAccountIsReg(String account) {
        Subscription subscribe = Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .timeout(30, TimeUnit.SECONDS)
                .delay(1, TimeUnit.SECONDS)
                .map(ret -> {
                    try {
                        return BaseApplication.getAppComponent().getCmd().checkAccountRegState(ret);
                    } catch (JfgException e) {
                        return -1;
                    }
                }).flatMap(result -> RxBus.getCacheInstance()
                        .toObservable(RxEvent.CheckRegisterBack.class).first())
                .filter(ret -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> getView().checkAccountResult(ret), AppLogger::e);
        addSubscription(subscribe);
    }


    /**
     * 登录计时
     */
    @Override
    public void loginCountTime() {
        addSubscription(Observable.just(null)
                .delay(30000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null)
                .subscribe(o -> getView().loginResult(JError.ErrorConnect), AppLogger::e));
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
    public Subscription reShowAccount() {
        return Observable.just(null)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<Object, Observable<String>>() {
                    @Override
                    public Observable<String> call(Object o) {
                        try {
                            String aesAccount = PreferencesUtils.getString(JConstant.AUTO_SIGNIN_KEY);
                            if (TextUtils.isEmpty(aesAccount)) {
                                AppLogger.d("reShowAccount:aes account is null");
                                return Observable.just(null);
                            }
                            String decryption = AESUtil.decrypt(aesAccount);
                            AutoSignIn.SignType signType = new Gson().fromJson(decryption, AutoSignIn.SignType.class);
                            AppLogger.d("Login:" + signType.toString());
                            if (signType.type != 1) {
                                //显示绑定的手机和邮箱
                                String re_show = PreferencesUtils.getString(JConstant.THIRD_RE_SHOW, "");
                                return Observable.just(TextUtils.isEmpty(re_show) ? "" : re_show);
                            } else {
                                return Observable.just(signType.account);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    if (s != null && !TextUtils.isEmpty(s)) {
                        getView().reShowAccount(s);
                    }
                }, AppLogger::e);
    }

    @Override
    public void performLogin(String account, String password) {
        if (mView != null) mView.showLoading();
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
//            AutoSignIn.getInstance().autoSave(account, loginType, token);
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
        Subscription subscribe = Observable.create(subscriber -> {
            try {
                if (loginType >= 3) {//第三方登录
                    BaseApplication.getAppComponent().getCmd().openLogin(JFGRules.getLanguageType(ContextUtils.getContext()), account, password, loginType);
                    RxBus.getCacheInstance().postSticky(new RxEvent.ThirdLoginTab(true));
                } else {//账号密码登录
                    BaseApplication.getAppComponent().getCmd().login(JFGRules.getLanguageType(ContextUtils.getContext()), account, password);
                }
            } catch (JfgException e) {
                e.printStackTrace();
            }
            subscriber.onNext("登录流程开始了...");
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.ResultLogin.class)
                        .timeout(30, TimeUnit.SECONDS, Observable.just(null)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    AppLogger.d("获取到登录结果:" + new Gson().toJson(result));
                    if (result != null && result.code == 0) {
                        AutoSignIn.getInstance().autoSave(account, loginType, password);
                    }
                    if (getView() != null) {
                        getView().loginResult(result == null ? JError.ErrorConnect : result.code);
                    }
                    unSubscribe("failedNetCheckSub");
                }, e -> {
                    AppLogger.e("获取登录结果失败:" + e.getMessage());
                    if (getView() != null) {
                        getView().loginResult(JError.ErrorConnect);
                    }
                   unSubscribe("failedNetCheckSub");
                });
        addSubscription(subscribe);
        Subscription failedNetCheckSub = Observable.just("netCheck")
                .subscribeOn(Schedulers.newThread())
                .delay(2, TimeUnit.SECONDS)
                .map(ret -> {
                    return NetUtils.isNetworkAvailable();
                })
                .filter(ret -> !ret)//网络失败才回调
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> getView() != null)
                .subscribe(ret -> getView().loginResult(JError.ErrorP2PSocket), AppLogger::e);
        addSubscription(failedNetCheckSub, "failedNetCheckSub");
    }

}
