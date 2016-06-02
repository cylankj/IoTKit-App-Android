package com.cylan.jiafeigou.n.model.impl;

import com.cylan.jiafeigou.n.model.contract.ModelContract;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginContract;
import com.cylan.jiafeigou.n.view.login.InfoLogin;

/**
 * Created by chen on 5/30/16.
 */
public class LoginModelImpl implements ModelContract.LoginModelOps {
    public LoginModelImpl(LoginContract.PresenterRequiredOps loginPresenter) {

    }

    @Override
    public String executeLoginda(InfoLogin infoLogin) {
        if (infoLogin == null)
            return "请初始化登陆类";
        else if (infoLogin.Usename == null)
            return "请输入用户名";
        else if (infoLogin.Passwd    == null)
            return "请输入密码";

        //开始执行登陆请求
        return "succeed";
    }
}
