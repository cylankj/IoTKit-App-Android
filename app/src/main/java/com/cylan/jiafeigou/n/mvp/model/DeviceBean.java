package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.cylan.entity.jniCall.JFGDevBaseValue;

/**
 * Created by hunt on 16-5-14.
 */
public class DeviceBean extends JFGDevBaseValue implements Parcelable {

    public int id = 0;
    public int pid;//os type
    public int msgCount = 0;
    public int isProtectedMode = 0;
    public int isShared;
    public long msgTime;
    public String uuid = "";
    public String shareAccount = "";
    public String alias = "";

    public DeviceBean() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceBean that = (DeviceBean) o;

        if (!uuid.equals(that.uuid)) return false;
        if (!sn.equals(that.sn)) return false;
        return uuid.equals(that.uuid);

    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + sn.hashCode();
        result = 31 * result + uuid.hashCode();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.pid);
        dest.writeInt(this.msgCount);
        dest.writeInt(this.isProtectedMode);
        dest.writeInt(this.isShared);
        dest.writeLong(this.msgTime);
        dest.writeString(this.uuid);
        dest.writeString(this.shareAccount);
        dest.writeString(this.alias);
        dest.writeString(this.sn);
        dest.writeString(this.mac);
        dest.writeString(this.netName);
        dest.writeInt(this.netType);
        dest.writeByte(this.hasSDCard ? (byte) 1 : (byte) 0);
        dest.writeLong(this.sdcardTotalCapacity);
        dest.writeLong(this.sdcardUsedCapacity);
        dest.writeInt(this.sdcardErrorCode);
        dest.writeString(this.version);
        dest.writeString(this.sysVersion);
        dest.writeByte(this.charging ? (byte) 1 : (byte) 0);
        dest.writeInt(this.battery);
        dest.writeInt(this.ledModel);
        dest.writeInt(this.intTimeZone);
        dest.writeString(this.strTimeZone);
        dest.writeByte(this.priorityMobleNet ? (byte) 1 : (byte) 0);
    }

    protected DeviceBean(Parcel in) {
        this.id = in.readInt();
        this.pid = in.readInt();
        this.msgCount = in.readInt();
        this.isProtectedMode = in.readInt();
        this.isShared = in.readInt();
        this.msgTime = in.readLong();
        this.uuid = in.readString();
        this.shareAccount = in.readString();
        this.alias = in.readString();
        this.sn = in.readString();
        this.mac = in.readString();
        this.netName = in.readString();
        this.netType = in.readInt();
        this.hasSDCard = in.readByte() != 0;
        this.sdcardTotalCapacity = in.readLong();
        this.sdcardUsedCapacity = in.readLong();
        this.sdcardErrorCode = in.readInt();
        this.version = in.readString();
        this.sysVersion = in.readString();
        this.charging = in.readByte() != 0;
        this.battery = in.readInt();
        this.ledModel = in.readInt();
        this.intTimeZone = in.readInt();
        this.strTimeZone = in.readString();
        this.priorityMobleNet = in.readByte() != 0;
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
