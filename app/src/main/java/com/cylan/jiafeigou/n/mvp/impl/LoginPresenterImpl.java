package com.cylan.jiafeigou.n.mvp.impl;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.cylan.entity.JfgEnum;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JfgCmdEnsurance;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginModelContract;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.support.sina.AccessTokenKeeper;
import com.cylan.jiafeigou.support.sina.SinaLogin;
import com.cylan.jiafeigou.support.sina.UsersAPI;
import com.cylan.jiafeigou.support.tencent.TenCentLoginUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


/**
 * Created by lxh on 16-6-24.
 */
public class LoginPresenterImpl extends AbstractPresenter<LoginModelContract.View>
        implements LoginModelContract.Presenter {

    private Context ctx;
    private CompositeSubscription subscription;

    public LoginPresenterImpl(LoginModelContract.View view) {
        super(view);
        view.setPresenter(this);
        ctx = view.getContext();
    }

    @Override
    public void executeLogin(LoginAccountBean login) {
        if (login != null) {
            rx.Observable.just(login)
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(new Action1<LoginAccountBean>() {
                        @Override
                        public void call(LoginAccountBean o) {
                            AppLogger.d("log: " + o.toString());
                            JfgCmdEnsurance.getCmd().login(o.userName, o.pwd);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            AppLogger.e("executeLogin: " + throwable.getLocalizedMessage());
                        }
                    });
        }
    }

    @Override
    public void start() {
        subscription = new CompositeSubscription();
        subscription.add(RxBus.getInstance()
                .toObservable()
                .delay(1000, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        //sdk中，登陆失败的话，自动一分钟登录一次。
                        if (o instanceof RxEvent.ResultLogin
                                && getView().isLoginViewVisible()) {
                            getView().loginResult(((RxEvent.ResultLogin) o).code);
                        }
                        if (o instanceof RxEvent.ResultRegister
                                && getView().isLoginViewVisible()) {
                            getView().registerResult(((RxEvent.ResultRegister) o).code);
                        }
                        if (o instanceof RxEvent.ResultRegister) {
                            if (((RxEvent.ResultRegister) o).code == JError.ErrorOK) {
                                //注册成功
                                PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN, "");
                                getView().registerResult(((RxEvent.ResultRegister) o).code);
                            }
                        }
                        if (o instanceof RxEvent.ResultVerifyCode) {
                            getView().verifyCodeResult(((RxEvent.ResultVerifyCode) o).code);
                        }
                        if (o instanceof RxEvent.SmsCodeResult
                                && getView().isLoginViewVisible() && JCache.isSmsAction) {
                            getView().registerResult(((RxEvent.SmsCodeResult) o).error);
                            if (((RxEvent.SmsCodeResult) o).error == 0) {
                                //store the token .
                                PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN, ((RxEvent.SmsCodeResult) o).token);
                            }
                        }
                        if (o instanceof RxEvent.SwitchBox) {
                            getView().switchBox("");
                        }
                        if (o instanceof RxEvent.LoginPopBack) {
                            getView().updateAccount(((RxEvent.LoginPopBack) o).account);
                        }
                    }
                }));
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
        TenCentLoginUtils qqUtils = new TenCentLoginUtils(activity);
        qqUtils.login(activity, new QQAuthorizeListener());
    }

    @Override
    public void startSinaAuthorize(Activity activity) {
        SinaLogin sinaUtil = new SinaLogin(activity);
        sinaUtil.login(activity, new SinaAuthorizeListener());
    }

    @Override
    public void registerByPhone(String phone, String verificationCode) {
        AppLogger.d("just send phone ");
    }

    @Override
    public void getCodeByPhone(final String phone) {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        JfgCmdEnsurance.getCmd().sendCheckCode(phone,
                                JfgEnum.JFG_SMS_REGISTER);
                    }
                });
    }

    @Override
    public void verifyCode(final String phone, final String code, final String token) {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        JfgCmdEnsurance.getCmd().verifySMS(phone, code, token);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.i("throw:" + throwable.getLocalizedMessage());
                    }
                });
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
//                PreferencesUtils.setThirDswLoginPicUrl( jsonResponse.getString("figureurl_qq_1"));
//                cmd.openLogin(alias, "", "QQ", ""); // 接口没测
            } catch (JSONException e) {
                AppLogger.e(e.toString());
            }


        }

        @Override
        public void onError(UiError uiError) {
            if (getView() != null) {
                getView().onQQAuthorizeResult(LoginModelContract.AUTHORIZE_ERROR);
            }
            AppLogger.e("errorCode: %d ,error: %s", uiError.errorCode, uiError.errorMessage);
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
                UsersAPI mUsersAPI = new UsersAPI(mAccessToken, getView().getContext());
                long uid = Long.parseLong(mAccessToken.getUid());
                mUsersAPI.show(uid, sinaRequestListener);

            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            if (getView() != null) {
                getView().onSinaAuthorizeResult(LoginModelContract.AUTHORIZE_ERROR);
            }
            AppLogger.e(e.toString());
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
//                    cmd.openLogin(strId, "", "SINA", ""); // 接口没测
                }
            } catch (JSONException e) {
                AppLogger.e(e.toString());
            }

        }

        @Override
        public void onWeiboException(WeiboException e) {
            if (getView() != null) {
                getView().onSinaAuthorizeResult(LoginModelContract.AUTHORIZE_CANCLE);
            }
            AppLogger.e(e.toString());
        }
    };

}
