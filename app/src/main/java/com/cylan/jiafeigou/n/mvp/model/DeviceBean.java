package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Pair;

import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DpMsgDefine;

import java.util.ArrayList;
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

    /**
     * 未读消息数量,时间戳
     */
    public Pair<Integer, BaseValue> msgCountPair;
    public List<DpMsgDefine.DpMsg> dataList;

    public DeviceBean() {
    }

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

    public void fillData(BaseBean baseDpMsg, List<DpMsgDefine.DpMsg> list) {
        this.uuid = baseDpMsg.uuid;
        this.pid = baseDpMsg.pid;
        this.sn = baseDpMsg.sn;
        this.shareAccount = baseDpMsg.shareAccount;
        this.alias = baseDpMsg.alias;
        this.dataList = list;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uuid);
        dest.writeString(this.sn);
        dest.writeString(this.alias);
        dest.writeString(this.shareAccount);
        dest.writeInt(this.pid);
        dest.writeInt(this.isChooseFlag);
        dest.writeInt(this.hasShareCount);
        dest.writeList(this.dataList);
    }

    protected DeviceBean(Parcel in) {
        this.uuid = in.readString();
        this.sn = in.readString();
        this.alias = in.readString();
        this.shareAccount = in.readString();
        this.pid = in.readInt();
        this.isChooseFlag = in.readInt();
        this.hasShareCount = in.readInt();
        this.dataList = new ArrayList<DpMsgDefine.DpMsg>();
        in.readList(this.dataList, DpMsgDefine.DpMsg.class.getClassLoader());
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