package com.cylan.jiafeigou.support.twitter;

import android.app.Activity;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

/**
 * 作者：zsl
 * 创建时间：2017/3/7
 * 描述：
 */
public class TwitterInstance {
    private static TwitterInstance instance;
    public TwitterAuthClient twitterAuthClient;

    private TwitterInstance() {
        if (twitterAuthClient == null) {
            twitterAuthClient = new TwitterAuthClient();
        }
    }

    public static TwitterInstance getInstance() {
        if (instance == null)
            synchronized (TwitterInstance.class) {
                if (instance == null)
                    instance = new TwitterInstance();
            }
        return instance;
    }

    /**
     * 启动授权
     *
     * @param activity
     */
    public void login(Activity activity, Callback<TwitterSession> callback) {
        twitterAuthClient.authorize(activity, callback);
    }

    public static void release() {
        if (instance != null) {
            instance.twitterAuthClient = null;
            instance = null;
        }
    }

}
