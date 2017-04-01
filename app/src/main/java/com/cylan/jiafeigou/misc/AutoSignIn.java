package com.cylan.jiafeigou.misc;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.sina.AccessTokenKeeper;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

import java.io.File;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 17-3-6.
 */

public class AutoSignIn {

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

    public static final String KEY = "fxxx";


    public Observable<Integer> autoLogin() {
        return Observable.just("run")
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(String s) {
                        try {
                            String aesAccount = PreferencesUtils.getString(JConstant.AUTO_SIGNIN_KEY);
                            if (aesAccount != null)
                                DataSourceManager.getInstance().initAccount();
                            AppLogger.d("autoLogin");
                            if (TextUtils.isEmpty(aesAccount)) {
                                AppLogger.d("account is null");
                                return Observable.just(-1);
                            }
                            String decryption = AESUtil.decrypt(aesAccount);
                            SignType signType = new Gson().fromJson(decryption, SignType.class);
                            if (signType != null) {
                                StringBuilder pwd = FileUtils.readFile(ContextUtils.getContext().getFilesDir() + File.separator + aesAccount + ".dat", "UTF-8");
                                AppLogger.d("log pwd: "+pwd);
                                if (!TextUtils.isEmpty(pwd)) {
                                    String finalPwd = AESUtil.decrypt(pwd.toString());
                                    if (signType.type == 1) {
                                        RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(-1));
                                        JfgCmdInsurance.getCmd().login(JFGRules.getLanguageType(ContextUtils.getContext()), signType.account, finalPwd);
                                        RxBus.getCacheInstance().postSticky(new RxEvent.ThirdLoginTab(false));
                                    } else if (signType.type >= 3) {
                                        //效验本地token是否过期
                                        if (checkTokenOut(signType.type)) {
                                            AppLogger.d("isout:ee");
                                            autoSave(signType.account, 1, "");
                                            return Observable.just(-1);
                                        } else {
                                            AppLogger.d("isout:no");
                                            RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(-1));
                                            JfgCmdInsurance.getCmd().openLogin(JFGRules.getLanguageType(ContextUtils.getContext()),signType.account,finalPwd,signType.type);
                                            RxBus.getCacheInstance().postSticky(new RxEvent.ThirdLoginTab(true));
                                        }
                                    }
                                    AppLogger.d("log type: " + signType);
                                    return Observable.just(0);
                                } else {
                                    return Observable.just(-1);
                                }
                            }
                            AppLogger.d("signType is :" + signType);
                            return Observable.just(-1);
                        } catch (Exception e) {
                            AppLogger.e("no sign type");
                            return Observable.just(-1);
                        }
                    }
                });
    }

    private boolean checkTokenOut(int type) {
        boolean isOut = false;
        switch (type) {
            case 3:
//                isOut = !TencentInstance.getInstance((Activity) ContextUtils.getContext()).mTencent.isSessionValid();
//                AppLogger.d("isout:" + isOut);
                break;
            case 4:
                Oauth2AccessToken oauth2AccessToken = AccessTokenKeeper.readAccessToken(ContextUtils.getContext());
                isOut = !(oauth2AccessToken != null && oauth2AccessToken.isSessionValid());
                break;
            case 6:
//                TwitterSession activeSession = Twitter.getSessionManager().getActiveSession();
//                TwitterAuthToken authToken = activeSession.getAuthToken();
//                if (authToken != null)
//                    isOut = !authToken.isExpired();
                break;
            case 7:
//                isOut = !AccessToken.getCurrentAccessToken().isExpired();
                break;
        }
        return isOut;
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
                        return 0;
                    } catch (Exception e) {
                        AppLogger.e("e:" + e.getLocalizedMessage());
                        return -1;
                    }
                });
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
