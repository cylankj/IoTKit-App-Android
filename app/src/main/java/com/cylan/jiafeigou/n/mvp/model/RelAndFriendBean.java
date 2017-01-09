package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 作者：zsl
 * 创建时间：2016/10/27
 * 描述：
 */
public class RelAndFriendBean implements Parcelable {

    public String iconUrl;
    public String alias;
    public String account;
    public String markName;
    public int isCheckFlag;

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
