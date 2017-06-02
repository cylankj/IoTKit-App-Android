package com.cylan.jiafeigou.support.twitter;

//import android.app.Activity;
//
//import com.cylan.jiafeigou.misc.JConstant;
//import com.cylan.jiafeigou.support.log.AppLogger;
//import com.cylan.jiafeigou.utils.PreferencesUtils;
//import com.cylan.jiafeigou.utils.ToastUtil;
//import com.twitter.sdk.android.core.Callback;
//import com.twitter.sdk.android.core.Result;
//import com.twitter.sdk.android.core.TwitterApiClient;
//import com.twitter.sdk.android.core.TwitterAuthToken;
//import com.twitter.sdk.android.core.TwitterCore;
//import com.twitter.sdk.android.core.TwitterException;
//import com.twitter.sdk.android.core.TwitterSession;
//import com.twitter.sdk.android.core.identity.TwitterAuthClient;
//import com.twitter.sdk.android.core.models.User;
//
//import retrofit2.Call;
//
///**
// * 作者：zsl
// * 创建时间：2017/3/7
// * 描述：
// */
//public class TwitterInstance {
//    private static TwitterInstance instance;
//    public TwitterAuthClient twitterAuthClient;
//
//    private TwitterInstance() {
//        if (twitterAuthClient == null) {
//            twitterAuthClient = new TwitterAuthClient();
//        }
//    }
//
//    public static TwitterInstance getInstance() {
//        if (instance == null)
//            synchronized (TwitterInstance.class) {
//                if (instance == null)
//                    instance = new TwitterInstance();
//            }
//        return instance;
//    }
//
//    /**
//     * 启动授权
//     *
//     * @param activity
//     */
//    public void login(Activity activity, Callback<TwitterSession> callback) {
//        twitterAuthClient.authorize(activity, callback);
//    }
//
//    public static void release() {
//        if (instance != null) {
//            instance.twitterAuthClient = null;
//            instance = null;
//        }
//    }
//
//}
