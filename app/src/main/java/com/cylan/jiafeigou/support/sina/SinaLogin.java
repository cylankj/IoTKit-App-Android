package com.cylan.jiafeigou.support.sina;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.sina.weibo.sdk.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class SinaLogin {

    /**
     * 访问微博服务接口的地址
     */
    public static final String API_SERVER = "https://api.weibo.com/2";

    private static final String URL_ACCOUNT = API_SERVER + "/account";

    private static SinaLogin instance;
    private Context context;
    /**
     * post请求方式
     */
    public static final String HTTPMETHOD_POST = "POST";

    private static final String TAG = "SinaLogin";


    private AuthInfo mWeibo;

    public static String APP_KEY;
    /**
     * 调用SSO授权
     **/
    public SsoHandler mSsoHandler;

    public SinaLogin(Context context) {
        this.context = context;
//        APP_KEY = PackageUtils.getMetaString(context, "sina_app_key");
        APP_KEY = "1315129656";
        mWeibo = new AuthInfo(context, APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        LogUtil.enableLog();
    }


    public static SinaLogin getInstance(Activity activity) {
        if (instance == null)
            synchronized (SinaLogin.class) {
                if (instance == null)
                    instance = new SinaLogin(activity);
            }
        return instance;
    }

    /**
     * @param ctx
     * @param mAuthListener
     */
    public void login(Context ctx, WeiboAuthListener mAuthListener) {
        if (null == mSsoHandler && mWeibo != null) {
            mSsoHandler = new SsoHandler((Activity) ctx, mWeibo);
        }

        if (mSsoHandler != null) {
            mSsoHandler.authorize(mAuthListener);
        } else {
            LogUtil.e(TAG, "Please setWeiboAuthInfo(...) for first");
        }
    }

    /**
     * @param mAuthListener
     */
    public void login(WeiboAuthListener mAuthListener) {
        if (null == mSsoHandler && mWeibo != null) {
            mSsoHandler = new SsoHandler((Activity) context, mWeibo);
        }

        if (mSsoHandler != null) {
            mSsoHandler.authorize(mAuthListener);
        } else {
            LogUtil.e(TAG, "Please setWeiboAuthInfo(...) for first");
        }
    }

    public void logout(Context ctx) {
        if (AccessTokenKeeper.readAccessToken(ctx) != null && AccessTokenKeeper.readAccessToken(ctx).isSessionValid()) {
            LogOutRequestListener mLogoutListener = new LogOutRequestListener(ctx);
            new LogoutAPI(AccessTokenKeeper.readAccessToken(ctx)).logout(ctx, mLogoutListener);
        }
    }

    /**
     * 登出按钮的监听器，接收登出处理结果。（API 请求结果的监听器）
     */
    private class LogOutRequestListener implements RequestListener {

        private Context mContext;

        public LogOutRequestListener(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                try {
                    JSONObject obj = new JSONObject(response);
                    String value = obj.getString("result");

                    if ("true".equalsIgnoreCase(value)) {
                        AccessTokenKeeper.clear(mContext);
                    }
                } catch (JSONException e) {
                    AppLogger.e(e.toString());
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
        }
    }

    /**
     * 检查是否已授权
     *
     * @return true 已授权，false 未授权
     */
    public boolean isAuth(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences(AccessTokenKeeper.PREFERENCES_NAME, Context.MODE_APPEND);
        String token = pref.getString(AccessTokenKeeper.KEY_ACCESS_TOKEN, "");
        return !TextUtils.isEmpty(token);
    }

    /**
     * 该类提供了授权回收接口，帮助开发者主动取消用户的授权。
     * 详情请参考<activity_cloud_live_mesg_call_out_item href="http://t.cn/zYeuB0k">授权回收</activity_cloud_live_mesg_call_out_item>
     *
     * @author SINA
     * @since 2013-11-05
     */
    public class LogoutAPI extends AbsOpenAPI {
        /**
         * 注销地址（URL）
         */
        private static final String REVOKE_OAUTH_URL = "https://api.weibo.com/oauth2/revokeoauth2";

        /**
         * 构造函数。
         *
         * @param oauth2AccessToken Token 实例
         */
        public LogoutAPI(Oauth2AccessToken oauth2AccessToken) {
            super(oauth2AccessToken);
        }

        /**
         * 异步取消用户的授权。
         *
         * @param listener 异步请求回调接口
         */
        public void logout(Context context,
                           RequestListener listener) {
            requestAsync(context, REVOKE_OAUTH_URL,
                    getWeiboParameters(), HTTPMETHOD_POST, listener);
        }
    }

    public static WeiboParameters getWeiboParameters() {
        return new WeiboParameters(APP_KEY);
    }
}
