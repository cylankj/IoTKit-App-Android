package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 作者：zsl
 * 创建时间：2016/10/27
 * 描述：
 */
public class RelAndFriendBean implements Parcelable {

    public String iconHead;
    public String alids;
    public String account;

    @Override
    public int describeContents() {
        return 0;
    }

    public RelAndFriendBean() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.iconHead);
        dest.writeString(this.alids);
        dest.writeString(this.account);
    }

    protected RelAndFriendBean(Parcel in) {
        this.iconHead = in.readString();
        this.alids = in.readString();
        this.account = in.readString();
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
