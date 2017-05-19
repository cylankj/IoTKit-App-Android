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
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.File;
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

    /**
     * 登录
     *
     * @param o
     * @return
     */
    private Observable<Object> loginObservable(LoginAccountBean o) {
        return Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(login -> {
                    Log.d("CYLAN_TAG", "map executeLogin next");
                    try {
                        if (o.loginType) {
                            BaseApplication.getAppComponent().getCmd().openLogin(JFGRules.getLanguageType(ContextUtils.getContext()), o.userName, o.pwd, o.openLoginType);
                            AppLogger.d("第三方登录:" + o.userName + ":" + o.pwd);
                        } else {
                            BaseApplication.getAppComponent().getCmd().login(JFGRules.getLanguageType(ContextUtils.getContext()), o.userName, o.pwd);
                            //账号和密码
                        }
                        AutoSignIn.getInstance().autoSave(o.userName, o.openLoginType, o.pwd)
                                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                                .subscribe(ret -> {
                                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
                        AppLogger.d("logresult:" + o.toString());
                    } catch (Exception e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
//                    AppLogger.i("LoginAccountBean: " + new Gson().toJson(o));
                    //非三方登录的标记
                    RxBus.getCacheInstance().postSticky(new RxEvent.ThirdLoginTab(o.loginType));
                    return null;
                });
    }

    /**
     * 登录结果
     *
     * @return
     */
    private Observable<RxEvent.ResultUserLogin> loginResultObservable() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultUserLogin.class);
    }

    @Override
    public void executeLogin(final LoginAccountBean login) {
        //加入
        Observable.zip(loginObservable(login), loginResultObservable(),
                (Object o, RxEvent.ResultUserLogin resultLogin) -> {
                    Log.d("CYLAN_TAG", "login: " + resultLogin);
                    return resultLogin;
                })
                .timeout(30, TimeUnit.SECONDS, Observable.just(null)
                        .observeOn(AndroidSchedulers.mainThread())
                        .map((Object o) -> {
                            Log.d("CYLAN_TAG", "login timeout: ");
                            if (getView() != null) getView().loginResult(JError.ErrorConnect);
                            return null;
                        }))
                .subscribeOn(Schedulers.io())
                .delay(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.ResultUserLogin o) -> {
                    Log.d("CYLAN_TAG", "login subscribe: " + o);
                    if (getView() != null) getView().loginResult(o.code);
                }, throwable -> {
                    try {
                        if (getView() != null) getView().loginResult(JError.ErrorConnect);
                        Log.d("CYLAN_TAG", "login err: " + throwable.getLocalizedMessage());
                    } catch (Exception e) {

                    }
                });
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                resultVerifyCodeSub(),
                switchBoxSub(),
                loginPopBackSub(),
                thirdAuthorizeBack(),
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
    public Subscription thirdAuthorizeBack() {
        return RxBus.getCacheInstance().toObservableSticky(LoginAccountBean.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loginAccountBean -> {
                    if (loginAccountBean != null && !TextUtils.isEmpty(loginAccountBean.userName)) {
                        executeLogin(loginAccountBean);
                        if (getView() != null) getView().showLoading();
                    } else {
                        getView().authorizeResult();
                    }
                }, AppLogger::e);
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

}
