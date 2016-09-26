package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * 作者：zsl
 * 创建时间：2016/9/24
 * 描述：
 */
public class MineShareDeviceBean implements Serializable {
    public String iconUrl;
    public int shareNumber;
    public String deviceName;

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setShareNumber(int shareNumber) {
        this.shareNumber = shareNumber;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public boolean isCheck;

    public int getShareNumber() {
        return shareNumber;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public boolean isCheck() {
        return isCheck;
    }

}
