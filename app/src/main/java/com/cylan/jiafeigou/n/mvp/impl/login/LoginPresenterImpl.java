package com.cylan.jiafeigou.n.mvp.impl.login;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.cylan.jiafeigou.n.model.contract.ModelContract;
import com.cylan.jiafeigou.n.model.impl.LoginModelImpl;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginContract;
import com.cylan.jiafeigou.n.model.BeanInfoLogin;
import com.cylan.jiafeigou.support.sina.SinaWeiboUtil;
import com.cylan.jiafeigou.support.tencent.TencentLoginUtils;
import com.cylan.jiafeigou.utils.UiHelper;

import java.lang.ref.WeakReference;

/**
 * Created by chen on 5/30/16.
 */
public class LoginPresenterImpl implements LoginContract.PresenterOps, LoginContract.PresenterRequiredOps {


    private WeakReference<LoginContract.ViewRequiredOps> mView;
    private ModelContract.LoginModelOps mModel;

    public LoginPresenterImpl(LoginContract.ViewRequiredOps loginFrament) {
        this.mView = new WeakReference<LoginContract.ViewRequiredOps>(loginFrament);
        this.mModel = new LoginModelImpl(this);
    }

    @Override
    public void executeLogin(Activity activity, BeanInfoLogin infoLogin) {
        //如果处理数据需要activity, 将其传给model
        final LoginContract.ViewRequiredOps mViewRef = mView.get();
        if (mViewRef != null) {
            mViewRef.LoginExecuted(mModel.executeLoginda(infoLogin));
        }
    }

    @Override
    public void thirdLogin(Activity activity, int type) {
        final LoginContract.ViewRequiredOps mViewRef = mView.get();
        //判断是否有网络
        if (!UiHelper.isNetworkConnected(activity)) {
            if (mViewRef != null) {
                mViewRef.LoginExecuted("没有网络");
            }
            return;
        }

        mModel.LoginType(activity, type, new ModelContract.OnLoginListener() {
            @Override
            public void loginSuccess(String result) {
                if (mViewRef != null) {
                    mViewRef.LoginExecuted(result);
                }
            }

            @Override
            public void loginFail(String msg) {
                if (mViewRef != null) {
                    mViewRef.LoginExecuted(msg);
                }
            }
        });
    }


    @Override
    public TencentLoginUtils getTencentObj() {
        return mModel.getTencentObj();
    }

    @Override
    public SinaWeiboUtil getSinaObj() {
        return mModel.getSinaObj();
    }

    @Override
    public void loginInited(String msg) {
        final LoginContract.ViewRequiredOps mViewRef = mView.get();
        if (mViewRef != null) {
            mViewRef.LoginExecuted(msg);
        }
    }
}
