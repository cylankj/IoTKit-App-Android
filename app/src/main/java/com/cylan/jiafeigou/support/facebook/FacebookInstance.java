package com.cylan.jiafeigou.support.facebook;

import android.app.Activity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

/**
 * 作者：zsl
 * 创建时间：2017/3/7
 * 描述：
 */
public class FacebookInstance {

    private static FacebookInstance instance;
    public CallbackManager callbackManager;

    private FacebookInstance() {
        if (callbackManager == null) {
            callbackManager = CallbackManager.Factory.create();
        }
    }

    public static FacebookInstance getInstance() {
        if (instance == null)
            synchronized (FacebookInstance.class) {
                if (instance == null)
                    instance = new FacebookInstance();
            }
        return instance;
    }

    public void login(Activity activity, FacebookCallback<LoginResult> callback) {
        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile", "user_friends", "email"));
        LoginManager.getInstance().registerCallback(callbackManager, callback);
    }

    public static void release(){
        if (instance != null){
            instance.callbackManager = null;
            instance = null;
        }
    }
}
