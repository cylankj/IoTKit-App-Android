package com.cylan.jiafeigou.n.mvp.contract.login;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;

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
         * @param account
         * @param code
         */
        void submitPhoneNumAndCode(final String account, final String code);


    }
}

