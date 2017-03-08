package com.cylan.jiafeigou.misc;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.facebook.FacebookInstance;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.qqLogIn.TencentInstance;
import com.cylan.jiafeigou.support.sina.SinaLogin;
import com.cylan.jiafeigou.support.sina.UsersAPI;
import com.cylan.jiafeigou.support.twitter.TwitterInstance;
import com.cylan.jiafeigou.utils.HandlerThreadUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
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

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;

/**
 * 作者：zsl
 * 创建时间：2017/3/7
 * 描述：
 */
public class OpenLoginHelper {

    private static LoginAccountBean loginBean;
    private static OpenLoginHelper instance;

    private static final String TWITTER_KEY = "kCEeFDWzz5xHi8Ej9Wx6FWqRL";
    private static final String TWITTER_SECRET = "Ih4rUwyhKreoHqzd9BeIseAKHoNRszi2rT2udlMz6ssq9LeXw5";

    public OpenLoginHelper() {
    }

    public static OpenLoginHelper getInstance() {
        if (instance == null)
            synchronized (OpenLoginHelper.class) {
                if (instance == null)
                    instance = new OpenLoginHelper();
            }
        return instance;
    }

    /**
     * 根据type选择授权
     * @param type
     * @param activity
     */
    public void loginAuthorize(Activity activity, int type){
        switch (type){
            case 3:
                qqAuthorize(activity);
                break;
            case 4:
                sinaAuthorize(activity);
                break;
            case 6:
                twitterAuthorize(activity);
                break;
            case 7:
                facebookAuthorize(activity);
                break;
        }
    }

    /**
     * QQ授权
     */
    private void qqAuthorize(Activity activity) {
        TencentInstance.getInstance().logIn(activity, new IUiListener() {
            @Override
            public void onComplete(Object response) {
                if (response == null){
                    AppLogger.d("QQ authorize failed");
                    return;
                }
                try {
                    JSONObject jsonObject = (JSONObject) response;
                    String accessToken = jsonObject.getString("access_token");

                    //post获取到的token
                    postAuthorizeResult(accessToken,3);

                    String nickname = jsonObject.getString("nickname");
                    String figureurl = jsonObject.getString("figureurl");
                    PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ICON, figureurl);
                    PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ALIAS, nickname);
                    AppLogger.d("nickname:"+nickname+"figureurl:"+figureurl);
                } catch (Exception e) {
                    AppLogger.d("QQGetUserInfo error:"+e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(UiError uiError) {
                LoginAccountBean login = null;
                RxBus.getCacheInstance().post(login);
                AppLogger.d("QQGetUserInfo error:"+uiError.errorMessage);
            }

            @Override
            public void onCancel() {
                AppLogger.d("QQ authorize cancle");
            }
        });
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
                if(accessToken != null && accessToken.isSessionValid()) {
                    //post 结果
                    postAuthorizeResult(accessToken.getToken(),4);
                } else {
                    String code = bundle.getString("code", "");
                    AppLogger.d("sina_code" + code);
                }

            }

            @Override
            public void onWeiboException(WeiboException e) {
                LoginAccountBean login = null;
                RxBus.getCacheInstance().post(login);
                AppLogger.e("sinaAuthorize:"+e.getLocalizedMessage());
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

                        //post授权结果
                        postAuthorizeResult(strToken,6);

                        // 获取用户的的信息
                        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
                        Call<User> call = twitterApiClient.getAccountService().verifyCredentials(false, false);
                        call.enqueue(new UserInfoBack());
                        AppLogger.d("token:"+strToken);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        LoginAccountBean login = null;
                        RxBus.getCacheInstance().post(login);
                        AppLogger.e("twitterAuthorize:"+e.getLocalizedMessage());
                    }
                });
            }
        });

    }

    /**
     * Twitter用户信息回调
     */
    private class UserInfoBack extends Callback<User>{
        @Override
        public void success(Result<User> result) {
            String twitter_name = result.data.name;
            String[] str = {twitter_name, result.data.profileImageUrl};

            PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ALIAS, str[0]);
            PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ICON, str[1]);
            AppLogger.d("twitterName:"+str[0]);
        }

        @Override
        public void failure(TwitterException e) {
            AppLogger.e("twitter_get_userinfo:"+e.getLocalizedMessage());
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
                        GraphRequest request = GraphRequest.newMeRequest(accessToken,getUserinfo);
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,picture,locale,updated_time,timezone,age_range,first_name,last_name");
                        request.setParameters(parameters);
                        request.executeAsync();

                        //post 结果
                        postAuthorizeResult(accessToken.getToken(),7);
                    }

                    @Override
                    public void onCancel() {
                        AppLogger.d("facebookAuthorize:cancle");
                    }

                    @Override
                    public void onError(FacebookException e) {
                        LoginAccountBean login = null;
                        RxBus.getCacheInstance().post(login);
                        AppLogger.e("facebookAuthorize:"+e.getLocalizedMessage());
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
               AppLogger.d("facebook_getUserinfo:"+name+photo);
           }
       }
   };

    /**
     * post 授权结果
     * @param token
     * @param type
     */
    private void postAuthorizeResult(String token, int type) {
        LoginAccountBean login = new LoginAccountBean();
        login.userName = token;
        login.pwd = "www.cylan.com";
        login.openLoginType = type;
        login.loginType = true;
        RxBus.getCacheInstance().postSticky(login);
    }
}
