package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.cylan.jiafeigou.support.db.annotation.Column;
import com.cylan.jiafeigou.support.db.annotation.Table;

/**
 * 作者：zsl
 * 创建时间：2016/10/27
 * 描述：
 */
@Table(name = "RelAndFriendBean")
public class RelAndFriendBean implements Parcelable {
    @Column(name = "dpMsgId", isId = true)
    public int id;

    @Column(name = "iconUrl")
    public String iconUrl;

    @Column(name = "alias")
    public String alias;

    @Column(name = "account")
    public String account;

    @Column(name = "markName")
    public String markName;

    @Column(name = "isCheckFlag")
    public int isCheckFlag;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getMarkName() {
        return markName;
    }

    public void setMarkName(String markName) {
        this.markName = markName;
    }

    public int getIsCheckFlag() {
        return isCheckFlag;
    }

    public void setIsCheckFlag(int isCheckFlag) {
        this.isCheckFlag = isCheckFlag;
    }

    public static Creator<RelAndFriendBean> getCREATOR() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public RelAndFriendBean() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.iconUrl);
        dest.writeString(this.alias);
        dest.writeString(this.account);
        dest.writeString(this.markName);
        dest.writeInt(this.isCheckFlag);
    }

    protected RelAndFriendBean(Parcel in) {
        this.iconUrl = in.readString();
        this.alias = in.readString();
        this.account = in.readString();
        this.markName = in.readString();
    }

    public static final Creator<RelAndFriendBean> CREATOR = new Creator<RelAndFriendBean>() {
        @Override
        public RelAndFriendBean createFromParcel(Parcel source) {
            return new RelAndFriendBean(source);
        }

        @Override
        public RelAndFriendBean[] newArray(int size) {
            return new RelAndFriendBean[size];
        }
    };

}
