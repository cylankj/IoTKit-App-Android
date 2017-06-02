package com.cylan.jiafeigou.misc;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.File;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 17-3-6.
 */

public class AutoSignIn {
    public boolean finish = false;
    private JFGAccount jfgAccount;
    private static final String TAG = "AutoSignIn";
    private static AutoSignIn instance;

    public static AutoSignIn getInstance() {
        if (instance == null)
            synchronized (AutoSignIn.class) {
                if (instance == null)
                    instance = new AutoSignIn();
            }
        return instance;
    }

    public void setJfgAccount(JFGAccount jfgAccount) {
        this.jfgAccount = jfgAccount;
    }

    public JFGAccount getJfgAccount() {
        return jfgAccount;
    }

    private AutoSignIn() {
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public boolean isFinish() {
        return finish;
    }

    public static final String KEY = "fxxx";


    public void autoLogin() {
        RxBus.getCacheInstance().removeAllStickyEvents();
        AppLogger.e("此处使用removeAll,可能引发潜在的bug");
        Observable.just(PreferencesUtils.getString(JConstant.AUTO_SIGNIN_KEY))
                .subscribeOn(Schedulers.io())
                .flatMap(account -> {
                            try {
                                final String netName = NetUtils.getNetName(ContextUtils.getContext());
                                if (netName != null && netName.contains("DOG"))
                                    MiscUtils.recoveryWiFi();
                                AppLogger.d("autoLogin");
                                String account2x = PreferencesUtils.getString(JConstant.KEY_PHONE, "");
                                String pwd2x = PreferencesUtils.getString(JConstant.SESSIONID, "");
                                PreferencesUtils.putString(JConstant.KEY_PHONE, "");
                                PreferencesUtils.putString(JConstant.SESSIONID, "");
                                String decryption = AESUtil.decrypt(account);
                                SignType signType = new Gson().fromJson(decryption, SignType.class);
                                if (signType != null) {
                                    String finalPwd = TextUtils.isEmpty(account2x) ? AESUtil.decrypt("" + FileUtils.readFile(ContextUtils.getContext().getFilesDir() + File.separator + account + ".dat", "UTF-8")) : pwd2x;
                                    String finalAccount = TextUtils.isEmpty(account2x) ? signType.account : account2x;
                                    if (TextUtils.isEmpty(finalAccount) || TextUtils.isEmpty(finalPwd)) {
                                        throw new IllegalArgumentException("用户名或者密码不能为空");
                                    }
                                    if (signType.type == 1) {
                                        RxBus.getCacheInstance().postSticky(new RxEvent.ThirdLoginTab(false));
                                        BaseApplication.getAppComponent().getCmd().login(JFGRules.getLanguageType(ContextUtils.getContext()), finalAccount, finalPwd);
                                    } else if (signType.type >= 3) {
                                        RxBus.getCacheInstance().postSticky(new RxEvent.ThirdLoginTab(true));
                                        BaseApplication.getAppComponent().getCmd().openLogin(JFGRules.getLanguageType(ContextUtils.getContext()), finalAccount, finalPwd, signType.type);
                                    }

                                    PreferencesUtils.putBoolean(JConstant.AUTO_lOGIN_PWD_ERR, false);
                                    autoSave(finalAccount, signType.type, finalPwd);
                                }

                            } catch (Exception e) {
                                AppLogger.e("no sign type" + e.getLocalizedMessage());
                                PreferencesUtils.putBoolean(JConstant.AUTO_lOGIN_PWD_ERR, true);
                                RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(JError.ErrorLoginInvalidPass));
                            }

                            if (!BaseApplication.isOnline() && !PreferencesUtils.getBoolean(JConstant.AUTO_lOGIN_PWD_ERR, true)) {//当前无法联网,则直指返回
                                BaseApplication.getAppComponent().getSourceManager().initFromDB();
                                RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(JError.ERROR_OFFLINE_LOGIN));
                            }
                            return RxBus.getCacheInstance().toObservableSticky(RxEvent.ResultLogin.class).first();
                        }
                )
                .observeOn(Schedulers.io())
                .timeout(5, TimeUnit.SECONDS, Observable.just(PreferencesUtils.getBoolean(JConstant.AUTO_lOGIN_PWD_ERR, true))
                        .map(hasPswError -> {
                            if (hasPswError) {
                                return new RxEvent.ResultLogin(JError.ErrorLoginInvalidPass);
                            } else {
                                BaseApplication.getAppComponent().getSourceManager().initFromDB();
                                return new RxEvent.ResultLogin(JError.ERROR_OFFLINE_LOGIN);
                            }
                        })
                )
                .subscribe(resultLogin -> RxBus.getCacheInstance().post(resultLogin), AppLogger::e);
    }

    public Observable<Integer> autoSave(String account, int type, String pwd) {
        return Observable.just("save")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    try {
                        SignType signType = new SignType();
                        signType.account = account;
                        signType.type = type;
                        //1.account的aes
                        String aes = AESUtil.encrypt(new Gson().toJson(signType));
                        PreferencesUtils.putString(JConstant.AUTO_SIGNIN_KEY, aes);
                        Log.d(TAG, "account aes: " + aes.length());
                        //2.保存密码
                        FileUtils.writeFile(ContextUtils.getContext().getFilesDir() + File.separator + aes + ".dat", AESUtil.encrypt(pwd));
                        if (TextUtils.isEmpty(pwd)) {
                            PreferencesUtils.putBoolean(JConstant.AUTO_lOGIN_PWD_ERR, true);
                        }
                        return 0;
                    } catch (Exception e) {
                        AppLogger.e("e:" + e.getLocalizedMessage());
                        return -1;
                    }
                });
    }

    public void clearPsw() {
        FileUtils.writeFile(ContextUtils.getContext().getFilesDir() + File.separator + PreferencesUtils.getString(JConstant.AUTO_SIGNIN_KEY) + ".dat", "");
        PreferencesUtils.putBoolean(JConstant.AUTO_lOGIN_PWD_ERR, true);
        PreferencesUtils.putString(JConstant.SESSIONID, "");
    }

    public static class SignType {
        public String account;
        public int type;

        @Override
        public String toString() {
            return "SignType{" +
                    "account='" + account + '\'' +
                    ", type=" + type +
                    '}';
        }
    }
}
