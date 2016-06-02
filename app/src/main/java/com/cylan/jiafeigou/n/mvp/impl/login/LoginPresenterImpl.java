package com.cylan.jiafeigou.n.mvp.impl.login;

import com.cylan.jiafeigou.n.model.contract.ModelContract;
import com.cylan.jiafeigou.n.model.impl.LoginModelImpl;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginContract;
import com.cylan.jiafeigou.n.view.login.InfoLogin;

import java.lang.ref.WeakReference;

/**
 * Created by chen on 5/30/16.
 */
public class LoginPresenterImpl implements LoginContract.PresenterOps,LoginContract.PresenterRequiredOps {


    private  WeakReference<LoginContract.ViewRequiredOps> mView;
    private  ModelContract.LoginModelOps mModel;


    public LoginPresenterImpl(LoginContract.ViewRequiredOps loginFrament) {
        this.mView = new WeakReference<LoginContract.ViewRequiredOps>(loginFrament);
        this.mModel = new LoginModelImpl(this);
    }

    @Override
    public void executeLogin(InfoLogin infoLogin) {
        final LoginContract.ViewRequiredOps mViewRef = mView.get();
        if (mViewRef != null) {
            mViewRef.LoginExecuted(mModel.executeLoginda(infoLogin));
        }
    }
}
