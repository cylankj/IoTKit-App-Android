package com.cylan.jiafeigou.n.mvp.contract.login;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;

import org.msgpack.annotation.NotNullable;

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
         * 账号
         *
         * @param account
         */
        void executeSubmitAccount(@NotNullable String account);

        void submitForVerificationCode(final String account);

        void submitPhoneNumAndCode(final String account, final String code);


    }
}

