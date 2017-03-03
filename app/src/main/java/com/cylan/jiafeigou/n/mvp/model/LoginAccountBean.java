package com.cylan.jiafeigou.n.mvp.model;

/**
 * Created by chen on 5/30/16.
 * perform by lxh on 6/25/16
 */
public class LoginAccountBean {


    public String userName;

    public String pwd;

    public String session; // relogin 用的，和登陆成功后的返回
    /**
     * 登陆后的结果，0为成功，
     */
    public int code; //

    public int event;

    public boolean loginType;  //是否三方登录标记

    public int openLoginType;  //三方登录方式的区分

    @Override
    public String toString() {
        return "LoginAccountBean{" +
                "userName='" + userName + '\'' +
                ", pwd='" + pwd + '\'' +
                ", session='" + session + '\'' +
                ", code=" + code +
                ", event=" + event +
                '}';
    }
}
