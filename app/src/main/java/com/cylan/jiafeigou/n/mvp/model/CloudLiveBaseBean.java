package com.cylan.jiafeigou.n.mvp.model;

import java.io.Serializable;

/**
 * 作者：zsl
 * 创建时间：2016/10/8
 * 描述：
 */
public class CloudLiveBaseBean implements Serializable{

    public void setType(int type) {
        this.type = type;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }

    public int getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public String getUserIcon() {
        return userIcon;
    }

    public int type;
    public Object data;
    public String userIcon;
}
