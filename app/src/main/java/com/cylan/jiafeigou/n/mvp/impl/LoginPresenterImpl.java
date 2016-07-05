package com.cylan.jiafeigou.n.mvp.impl;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.cylan.jiafeigou.n.mvp.contract.login.LoginModelContract;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.support.sina.AccessTokenKeeper;
import com.cylan.jiafeigou.support.sina.SinaWeiboUtil;
import com.cylan.jiafeigou.support.sina.UsersAPI;
import com.cylan.jiafeigou.support.tencent.TencentLoginUtils;
import com.cylan.sdkjni.JfgCmd;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.superlog.SLog;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by lxh on 16-6-24.
 */
public class LoginPresenterImpl extends AbstractPresenter<LoginModelContract.View> implements LoginModelContract.Presenter {

    private JfgCmd cmd;
    Context ctx;
    CompositeSubscription subscription;

    public LoginPresenterImpl(LoginModelContract.View view) {
        super(view);
        view.setPresenter(this);
        ctx = view.getContext();
    }

    @Override
    public void executeLogin(LoginAccountBean login) {
        if (login != null) {
            if (!TextUtils.isEmpty(login.session)) {
                cmd.reLogin(login.session);
            } else {
                cmd.login(login.userName, login.pwd, "");
            }
        }
    }


    @Override
    public void start() {
        cmd = JfgCmd.getCmd();
        subscription = new CompositeSubscription();
        subscription.add(
                RxBus.getInstance()
                        .toObservable()
                        .delay(3000, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                if (o instanceof LoginAccountBean) {
                                    getView().loginResult((LoginAccountBean) o);
                                }
                            }
                        })
        );

    }

    @Override
    public void stop() {
        if (subscription != null) {
            if (!subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
    }


    @Override
    public void getQQAuthorize(Activity activity) {
        TencentLoginUtils qqUtils = new TencentLoginUtils(activity);
        qqUtils.login(activity, new QQAuthorizeListener());
    }

    @Override
    public void getSinaAuthorize(Activity activity) {
        SinaWeiboUtil sinaUtil = new SinaWeiboUtil(activity);
        sinaUtil.login(activity, new SinaAuthorizeListener());
    }

    @Override
    public void registerByPhone(String phone, String verificationCode) {
        SLog.d("just send phone ");
    }

    /**
     * QQ授权的监听器
     */
    private class QQAuthorizeListener implements IUiListener {

        @Override
        public void onComplete(Object response) {
            if (null == response) {
                if (getView() != null) {
                    getView().onQQAuthorizeResult(LoginModelContract.AUTHORIZE_ERROR);
                }
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            String alias = "";
            try {
                if (jsonResponse.has("nickname")) {
                    alias = jsonResponse.getString("nickname");
                }
//                PreferencesUtils.setThirDswLoginPicUrl(ctx, jsonResponse.getString("figureurl_qq_1"));
                cmd.openLogin(alias, "", "QQ", ""); // 接口没测
            } catch (JSONException e) {
                SLog.e(e.toString());
            }


        }

        @Override
        public void onError(UiError uiError) {
            if (getView() != null) {
                getView().onQQAuthorizeResult(LoginModelContract.AUTHORIZE_ERROR);
            }
            SLog.e("errorCode: %d ,error: %s", uiError.errorCode, uiError.errorMessage);
        }

        @Override
        public void onCancel() {
            if (getView() != null) {
                getView().onQQAuthorizeResult(LoginModelContract.AUTHORIZE_CANCLE);
            }
        }
    }


    /**
     * 新浪微博的授权
     */
    private class SinaAuthorizeListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle values) {
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
            if (accessToken != null && accessToken.isSessionValid()) {
                AccessTokenKeeper.writeAccessToken(ctx, accessToken);
                Oauth2AccessToken mAccessToken = AccessTokenKeeper.readAccessToken(ctx);
                UsersAPI mUsersAPI = new UsersAPI(mAccessToken);
                long uid = Long.parseLong(mAccessToken.getUid());
                mUsersAPI.show(uid, sinaRequestListener);

            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            if (getView() != null) {
                getView().onSinaAuthorizeResult(LoginModelContract.AUTHORIZE_ERROR);
            }
            SLog.e(e.toString());
        }

        @Override
        public void onCancel() {
            if (getView() != null) {
                getView().onSinaAuthorizeResult(LoginModelContract.AUTHORIZE_CANCLE);
            }
        }

    }


    /**
     * 新浪权限检查的监听器
     */
    private RequestListener sinaRequestListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            try {
                if (!TextUtils.isEmpty(response)) {
                    String strId = new JSONObject(response).getString("idstr");
//                    PreferencesUtils.setThirDswLoginPicUrl(ctx, new JSONObject(response).getString("profile_image_url"));
                    cmd.openLogin(strId, "", "SINA", ""); // 接口没测
                }
            } catch (JSONException e) {
                SLog.e(e.toString());
            }

        }

        @Override
        public void onWeiboException(WeiboException e) {
            if (getView() != null) {
                getView().onSinaAuthorizeResult(LoginModelContract.AUTHORIZE_CANCLE);
            }
            SLog.e(e.toString());
        }
    };

}
