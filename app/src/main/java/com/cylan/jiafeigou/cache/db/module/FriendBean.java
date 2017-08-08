package com.cylan.jiafeigou.cache.db.module;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;


/**
 * 作者：zsl
 * 创建时间：2016/10/27
 * 描述：
 */
@Entity
public class FriendBean implements Parcelable {

    @Id
    public Long id;
    public String iconUrl;
    public String alias;
    public String account;
    public String markName;
    public int isCheckFlag;
    public String sortkey;

    public String getSortkey() {
        return sortkey;
    }

    public void setSortkey(String sortkey) {
        this.sortkey = sortkey;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public static Creator<FriendBean> getCREATOR() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public FriendBean() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.iconUrl);
        dest.writeString(this.alias);
        dest.writeString(this.account);
        dest.writeString(this.markName);
        dest.writeInt(this.isCheckFlag);
    }

    protected FriendBean(Parcel in) {
        this.iconUrl = in.readString();
        this.alias = in.readString();
        this.account = in.readString();
        this.markName = in.readString();
    }

    @Generated(hash = 1327211143)
    public FriendBean(Long id, String iconUrl, String alias, String account,
                      String markName, int isCheckFlag, String sortkey) {
        this.id = id;
        this.iconUrl = iconUrl;
        this.alias = alias;
        this.account = account;
        this.markName = markName;
        this.isCheckFlag = isCheckFlag;
        this.sortkey = sortkey;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FriendBean)) return false;
        FriendBean bean = (FriendBean) obj;
        return TextUtils.equals(bean.account, account);
    }


    public static final Creator<FriendBean> CREATOR = new Creator<FriendBean>() {
        @Override
        public FriendBean createFromParcel(Parcel source) {
            return new FriendBean(source);
        }

        @Override
        public FriendBean[] newArray(int size) {
            return new FriendBean[size];
        }
    };

}
