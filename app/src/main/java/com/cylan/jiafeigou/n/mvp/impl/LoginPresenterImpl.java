package com.cylan.jiafeigou.n.mvp.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.cylan.entity.JfgEnum;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginContract;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;
import java.io.File;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by lxh on 16-6-24.
 */
public class LoginPresenterImpl extends AbstractPresenter<LoginContract.View>
        implements LoginContract.Presenter {

    private Context ctx;
    private boolean isLoginSucc;
    private boolean isRegSms;
    private boolean isReg;


    public LoginPresenterImpl(LoginContract.View view) {
        super(view);
        view.setPresenter(this);
        ctx = view.getContext();
    }

    /**
     * 登录
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
                            JfgCmdInsurance.getCmd().openLogin(o.userName,o.pwd, o.openLoginType);
                        } else {
                            JfgCmdInsurance.getCmd().login(o.userName, o.pwd);
                            //账号和密码
                        }
                        AutoSignIn.getInstance().autoSave(o.userName, o.openLoginType, o.pwd)
                                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                                .subscribe();
                    } catch (Exception e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                    AppLogger.i("LoginAccountBean: " + new Gson().toJson(o));
                    //非三方登录的标记
                    RxBus.getCacheInstance().postSticky(o.loginType);
                    return null;
                });
    }

    /**
     * 登录结果
     * @return
     */
    private Observable<RxEvent.ResultLogin> loginResultObservable() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultLogin.class);
    }

    @Override
    public void executeLogin(final LoginAccountBean login) {
        //加入
        Observable.zip(loginObservable(login), loginResultObservable(),
                (Object o, RxEvent.ResultLogin resultLogin) -> {
                    Log.d("CYLAN_TAG", "login: " + resultLogin);
                    return resultLogin;
                })
                .timeout(30 * 1000L, TimeUnit.SECONDS, Observable.just(null)
                        .observeOn(AndroidSchedulers.mainThread())
                        .map((Object o) -> {
                            Log.d("CYLAN_TAG", "login timeout: ");
                            if (getView() != null) getView().loginResult(JError.ErrorConnect);
                            return null;
                        }))
                .subscribeOn(Schedulers.io())
                .delay(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.ResultLogin o) -> {
                    Log.d("CYLAN_TAG", "login subscribe: " + o);
                    if (getView() != null) getView().loginResult(o.code);
                }, throwable -> {
                    if (getView() != null) getView().loginResult(JError.ErrorConnect);
                    Log.d("CYLAN_TAG", "login err: " + throwable.getLocalizedMessage());
                });
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                resultVerifyCodeSub(),
                smsCodeResultSub(),
                switchBoxSub(),
                loginPopBackSub(),
                checkAccountBack(),
                thirdAuthorizeBack()
        };
    }

    private Subscription resultVerifyCodeSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultVerifyCode.class)
                .subscribeOn(Schedulers.newThread())
                .delay(1000, TimeUnit.MILLISECONDS)//set a delay
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

    private Subscription smsCodeResultSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SmsCodeResult.class)
                .delay(1000, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.SmsCodeResult smsCodeResult) -> {
                    if (getView().isLoginViewVisible() && JCache.isSmsAction) {
//                            getView().registerResult(smsCodeResult.error);
                        if (smsCodeResult.error == 0) {
                            //store the token .
                            PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN, smsCodeResult.token);
                        }
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e("" + throwable.getLocalizedMessage());
                });
    }

    private Subscription switchBoxSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SwitchBox.class)
                .delay(1000, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(switchBox -> getView().switchBox(""),
                        throwable -> AppLogger.e("" + throwable.getLocalizedMessage()));
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
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(o -> JfgCmdInsurance.getCmd().sendCheckCode(phone,
                        JfgEnum.JFG_SMS_REGISTER),
                        throwable -> AppLogger.e("" + throwable.getLocalizedMessage()));
    }

    @Override
    public void verifyCode(final String phone, final String code, final String token) {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(o -> {
                    try {
                        JfgCmdInsurance.getCmd().verifySMS(phone, code, token);
                        isRegSms = true;
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, throwable -> AppLogger.i("throw:" + throwable.getLocalizedMessage()));
    }


    @Override
    public void checkAccountIsReg(String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {
                            isReg = true;
                            JfgCmdInsurance.getCmd().checkAccountRegState(s);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, throwable -> {
                    AppLogger.e("checkAccountIsReg" + throwable.getLocalizedMessage());
                });
    }

    @Override
    public Subscription checkAccountBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckRegsiterBack.class)
                .delay(5, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.CheckRegsiterBack>() {
                    @Override
                    public void call(RxEvent.CheckRegsiterBack checkRegsiterBack) {
                        if (isReg) {
                            getView().checkAccountResult(checkRegsiterBack);
                            isReg = false;
                        }
                    }
                });
    }

    /**
     * 登录计时
     */
    @Override
    public void loginCountTime() {
        rx.Observable.just(null)
                .delay(30000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    if (getView() != null && !isLoginSucc)
                        getView().loginResult(JError.ErrorConnect);
                });
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
    public boolean checkOverCount() {
        int count = PreferencesUtils.getInt(JConstant.KEY_REG_GET_SMS_COUNT, 0);
        long first_time = PreferencesUtils.getLong(JConstant.KEY_REG_FRIST_GET_SMS, 0);

        if (count == 0) {
            PreferencesUtils.putLong(JConstant.KEY_REG_FRIST_GET_SMS, System.currentTimeMillis());
            PreferencesUtils.putInt(JConstant.KEY_REG_GET_SMS_COUNT, count + 1);
            return false;
        }

        if (count < 3) {
            if (System.currentTimeMillis() - first_time < 10 * 60 * 1000) {
                PreferencesUtils.putInt(JConstant.KEY_REG_GET_SMS_COUNT, count + 1);
            } else {
                PreferencesUtils.putInt(JConstant.KEY_REG_GET_SMS_COUNT, 0);
                PreferencesUtils.putLong(JConstant.KEY_REG_FRIST_GET_SMS, System.currentTimeMillis());
            }
            return false;
        } else {
            if (System.currentTimeMillis() - first_time < 10 * 60 * 1000) {
                return true;
            } else {
                PreferencesUtils.putInt(JConstant.KEY_REG_GET_SMS_COUNT, 0);
                PreferencesUtils.putLong(JConstant.KEY_REG_FRIST_GET_SMS, System.currentTimeMillis());
                return false;
            }
        }
    }

    @Override
    public Subscription thirdAuthorizeBack() {
        return RxBus.getCacheInstance().toObservableSticky(LoginAccountBean.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loginAccountBean -> {
                    if (loginAccountBean != null){
                        executeLogin(loginAccountBean);
                    }else {
                        getView().authorizeResult();
                    }
                });
    }

}
