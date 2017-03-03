package com.cylan.jiafeigou.n.mvp.contract.login;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;

import rx.Subscription;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface ForgetPwdContract {


    interface View extends BaseView<Presenter> {

        /**
         * 登陆结果
         *
         * @param bean , 返回结果。
         */
        void submitResult(RequestResetPwdBean bean);

        /**
         * 校验短信验证码结果
         */
        void checkSmsCodeResult(int code);

        /**
         * 重置密码的结果
         *
         * @param code
         */
        void resetPwdResult(int code);

        /**
         * 检测是否注册的结果
         */
        void checkIsRegReuslt(int code);

    }

    interface Presenter extends BasePresenter {

        /**
         * 忘记密码，第一步：提交账号，等待返回。
         *
         * @param account
         */
        void submitAccount(final String account);

        /**
         * 手机号，忘记密码，手机号+验证码
         *
         * @param account
         * @param code
         */
        void submitPhoneNumAndCode(final String account, final String code);

        /**
         * 短信验证码的回调
         *
         * @return
         */
        Subscription checkSmsCodeBack();

        /**
         * 重置密码
         */
        void resetPassword(String newPassword);

        /**
         * 重置密码的回调
         *
         * @return
         */
        Subscription resetPwdBack();

        /**
         * 检测是否已注册
         *
         * @param account
         */
        void checkIsReg(String account);

        /**
         * 检测是否已注册回调
         *
         * @return
         */
        Subscription checkIsRegBack();

        /**
         * 十分钟是否超过3次获取
         * @return
         */
        boolean checkOverCount();

    }
}

