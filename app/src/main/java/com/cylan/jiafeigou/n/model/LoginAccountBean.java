package com.cylan.jiafeigou.n.model;

/**
 * Created by chen on 5/30/16.
 * modify by lxh on 6/25/16
 */
public class LoginAccountBean {
    public String userName;

    public String pwd;

    public String session; // relogin 用的，和登陆成功后的返回
    /**
     * 登陆后的结果，0为成功，
     */
    public int ret; //

}
