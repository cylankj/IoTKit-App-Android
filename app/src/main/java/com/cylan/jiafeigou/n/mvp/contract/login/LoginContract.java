package com.cylan.jiafeigou.n.mvp.contract.login;

import android.app.Activity;
import android.content.Intent;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.rx.RxEvent;
import com.facebook.CallbackManager;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import rx.Subscription;

/**
 * Created by lxh on 16-6-24.
 */
public interface LoginContract {

    /**
     * 授权失败
     */
    static final int AUTHORIZE_ERROR = 2;

    /**
     * 取消授权
     */
    static final int AUTHORIZE_CANCLE = 1;

    /**
     * 授权成功
     */
    static final int AUTHORIZE_SUCCESSFUL = 0;


    interface View extends BaseView<Presenter> {

        /**
         * @return
         */
        boolean isLoginViewVisible();

        void verifyCodeResult(int code);

        /**
         * 登陆结果
         *
         * @param code , 只需要关注BeanInfoLogin 中的ret 和session . ret 为0才有session
         */
        void loginResult(int code);


        /**
         * 新浪授权的结果
         *
         * @param ret 0为成功，1为用户取消授权，2为授权失败。
         */
        void onQQAuthorizeResult(int ret);

        /**
         * 新浪授权的结果
         *
         * @param ret 0为成功，1为用户取消授权，2为授权失败。
         */
        void onSinaAuthorizeResult(int ret);

        /**
         * @param result:查阅 error_define.md
         */
        void registerResult(int result);

        void switchBox(String account);

        void updateAccount(String account);

        /**
         * 登陆超时
         */
        void loginTimeout();

        /**
         * 注册跳转到设置密码页
         */
        void jump2NextPage();

        /**
         * 检测好友回调的结果
         * @param callback
         */
        void checkAccountResult(RxEvent.CheckRegsiterBack callback);


    }

    interface Presenter extends BasePresenter {
        /**
         * 执行登陆
         *
         * @param info
         */
        void executeLogin(LoginAccountBean info);

        /**
         * 执行第三方登录
         */
        void executeOpenLogin(String openId, int type);

        /**
         * 获取QQ的授权
         */
        void getQQAuthorize(Activity activity);

        /**
         * 获取新浪微博的授权
         */
        void startSinaAuthorize(Activity activity);

        /**
         * 获取twiiter的授权
         * @param activity
         */
        void getTwitterAuthorize(Activity activity);

        /**
         * 获取Facebook的授权
         * @param activity
         */
        void getFaceBookAuthorize(Activity activity);

        void registerByPhone(String phone, String verificationCode);

        void getCodeByPhone(String phone);

        void verifyCode(String phone, String code, String token);

        /**
         * 拿到新浪回调对象
         *
         * @return
         */
        SsoHandler getSinaCallBack();

        /**
         * QQ登录在onactivity中的回调
         *
         * @param requestCode
         * @param resultCode
         * @param data
         */
        void onActivityResultData(int requestCode, int resultCode, Intent data);

        /**
         * 获取Twitter的回调对象
         * @return
         */
        TwitterAuthClient getTwitterBack();

        /**
         * 检测账号是否已经注册
         * @param account
         */
        void checkAccountIsReg(String account);

        /**
         * 是否已注册的回调
         * @return
         */
        Subscription checkAccountBack();

        /**
         * 登录计时
         */
        void loginCountTime();

        /**
         * 账号回显
         * @return
         */
        String getTempAccPwd();

        /**
         * fackBook授权回调结果
         */
        void fackBookCallBack();

        /**
         * 获取facebook回调的对象
         * @return
         */
        CallbackManager getFaceBookBackObj();
    }

}
