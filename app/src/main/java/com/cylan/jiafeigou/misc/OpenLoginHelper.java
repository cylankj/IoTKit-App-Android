package com.cylan.jiafeigou.misc;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.facebook.FacebookInstance;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.qqLogIn.TencentInstance;
import com.cylan.jiafeigou.support.sina.AccessTokenKeeper;
import com.cylan.jiafeigou.support.sina.SinaLogin;
import com.cylan.jiafeigou.support.sina.UsersAPI;
import com.cylan.jiafeigou.support.twitter.TwitterInstance;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.HandlerThreadUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.connect.common.Constants;
import com.tencent.open.utils.HttpUtils;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;

/**
 * 作者：zsl
 * 创建时间：2017/3/7
 * 描述：
 */
public class OpenLoginHelper {

    private static OpenLoginHelper instance;
    private static final String TWITTER_KEY = "kCEeFDWzz5xHi8Ej9Wx6FWqRL";
    private static final String TWITTER_SECRET = "Ih4rUwyhKreoHqzd9BeIseAKHoNRszi2rT2udlMz6ssq9LeXw5";
    private Activity activity;

    public OpenLoginHelper(Activity activity) {
        this.activity = activity;
    }

    public static OpenLoginHelper getInstance(Activity activity) {
        if (instance == null)
            synchronized (OpenLoginHelper.class) {
                if (instance == null)
                    instance = new OpenLoginHelper(activity);
            }
        return instance;
    }

    /**
     * 根据type选择授权
     *
     * @param type
     */
    public void loginAuthorize(int type) {
        switch (type) {
            case 3:
                qqAuthorize(activity);
                break;
            case 4:
                sinaAuthorize(activity);
                break;
            case 6:
                PreferencesUtils.putBoolean(JConstant.TWITTER_INIT_KEY, true);
                twitterAuthorize(activity);
                break;
            case 7:
                PreferencesUtils.putBoolean(JConstant.FACEBOOK_INIT_KEY, true);
                facebookAuthorize(activity);
                break;
        }
    }

    /**
     * QQ授权
     */
    private void qqAuthorize(Activity activity) {
        TencentInstance.getInstance(activity).logIn(qqListener);
    }

    private IUiListener qqListener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject values) {
            initLoginID(values);
        }
    };

    /**
     * 获取qq信息
     *
     * @param jsonObject
     */
    private void initLoginID(JSONObject jsonObject) {
        try {
            if (jsonObject.getInt("ret") == 0) {
                String openID = jsonObject.getString("openid");
                String expires = jsonObject.getString("expires_in");
                String accessToken = jsonObject.getString("access_token");
                //**下面这两步设置很重要,如果没有设置,返回为空**
                TencentInstance.getInstance(activity).mTencent.setOpenId(openID);
                TencentInstance.getInstance(activity).mTencent.setAccessToken(accessToken, expires);
                //post获取到的token
                postAuthorizeResult(accessToken, openID, 3);
                getuserInfo();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getuserInfo() {
        QQToken qqToken = TencentInstance.getInstance(activity).mTencent.getQQToken();
        AppLogger.d("tokendd:" + qqToken.getAccessToken());
        UserInfo qqInfo = new UserInfo(activity, qqToken);
        qqInfo.getUserInfo(getQQinfoListener);
    }

    /**
     * 获取用户信息
     */
    private IUiListener getQQinfoListener = new IUiListener() {
        @Override
        public void onComplete(Object response) {
            try {
                //处理自己需要的信息
                JSONObject jsonObject = (JSONObject) response;
                String nickname = jsonObject.getString("nickname");
                String figureurl = jsonObject.getString("figureurl");
                PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ICON, figureurl);
                PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ALIAS, nickname);
                AppLogger.d("jsonObject:" + jsonObject.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(UiError uiError) {

        }

        @Override
        public void onCancel() {

        }
    };

    private class BaseUiListener implements IUiListener {

        protected void doComplete(JSONObject values) {
        }

        @Override
        public void onComplete(Object response) {
            if (null == response) {
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                return;
            }
            doComplete((JSONObject) response);
        }

        @Override
        public void onError(UiError e) {
            AppLogger.e("qqauthorize:" + e.errorMessage);
        }

        @Override
        public void onCancel() {
            AppLogger.d("qq cancle");
        }
    }

    /**
     * sina授权
     */
    private void sinaAuthorize(Activity activity) {
        SinaLogin.getInstance(activity).login(new WeiboAuthListener() {
            @Override
            public void onComplete(Bundle bundle) {
                Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(bundle);
                UsersAPI usersAPI = new UsersAPI(accessToken, activity);
                Long uid = Long.parseLong(accessToken.getUid());
                usersAPI.show(uid, sinaRequestListener);
                if (accessToken != null && accessToken.isSessionValid()) {
                    AccessTokenKeeper.writeAccessToken(ContextUtils.getContext(), accessToken);
                    //post 结果
                    postAuthorizeResult(accessToken.getToken(), accessToken.getUid(), 4);
                } else {
                    String code = bundle.getString("code", "");
                    AppLogger.d("sina_code" + code);
                }
                SinaLogin.getInstance(activity).mSsoHandler = null;
            }

            @Override
            public void onWeiboException(WeiboException e) {
                LoginAccountBean login = null;
                RxBus.getCacheInstance().postSticky(login);
                AppLogger.e("sinaAuthorize:" + e.getLocalizedMessage());
            }

            @Override
            public void onCancel() {
                AppLogger.e("sinaAuthorize:cancle");
            }
        });
    }

    /**
     * 新浪获取用户信息监听器
     */
    private RequestListener sinaRequestListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            try {
                if (!TextUtils.isEmpty(response)) {
                    String profile_image_url = new JSONObject(response).getString("profile_image_url");
                    String userAlias = new JSONObject(response).getString("screen_name");
                    PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ICON, profile_image_url);
                    PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ALIAS, userAlias);
                }
            } catch (JSONException e) {
                AppLogger.e(e.toString());
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            AppLogger.e(e.toString());
        }
    };

    /**
     * Twitter授权
     */
    private void twitterAuthorize(Activity activity) {
        HandlerThreadUtils.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
                Fabric.with(activity.getApplicationContext(), new TwitterCore(authConfig), new TweetComposer());

                //获取授权
                TwitterInstance.getInstance().login(activity, new Callback<TwitterSession>() {
                    @Override
                    public void success(Result<TwitterSession> result) {
                        TwitterAuthToken token = result.data.getAuthToken();
                        String strToken = token.token;
                        String secret = token.secret;
                        //post授权结果
                        postAuthorizeResult(strToken, secret, 6);

                        // 获取用户的的信息
                        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
                        Call<User> call = twitterApiClient.getAccountService().verifyCredentials(false, false);
                        call.enqueue(new UserInfoBack());
                        AppLogger.d("token:" + strToken);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        LoginAccountBean login = null;
                        RxBus.getCacheInstance().postSticky(login);
                        AppLogger.e("twitterAuthorize:" + e.getLocalizedMessage());
                    }
                });
            }
        });
    }

    /**
     * Twitter用户信息回调
     */
    private class UserInfoBack extends Callback<User> {
        @Override
        public void success(Result<User> result) {
            String twitter_name = result.data.name;
            String[] str = {twitter_name, result.data.profileImageUrl};

            PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ALIAS, str[0]);
            PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ICON, str[1]);
            AppLogger.d("twitterName:" + str[0]);
        }

        @Override
        public void failure(TwitterException e) {
            AppLogger.e("twitter_get_userinfo:" + e.getLocalizedMessage());
        }
    }

    /**
     * facebook授权
     */
    private void facebookAuthorize(Activity activity) {
        HandlerThreadUtils.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                FacebookSdk.sdkInitialize(activity.getApplicationContext());
                //启动授权
                FacebookInstance.getInstance().login(activity, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        AccessToken accessToken = loginResult.getAccessToken();

                        //post 结果
                        postAuthorizeResult(accessToken.getToken(), accessToken.getUserId(), 7);
                        GraphRequest request = GraphRequest.newMeRequest(accessToken, getUserinfo);

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "name,picture,locale,updated_time,timezone,age_range,first_name,last_name");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        AppLogger.d("facebookAuthorize:cancle");
                    }

                    @Override
                    public void onError(FacebookException e) {
                        LoginAccountBean login = null;
                        RxBus.getCacheInstance().postSticky(login);
                        AppLogger.e("facebookAuthorize:" + e.getLocalizedMessage());
                    }
                });
            }
        });
    }

    private GraphRequest.GraphJSONObjectCallback getUserinfo = new GraphRequest.GraphJSONObjectCallback() {
        @Override
        public void onCompleted(JSONObject object, GraphResponse graphResponse) {
            if (object != null) {
                String name = object.optString("name");
                //获取用户头像
                JSONObject object_pic = object.optJSONObject("picture");
                JSONObject object_data = object_pic.optJSONObject("data");
                String photo = object_data.optString("url");

                // 保存用户信息
                PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ICON, photo);
                PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ALIAS, name);
                AppLogger.d("facebook_getUserinfo:" + name + photo);
            }else {
                AppLogger.d("facebook_getUserinfo:obj null");
                AppLogger.d("facebook_back:"+graphResponse.toString());
            }
        }
    };

    /**
     * post 授权结果
     *
     * @param token
     * @param type
     */
    private void postAuthorizeResult(String token, String openId, int type) {
        LoginAccountBean login = new LoginAccountBean();
        login.userName = token;
        login.pwd = openId;
        login.openLoginType = type;
        login.loginType = true;
        RxBus.getCacheInstance().postSticky(login);
    }

    public static void release() {
        if (instance != null) {
            instance.activity = null;
            instance = null;
        }
    }
}
