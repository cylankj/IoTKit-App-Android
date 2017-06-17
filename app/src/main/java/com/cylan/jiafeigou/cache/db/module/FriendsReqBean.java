package com.cylan.jiafeigou.cache.db.module;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.io.Serializable;

/**
 * 作者：zsl
 * 创建时间：2016/11/1
 * 描述：添加亲友请求类
 */
@Entity
public class FriendsReqBean implements Parcelable, Serializable {

    private static final long serialVersionUID = 1608319968864588726L;
    @Id
    public int id;
    public String iconUrl;              //头像
    public String account;              //账号
    public String alias;                //昵称
    public String sayHi;                //添加请求信息
    public long time;                   //添加请求时间

    public FriendsReqBean() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.iconUrl);
        dest.writeString(this.account);
        dest.writeString(this.alias);
        dest.writeString(this.sayHi);
        dest.writeLong(this.time);
    }

    protected FriendsReqBean(Parcel in) {
        this.iconUrl = in.readString();
        this.account = in.readString();
        this.alias = in.readString();
        this.sayHi = in.readString();
        this.time = in.readLong();
    }

    @Generated(hash = 1765491470)
    public FriendsReqBean(int id, String iconUrl, String account, String alias, String sayHi,
                          long time) {
        this.id = id;
        this.iconUrl = iconUrl;
        this.account = account;
        this.alias = alias;
        this.sayHi = sayHi;
        this.time = time;
    }

    public static final Creator<FriendsReqBean> CREATOR = new Creator<FriendsReqBean>() {
        @Override
        public FriendsReqBean createFromParcel(Parcel source) {
            return new FriendsReqBean(source);
        }

        @Override
        public FriendsReqBean[] newArray(int size) {
            return new FriendsReqBean[size];
        }
    };

    public void setId(int id) {
        this.id = id;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setSayHi(String sayHi) {
        this.sayHi = sayHi;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getAccount() {
        return account;
    }

    public String getAlias() {
        return alias;
    }

    public String getSayHi() {
        return sayHi;
    }

    public long getTime() {
        return time;
    }
}
