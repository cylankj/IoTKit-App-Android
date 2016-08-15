package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hunt on 16-5-14.
 */
public class DeviceBean implements Parcelable {

    public int id = 0;
    public int deviceType;
    //-1:offline
    public int netType = -1;
    public int msgCount = 0;
    public int battery;
    public int isProtectedMode = 0;
    public int isShared;
    public long msgTime;
    public String cid = "";
    public String alias = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.deviceType);
        dest.writeInt(this.netType);
        dest.writeInt(this.msgCount);
        dest.writeInt(this.battery);
        dest.writeInt(this.isProtectedMode);
        dest.writeInt(this.isShared);
        dest.writeLong(this.msgTime);
        dest.writeString(this.cid);
        dest.writeString(this.alias);
    }

    public DeviceBean() {
    }

    protected DeviceBean(Parcel in) {
        this.id = in.readInt();
        this.deviceType = in.readInt();
        this.netType = in.readInt();
        this.msgCount = in.readInt();
        this.battery = in.readInt();
        this.isProtectedMode = in.readInt();
        this.isShared = in.readInt();
        this.msgTime = in.readLong();
        this.cid = in.readString();
        this.alias = in.readString();
    }

    public static final Creator<DeviceBean> CREATOR = new Creator<DeviceBean>() {
        @Override
        public DeviceBean createFromParcel(Parcel source) {
            return new DeviceBean(source);
        }

        @Override
        public DeviceBean[] newArray(int size) {
            return new DeviceBean[size];
        }
    };
}
