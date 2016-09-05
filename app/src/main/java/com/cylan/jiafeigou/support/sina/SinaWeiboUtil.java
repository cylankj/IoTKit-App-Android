package com.cylan.jiafeigou.support.sina;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuth.AuthInfo;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

;

public class SinaWeiboUtil {

    /**
     * 访问微博服务接口的地址
     */
    public static final String API_SERVER = "https://api.weibo.com/2";

    private static final String URL_ACCOUNT = API_SERVER + "/account";

    /**
     * post请求方式
     */
    public static final String HTTPMETHOD_POST = "POST";

    private static final String TAG = "SinaWeiboUtil";


    private AuthInfo mWeibo;

    /**
     * 调用SSO授权
     **/
    public SsoHandler mSsoHandler;

    private WeiboListener listener;

    public SinaWeiboUtil(Context context) {
        mWeibo = new AuthInfo(context, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
    }


    public SsoHandler getMySsoHandler() {
        return mSsoHandler;
    }

    // public boolean isVaild(){
    // mWeibo.
    // }

    public void login(Context ctx, WeiboAuthListener mAuthListener) {
        if (null == mSsoHandler && mWeibo != null) {
            WeiboAuth weiboAuth = new WeiboAuth(ctx, mWeibo);
            mSsoHandler = new SsoHandler((Activity) ctx, weiboAuth);
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
            new LogoutAPI(AccessTokenKeeper.readAccessToken(ctx)).logout(mLogoutListener);
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

}
