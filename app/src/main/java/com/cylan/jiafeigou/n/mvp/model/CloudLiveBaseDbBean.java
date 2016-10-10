package com.cylan.jiafeigou.n.mvp.model;

/**
 * 作者：zsl
 * 创建时间：2016/10/10
 * 描述：
 */
public class CloudLiveBaseDbBean {


    public int id;
    public int type;
    public byte[] data;
    public String userIcon;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
