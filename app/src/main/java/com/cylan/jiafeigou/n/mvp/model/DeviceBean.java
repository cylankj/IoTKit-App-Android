package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.cylan.jiafeigou.dp.DpMsgDefine;

import java.util.List;

/**
 * Created by hunt on 16-5-14.
 */
public class DeviceBean implements Parcelable {
    public String uuid;
    public String sn;
    public String alias;
    public String shareAccount;
    public int pid;
    public int isChooseFlag;
    public int hasShareCount;

    public List<DpMsgDefine.BaseDpMsg> dataList;

    public DeviceBean() {
    }

    protected DeviceBean(Parcel in) {
        uuid = in.readString();
        sn = in.readString();
        alias = in.readString();
        shareAccount = in.readString();
        pid = in.readInt();
        isChooseFlag = in.readInt();
    }

    public static final Creator<DeviceBean> CREATOR = new Creator<DeviceBean>() {
        @Override
        public DeviceBean createFromParcel(Parcel in) {
            return new DeviceBean(in);
        }

        @Override
        public DeviceBean[] newArray(int size) {
            return new DeviceBean[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceBean bean = (DeviceBean) o;

        return TextUtils.equals(uuid, bean.uuid);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }

    public void fillData(DpMsgDefine.BaseDpDevice baseDpMsg, List<DpMsgDefine.BaseDpMsg> list) {
        this.uuid = baseDpMsg.uuid;
        this.pid = baseDpMsg.pid;
        this.sn = baseDpMsg.sn;
        this.shareAccount = baseDpMsg.shareAccount;
        this.alias = baseDpMsg.alias;
        this.dataList = list;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(sn);
        dest.writeString(alias);
        dest.writeString(shareAccount);
        dest.writeInt(pid);
        dest.writeInt(isChooseFlag);
    }

    @Override
    public String toString() {
        return "DeviceBean{" +
                "uuid='" + uuid + '\'' +
                ", sn='" + sn + '\'' +
                ", alias='" + alias + '\'' +
                ", shareAccount='" + shareAccount + '\'' +
                ", pid=" + pid +
                ", isChooseFlag=" + isChooseFlag +
                ", dataList=" + dataList +
                '}';
    }
}