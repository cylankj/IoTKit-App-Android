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
         * @return
         */
        Subscription checkSmsCodeBack();

        /**
         *重置密码
         */
        void resetPassword(String newPassword);
    }
}

