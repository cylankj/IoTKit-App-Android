package com.cylan.jiafeigou.n.mvp.impl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.cylan.entity.JfgEnum;
import com.cylan.jiafeigou.R;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginContract;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.qqLogIn.Constants;
import com.cylan.jiafeigou.support.qqLogIn.TencentInstance;
import com.cylan.jiafeigou.support.sina.AccessTokenKeeper;
import com.cylan.jiafeigou.support.sina.SinaLogin;
import com.cylan.jiafeigou.support.sina.UsersAPI;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.google.gson.Gson;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;
import org.msgpack.util.json.JSON;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


/**
 * Created by lxh on 16-6-24.
 */
public class LoginPresenterImpl extends AbstractPresenter<LoginContract.View>
        implements LoginContract.Presenter {

    private Context ctx;
    private CompositeSubscription subscription;
    private SinaLogin sinaUtil;
    private TencentInstance tencentInstance;
    private QQAuthrizeListener qqAuthrizeListener;

    public LoginPresenterImpl(LoginContract.View view) {
        super(view);
        view.setPresenter(this);
        ctx = view.getContext();
    }

    @Override
    public void executeLogin(final LoginAccountBean login) {
        Observable.just(login)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<LoginAccountBean, LoginAccountBean>() {
                    @Override
                    public LoginAccountBean call(LoginAccountBean o) {
                        try {
                        JfgCmdInsurance.getCmd().login(o.userName, o.pwd);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                        AppLogger.i("LoginAccountBean: " + new Gson().toJson(login));
                        //非三方登录的标记
                        RxBus.getCacheInstance().postSticky(false);
                        return o;
                    }
                })
                .subscribe();
    }

    /**
     * 执行第三方登录
     * @param openId
     */
    @Override
    public void executeOpenLogin(final String openId) {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        try {
                            JfgCmdInsurance.getCmd().openLogin(openId,"www.cylan.com");
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                        //第三方登录的标记
                        RxBus.getCacheInstance().postSticky(true);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("executeOpenLogin"+new Gson().toJson(openId));
                    }
                });

    }

    @Override
    public void start() {
        unSubscribe(subscription);
        subscription = new CompositeSubscription();
        subscription.add(resultLoginSub());
        subscription.add(resultRegisterSub());
        subscription.add(resultVerifyCodeSub());
        subscription.add(smsCodeResultSub());
        subscription.add(switchBoxSub());
        subscription.add(loginPopBackSub());
    }

    private Subscription resultLoginSub() {
        //sdk中，登陆失败的话，自动一分钟登录一次。
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultLogin.class)
                .delay(500, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ResultLogin>() {
                    @Override
                    public void call(RxEvent.ResultLogin resultLogin) {
                        if (getView().isLoginViewVisible()) {
                            getView().loginResult(resultLogin.code);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("" + throwable);
                    }
                });
    }

    private Subscription resultRegisterSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultRegister.class)
                .subscribeOn(Schedulers.newThread())
                .delay(1000, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ResultRegister>() {
                    @Override
                    public void call(RxEvent.ResultRegister register) {
                        if (getView().isLoginViewVisible()) {
                            getView().registerResult(register.code);
                        }
                        if (register.code == JError.ErrorOK) {
                            //注册成功
                            PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN, "");
                            getView().registerResult(register.code);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("" + throwable.getLocalizedMessage());
                    }
                });
    }

    private Subscription resultVerifyCodeSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultVerifyCode.class)
                .subscribeOn(Schedulers.newThread())
                .delay(1000, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ResultVerifyCode>() {
                    @Override
                    public void call(RxEvent.ResultVerifyCode resultVerifyCode) {
                        getView().verifyCodeResult(resultVerifyCode.code);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("" + throwable.getLocalizedMessage());
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        AppLogger.d("complete?");
                    }
                });
    }

    private Subscription smsCodeResultSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SmsCodeResult.class)
                .delay(1000, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.SmsCodeResult>() {
                    @Override
                    public void call(RxEvent.SmsCodeResult smsCodeResult) {
                        if (getView().isLoginViewVisible() && JCache.isSmsAction) {
                            getView().registerResult(smsCodeResult.error);
                            if (smsCodeResult.error == 0) {
                                //store the token .
                                PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN, smsCodeResult.token);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("" + throwable.getLocalizedMessage());
                    }
                });
    }

    private Subscription switchBoxSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SwitchBox.class)
                .delay(1000, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.SwitchBox>() {
                    @Override
                    public void call(RxEvent.SwitchBox switchBox) {
                        getView().switchBox("");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("" + throwable.getLocalizedMessage());
                    }
                });

    }

    private Subscription loginPopBackSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LoginPopBack.class)
                .delay(1000, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.LoginPopBack>() {
                    @Override
                    public void call(RxEvent.LoginPopBack loginPopBack) {
                        getView().updateAccount(loginPopBack.account);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("" + throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public void stop() {
        unSubscribe(subscription);
    }


    @Override
    public void getQQAuthorize(Activity activity) {
        tencentInstance = new TencentInstance();
        qqAuthrizeListener = new QQAuthrizeListener();
        tencentInstance.logIn(activity,Constants.SCOPE, qqAuthrizeListener);
    }

    @Override
    public void startSinaAuthorize(Activity activity) {
        sinaUtil = new SinaLogin(activity);
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
                        JfgCmdInsurance.getCmd().sendCheckCode(phone,
                                JfgEnum.JFG_SMS_REGISTER);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("" + throwable.getLocalizedMessage());
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
                        try {
                        JfgCmdInsurance.getCmd().verifySMS(phone, code, token);
                        } catch (JfgException e) {
                            e.printStackTrace();
                    }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.i("throw:" + throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public SsoHandler getSinaCallBack() {
        return sinaUtil.mSsoHandler;
    }

    /**
     * QQ登录在OnActivity中的回调
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResultData(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode,resultCode,data,qqAuthrizeListener);
    }

    /**
     * 新浪微博的授权
     */
    private class SinaAuthorizeListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle values) {
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
            if (accessToken != null && accessToken.isSessionValid()) {
                executeOpenLogin(accessToken.getToken());
            }else {
                String code = values.getString("code", "");
                AppLogger.d("sina_code"+code);
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            if (getView() != null) {
                getView().onSinaAuthorizeResult(LoginContract.AUTHORIZE_ERROR);
            }
            AppLogger.e(e.toString());
        }

        @Override
        public void onCancel() {
            if (getView() != null) {
                getView().onSinaAuthorizeResult(LoginContract.AUTHORIZE_CANCLE);
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
                    ToastUtil.showToast(strId);
//                    PreferencesUtils.setThirDswLoginPicUrl(ctx, new JSONObject(response).getString("profile_image_url"));
//                    cmd.openLogin(strId, "", "SINA", ""); // 接口没测
                    executeOpenLogin(strId);
                }
            } catch (JSONException e) {
                AppLogger.e(e.toString());
            }

        }

        @Override
        public void onWeiboException(WeiboException e) {
            if (getView() != null) {
                getView().onSinaAuthorizeResult(LoginContract.AUTHORIZE_CANCLE);
            }
            AppLogger.e(e.toString());
        }
    };

    /**
     * QQ授权回调
     */
    private class QQAuthrizeListener implements IUiListener{

        @Override
        public void onComplete(Object response) {
            if (null == response) {
                if (getView() != null) {
                    getView().onQQAuthorizeResult(LoginContract.AUTHORIZE_ERROR);
                }
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                ToastUtil.showNegativeToast("授权失败");
                return;
            }
            doComplete((JSONObject)response);
        }

        @Override
        public void onError(UiError uiError) {
            if (getView() != null) {
                getView().onQQAuthorizeResult(LoginContract.AUTHORIZE_ERROR);
            }
        }

        @Override
        public void onCancel() {
            if (getView() != null) {
                getView().onQQAuthorizeResult(LoginContract.AUTHORIZE_CANCLE);
            }
        }
    }

    /**
     * QQ登录回调解析token
     * @param response
     */
    private void doComplete(JSONObject response) {
        try {
            String openID = response.getString("openid");
            String accessToken = response.getString("access_token");
            String expires = response.getString("expires_in");
            //执行登录
            executeOpenLogin(accessToken);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
