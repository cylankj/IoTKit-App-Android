package com.cylan.jiafeigou.n.mvp.model;

import java.io.Serializable;

/**
 * 作者：zsl
 * 创建时间：2016/11/1
 * 描述：添加亲友请求类
 */
public class MineAddReqBean implements Serializable{

    public String iconUrl;              //头像
    public String account;              //账号
    public String alias;                //昵称
    public String sayHi;                //添加请求信息
    public long time;                   //添加请求时间

    public MineAddReqBean() {
    }
}
