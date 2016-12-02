package com.cylan.jiafeigou.n.mvp.impl;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.cylan.entity.JfgEnum;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginContract;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.sina.AccessTokenKeeper;
import com.cylan.jiafeigou.support.sina.SinaLogin;
import com.cylan.jiafeigou.support.sina.UsersAPI;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;
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
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.SafeSubscriber;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


/**
 * Created by lxh on 16-6-24.
 */
public class LoginPresenterImpl extends AbstractPresenter<LoginContract.View>
        implements LoginContract.Presenter {

    private Context ctx;
    private CompositeSubscription subscription;

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
                        JfgCmdInsurance.getCmd().login(o.userName, o.pwd);
                        AppLogger.e("LoginAccountBean: " + new Gson().toJson(login));
                        return o;
                    }
                })
                .subscribe();
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
//        TenCentLoginUtils qqUtils = new TenCentLoginUtils(activity);
//        qqUtils.login(activity, new QQAuthorizeListener());
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
                        JfgCmdInsurance.getCmd().verifySMS(phone, code, token);
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
                    getView().onQQAuthorizeResult(LoginContract.AUTHORIZE_ERROR);
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
//                cmd.openLogin(name, "", "QQ", ""); // 接口没测
            } catch (JSONException e) {
                AppLogger.e(e.toString());
            }


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
                getView().onSinaAuthorizeResult(LoginContract.AUTHORIZE_CANCLE);
            }
            AppLogger.e(e.toString());
        }
    };

}
