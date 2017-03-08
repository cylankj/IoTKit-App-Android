package com.cylan.jiafeigou.misc;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.support.facebook.FacebookInstance;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.qqLogIn.TencentInstance;
import com.cylan.jiafeigou.support.sina.AccessTokenKeeper;
import com.cylan.jiafeigou.support.sina.SinaLogin;
import com.cylan.jiafeigou.support.twitter.TwitterInstance;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.facebook.AccessToken;
import com.google.gson.Gson;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterSession;

import java.io.File;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 17-3-6.
 */

public class AutoSignIn {

    private static final int FIRST_SIGN_IN = -1;
    private static final int HAS_ACCOUNT = 0;

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

    public Observable<Integer> autoLoad() {
        return Observable.just("run")
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(String s) {
                        try {
                            String aesAccount = PreferencesUtils.getString(JConstant.AUTO_SIGNIN_KEY);
                            if (TextUtils.isEmpty(aesAccount)) {
                                Log.d(TAG, "aes account is null");
                                return Observable.just(-1);
                            }
                            String decryption = AESUtil.decrypt(aesAccount);
                            SignType signType = new Gson().fromJson(decryption, SignType.class);
                            Log.d(TAG, "signType: " + signType);
                            if (signType != null) {
                                StringBuilder pwd = FileUtils.readFile(ContextUtils.getContext().getFilesDir() + File.separator + aesAccount + ".dat", "UTF-8");
                                if (!TextUtils.isEmpty(pwd)) {
                                    String finalPwd = AESUtil.decrypt(pwd.toString());
                                    if (signType.type == 1)
                                        JfgCmdInsurance.getCmd().login(signType.account, finalPwd);
                                    else if (signType.type >= 3) {
                                        JfgCmdInsurance.getCmd().openLogin(signType.account, finalPwd, signType.type);
                                    }
                                    AppLogger.d("log type: " + signType+":"+finalPwd);
                                    return Observable.just(0);
                                }
                            }
                            return Observable.just(-1);
                        } catch (Exception e) {
                            AppLogger.e("no sign type");
                            return Observable.just(-1);
                        }
                    }
                });
    }

    
    public Observable<Integer> autoLogin() {
        return Observable.just("run")
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(String s) {
                        try {
                            String aesAccount = PreferencesUtils.getString(JConstant.AUTO_SIGNIN_KEY);
                            if (TextUtils.isEmpty(aesAccount)) {
                                Log.d(TAG, "aes account is null");
                                return Observable.just(-1);
                            }
                            String decryption = AESUtil.decrypt(aesAccount);
                            SignType signType = new Gson().fromJson(decryption, SignType.class);
                            Log.d(TAG, "signType: " + signType);
                            if (signType != null) {
                                StringBuilder pwd = FileUtils.readFile(ContextUtils.getContext().getFilesDir() + File.separator + aesAccount + ".dat", "UTF-8");
                                if (!TextUtils.isEmpty(pwd)) {
                                    String finalPwd = AESUtil.decrypt(pwd.toString());
                                    if (signType.type == 1)
                                        JfgCmdInsurance.getCmd().login(signType.account, finalPwd);
                                    else if (signType.type >= 3) {
                                        //效验本地token是否过期
                                        if(checkTokenOut(signType.type)){
                                            return Observable.just(-1);
                                        }else {
                                            JfgCmdInsurance.getCmd().openLogin(signType.account, finalPwd, signType.type);
                                        }
                                    }
                                    AppLogger.d("log type: " + signType);
                                    return Observable.just(0);
                                }else {
                                    return Observable.just(-1);
                                }
                            }
                            return Observable.just(-1);
                        } catch (Exception e) {
                            AppLogger.e("no sign type");
                            return Observable.just(-1);
                        }
                    }
                });
    }

    private boolean checkTokenOut(int type) {
        boolean isOut = true;
        switch (type){
            case 3:
                isOut = !TencentInstance.getInstance().mTencent.isSessionValid();
                break;
            case 4:
                Oauth2AccessToken oauth2AccessToken = AccessTokenKeeper.readAccessToken(ContextUtils.getContext());
                isOut = !(oauth2AccessToken != null && oauth2AccessToken.isSessionValid());
                break;
            case 6:
//                TwitterSession activeSession = Twitter.getSessionManager().getActiveSession();
//                TwitterAuthToken authToken = activeSession.getAuthToken();
//                if (authToken != null)
//                isOut = !authToken.isExpired();
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
