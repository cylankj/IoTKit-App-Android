package com.cylan.jiafeigou.n.mvp.contract.login;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;

import org.msgpack.annotation.NotNullable;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface ForgetPwdContract {

    int AUTHORIZE_PHONE = 2;
    int AUTHORIZE_MAIL = 1;
    /**
     * 账号未注册
     */
    int THIS_ACCOUNT_NOT_REGISTERED = 0;


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

        void submitPhoneNumAndCode(final String account, final String code);


    }
}

