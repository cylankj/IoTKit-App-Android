package com.cylan.jiafeigou.n.mvp.contract.login;

import com.cylan.jiafeigou.n.view.login.InfoLogin;

/**
 * Created by chen on 5/30/16.
 */
public interface LoginContract {

    interface ViewRequiredOps {

        void  LoginExecuted(String succeed);

    }

    interface PresenterOps {

        void executeLogin(InfoLogin infoLogin);
    }

    interface PresenterRequiredOps {

    }
}
