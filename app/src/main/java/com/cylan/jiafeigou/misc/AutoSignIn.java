package com.cylan.jiafeigou.misc;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.cylan.jiafeigou.utils.MD5Util;
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
    private String loginAccount = null;
    private String loginpwd = null;

    public static AutoSignIn getInstance() {
        if (instance == null)
            synchronized (AutoSignIn.class) {
                if (instance == null)
                    instance = new AutoSignIn();
            }
        return instance;
    }

    public boolean isNotEmpty() {
        return !TextUtils.isEmpty(loginAccount) && !TextUtils.isEmpty(loginpwd);
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


    public void autoLogin() {
        RxBus.getCacheInstance().removeAllStickyEvents();
        AppLogger.e("此处使用removeAll,可能引发潜在的bug");
        Observable.just(PreferencesUtils.getString(JConstant.AUTO_SIGNIN_KEY))
                .subscribeOn(Schedulers.io())
                .flatMap(signTypeAes -> {
                    try {
                        AppLogger.e("autoLogin");
                        String finalAccount = null;
                        String finalPwd = null;
                        SignType signType = null;
                        if (TextUtils.isEmpty(signTypeAes) && PreferencesUtils.getBoolean(JConstant.KEY_FRESH, true)) {
                            finalAccount = getString(JConstant.KEY_PHONE);
                            //2.x的密码是MD5保存
                            finalPwd = getString(JConstant.KEY_PSW);
                            Log.d(TAG, "get info from 2.x? " + finalAccount);
                            Log.d(TAG, "get info from 2.x? " + finalPwd);
//                                    clear_2x();
                            signType = new SignType();
                            signType.account = finalAccount;
                            signType.type = 0;//此处不能填1,会自动登录.0 不去登录,走欢迎页的逻辑.
                        } else if (!TextUtils.isEmpty(signTypeAes)) {
                            String decryption = AESUtil.decrypt(signTypeAes);
                            signType = new Gson().fromJson(decryption, SignType.class);
                            if (signType != null) {
                                finalAccount = signType.account;
                                if (signType.type == 0)//=0 可能是上一步,从2.x读配置文件.
                                    signType.type = 1;
                                finalPwd = FileUtils.readFile(ContextUtils.getContext().getFilesDir() + File.separator + signTypeAes + ".dat", "UTF-8").toString();
                                Log.d(TAG, "read account from file: " + finalAccount);
                                Log.d(TAG, "read pwd md5 from file: " + finalPwd);
                                if (TextUtils.isEmpty(finalAccount) || TextUtils.isEmpty(finalPwd)) {
                                    throw new IllegalArgumentException("用户名或者密码不能为空");
                                }
                                PreferencesUtils.putBoolean(JConstant.AUTO_lOGIN_PWD_ERR, false);
                            }
                        }
                        String aes = saveAccountAes(signType);
                        savePwdMd5(aes, finalPwd);
                        if (signType != null) {
                            if (!TextUtils.isEmpty(finalPwd) && !TextUtils.isEmpty(finalAccount)) {
                                //需要告知
                                loginAccount = finalAccount;
                                loginpwd = finalPwd;
                                RxBus.getCacheInstance().postSticky(RxEvent.InitFrom2x.INSTANCE);
                            }
                            if (signType.type == 1) {
                                clear_2x();
                                BaseApplication.getAppComponent().getCmd().login(JFGRules.getLanguageType(ContextUtils.getContext()), finalAccount, finalPwd);
                            } else if (signType.type >= 3) {
                                clear_2x();
                                BaseApplication.getAppComponent().getCmd().openLogin(JFGRules.getLanguageType(ContextUtils.getContext()), finalAccount, finalPwd, signType.type);
                            } else if (signType.type == 0) {
                                PreferencesUtils.putBoolean(JConstant.AUTO_lOGIN_PWD_ERR, true);
                                RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(JError.ErrorLoginInvalidPass));
                            }
                            Log.d(TAG, "finalPwd:" + finalPwd);
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
                })
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

    public SignType getSignType() {
        try {
            //1.account的aes
            String string = PreferencesUtils.getString(JConstant.AUTO_SIGNIN_KEY);
            String decrypt = AESUtil.decrypt(string);
            return new Gson().fromJson(decrypt, SignType.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private void savePwdMd5(String accountAes, String pwd) {
        try {
            //2.保存密码,md5
            FileUtils.deleteAbsoluteFile(ContextUtils.getContext().getFilesDir() + File.separator + accountAes + ".dat");
            FileUtils.writeFile(ContextUtils.getContext().getFilesDir() + File.separator + accountAes + ".dat", pwd);
            if (TextUtils.isEmpty(pwd)) {
                PreferencesUtils.putBoolean(JConstant.AUTO_lOGIN_PWD_ERR, true);
            }
        } catch (Exception e) {
        }
    }

    private String saveAccountAes(SignType signType) {
        try {
            //1.account的aes
            String aes = AESUtil.encrypt(new Gson().toJson(signType));
            PreferencesUtils.putString(JConstant.AUTO_SIGNIN_KEY, aes);
            Log.d(TAG, "signType aes: " + aes);
            return aes;
        } catch (Exception e) {
            AppLogger.e("e:" + e.getLocalizedMessage());
            return "";
        }
    }

    public void autoSave(String account, int type, String pwd) {
        try {
            SignType signType = new SignType();
            signType.account = account;
            signType.type = type;
            String aes = saveAccountAes(signType);
            savePwdMd5(aes, TextUtils.isEmpty(pwd) ? "" :
                    pwd.length() == 32 ? pwd : MD5Util.lowerCaseMD5(pwd));
        } catch (Exception e) {
            AppLogger.e("e:" + e.getLocalizedMessage());
        }
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

    /**
     * 兼容2.x版本
     *
     * @param key
     * @return
     */
    private String getString(String key) {
        return ContextUtils.getContext().getSharedPreferences("config_pref", Context.MODE_PRIVATE).getString(key, "");
    }

    private void clear_2x() {
        SharedPreferences settings = ContextUtils.getContext().getSharedPreferences("config_pref", Context.MODE_PRIVATE);
        settings.edit().clear().apply();
    }
}
