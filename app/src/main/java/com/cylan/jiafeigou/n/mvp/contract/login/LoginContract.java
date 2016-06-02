package com.cylan.jiafeigou.n.mvp.contract.login;

import com.cylan.jiafeigou.n.model.BeanInfoLogin;

/**
 * Created by chen on 5/30/16.
 */
public interface LoginContract {

    interface ViewRequiredOps {

        void  LoginExecuted(String succeed);

    }

    interface PresenterOps {

        void executeLogin(BeanInfoLogin infoLogin);
    }

    interface PresenterRequiredOps {

    }
}
