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
         *
         * @param callback
         */
        void checkAccountResult(RxEvent.CheckRegsiterBack callback);

        void authorizeResult();

        void reShowAccount(String account);
    }

    interface Presenter extends BasePresenter {
        /**
         * 执行登陆
         *
         * @param info
         */
        void executeLogin(LoginAccountBean info);

        void registerByPhone(String phone, String verificationCode);

        void getCodeByPhone(String phone);

        void verifyCode(String phone, String code, String token);

        /**
         * 检测账号是否已经注册
         *
         * @param account
         */
        void checkAccountIsReg(String account);

        /**
         * 是否已注册的回调
         *
         * @return
         */
        Subscription checkAccountBack();

        /**
         * 登录计时
         */
        void loginCountTime();

        /**
         * 账号回显
         *
         * @return
         */
        String getTempAccPwd();

        /**
         * 十分钟是否超过3次获取
         * @return
         */
        boolean checkOverCount(String account);

        /**
         * 三方授权结果
         * @return
         */
        Subscription thirdAuthorizeBack();

        /**
         * 回显账号
         * @return
         */
        Subscription reShowAccount();
    }

}
