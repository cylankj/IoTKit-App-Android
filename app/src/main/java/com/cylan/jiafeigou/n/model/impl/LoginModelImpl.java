package com.cylan.jiafeigou.n.model.impl;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.model.BeanInfoLogin;
import com.cylan.jiafeigou.n.model.contract.ModelContract;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginContract;
import com.cylan.jiafeigou.support.sina.AccessTokenKeeper;
import com.cylan.jiafeigou.support.sina.SinaWeiboUtil;
import com.cylan.jiafeigou.support.sina.UsersAPI;
import com.cylan.jiafeigou.support.tencent.TencentLoginUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.superlog.SLog;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chen on 5/30/16.
 */
public class LoginModelImpl implements ModelContract.LoginModelOps {
    private Activity activity;
    private TencentLoginUtils mTencent = null;
    private SinaWeiboUtil mSilna = null;

    private ModelContract.OnLoginListener loginListener;
    private LoginContract.PresenterRequiredOps mPresenter;

    private final int LOGIN_QQ_TYPE = 1;
    private final int LOGIN_XL_TYPE = 2;


    public LoginModelImpl(LoginContract.PresenterRequiredOps loginPresenter) {
        this.mPresenter = loginPresenter;
    }

    @Override
    public String executeLoginda(BeanInfoLogin infoLogin) {
/*        if (infoLogin == null)
            return "请初始化登陆类";
        else if (infoLogin.userName == null)
            return "请输入用户名";
        else if (infoLogin.pwd == null)
            return "请输入密码";*/

        //开始执行登陆请求
        return "succeed";
    }

    @Override
    public void LoginType(Activity activity, int type, ModelContract.OnLoginListener onLoginListener) {
        this.activity = activity;
        this.loginListener = onLoginListener;

        if (type == LOGIN_QQ_TYPE) {
            mTencent = new TencentLoginUtils(activity.getApplicationContext());
            if (!mTencent.isSessionValid()) {
                mTencent.login(activity, new BaseUiListener());
            } else {
                mPresenter.loginInited("qq请求初始化失败");
            }

        } else if (type == LOGIN_XL_TYPE) {

            mSilna = new SinaWeiboUtil(activity.getApplicationContext());
            mSilna.login(activity, new AuthListener());
        }
    }



    @Override
    public TencentLoginUtils getTencentObj() {
        return mTencent;
    }

    @Override
    public SinaWeiboUtil getSinaObj() {
        return mSilna;
    }

    public void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String token = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_EXPIRES_IN);
            String openId = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_OPEN_ID);
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires) && !TextUtils.isEmpty(openId)) {
                mTencent.getMyTencent().setAccessToken(token, expires);
                mTencent.getMyTencent().setOpenId(openId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object response) {
            if (null == response) {
                mTencent.logout(activity);
                loginListener.loginFail("QQ没有返回数据");
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                mTencent.logout(activity);
                loginListener.loginFail("QQ返回数据为空");
                return;
            }
            initOpenidAndToken(jsonResponse);
            String alias = "";
            try {
                if (jsonResponse.has("nickname"))
                    alias = jsonResponse.getString("nickname");
                PreferencesUtils.setThirDswLoginPicUrl(activity, jsonResponse.getString("figureurl_qq_1"));
            } catch (JSONException e) {
                SLog.e(e.toString());
            }

/*            //向服务器发送数据
            if (MyApp.getIsConnectServer()) {
                theThirdPartyLogin(mTencentLoginUtils.getMyTencent().getOpenId(), MsgpackMsg.CLIENT_LOGINBYQQ_REQ, alias);
            } else {
                ToastUtil.showFailToast(LoginActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            }*/

            loginListener.loginSuccess("success");
        }

        @Override
        public void onError(UiError uiError) {
            mTencent.logout(activity);
            loginListener.loginFail(uiError.errorMessage);
        }

        @Override
        public void onCancel() {

        }
    }

    private class AuthListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle values) {
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
            if (accessToken != null && accessToken.isSessionValid()) {
                AccessTokenKeeper.writeAccessToken(activity.getApplicationContext(), accessToken);
                Oauth2AccessToken mAccessToken = AccessTokenKeeper.readAccessToken(activity);
                UsersAPI mUsersAPI = new UsersAPI(mAccessToken);
                long uid = Long.parseLong(mAccessToken.getUid());
                mUsersAPI.show(uid, mListener);
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {

            mSilna.mSsoHandler = null;
            mSilna.logout(activity);
            mPresenter.loginInited("新浪登录异常 1");
        }

        @Override
        public void onCancel() {
            mSilna.mSsoHandler = null;
            mSilna.logout(activity);
        }

    }

    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {

            try {
                if (!TextUtils.isEmpty(response)) {
                    String strId = new JSONObject(response).getString("idstr");
                    PreferencesUtils.setThirDswLoginPicUrl(activity, new JSONObject(response).getString("profile_image_url"));

                    //给服务器发送数据
                /*    if (MyApp.getIsConnectServer())
                        theThirdPartyLogin(strId, MsgpackMsg.CLIENT_LOGINBYSINA_REQ, new JSONObject(response).getString("name"));
                    else
                        ToastUtil.showFailToast(LoginActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));*/
                    loginListener.loginSuccess("微博登陆成功");
                } else {
                    loginListener.loginFail("新浪没有返回数据");
                }
            } catch (JSONException e) {
                SLog.e(e.toString());
            }
            mSilna.mSsoHandler = null;
            mSilna.logout(activity);
        }

        @Override
        public void onWeiboException(WeiboException e) {
            loginListener.loginFail("新浪登录异常 2");
        }
    };
}
