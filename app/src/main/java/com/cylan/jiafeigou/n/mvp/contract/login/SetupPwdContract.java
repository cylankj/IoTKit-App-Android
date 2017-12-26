package com.cylan.jiafeigou.n.mvp.contract.login;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.rx.RxEvent;

/**
 * Created by chen on 5/30/16.
 */
public interface SetupPwdContract {


    interface View extends BaseView {

        /**
         * 登陆结果
         *
         * @param register , 返回结果。
         */
        void submitResult(RxEvent.ResultRegister register);

        void loginResult(int code);

        boolean isLoginViewVisible();
    }

    interface Presenter extends BasePresenter {
        void register(final String account, final String pwd, int type, String token);

        void executeLogin(LoginAccountBean login);

    }

}
