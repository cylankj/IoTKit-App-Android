package com.cylan.jiafeigou.n.mvp.model;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public class RequestResetPwdBean {
    public int ret;
    public String content;

    public RequestResetPwdBean() {
    }

    public RequestResetPwdBean(int ret, String content) {
        this.ret = ret;
        this.content = content;
    }
}
