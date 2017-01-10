package com.cylan.jiafeigou.n.mvp.model;

import java.io.Serializable;

/**
 * 作者：zsl
 * 创建时间：2016/9/12
 * 描述：
 */
public class UserInfoBean implements Serializable {
    private int flag;
    public String account;
    public String name;
    public String token;
    public String phone;
    public String email;
    public boolean enableVibrate;
    public boolean enablePush;
    public boolean enableSound;
    public int photo;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }

    public String photoUrl;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccount() {
        return account;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}
