package com.cylan.jiafeigou.base.module;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.cylan.annotation.DPProperty;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_202_MAC;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_205_CHARGING;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_207_DEVICE_VERSION;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_208_DEVICE_SYS_VERSION;

/*
 *  @项目名：  JFGAndroid 
 *  @包名：    com.cylan.jiafeigou.base.module
 *  @文件名:   JFGDevice
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/8 11:03
 *  @描述：    TODO
 */
public abstract class JFGDevice extends DataPoint<JFGDevice> implements Parcelable {
    public String uuid;
    public String sn;
    public String alias;
    public String shareAccount;
    public int pid;

    @DPProperty(msgId = ID_202_MAC)
    public DpMsgDefine.DPPrimary<String> mac;//DpMsgMap.MAC_202
    @DPProperty(msgId = ID_207_DEVICE_VERSION)
    public DpMsgDefine.DPPrimary<String> device_version;//DpMsgMap.DEVICE_VERSION_207
    @DPProperty(msgId = ID_208_DEVICE_SYS_VERSION)
    public DpMsgDefine.DPPrimary<String> device_sys_version;//DpMsgMap.DEVICE_SYS_VERSION_208
    @DPProperty(msgId = ID_205_CHARGING)
    public DpMsgDefine.DPPrimary<Boolean> charging;//DpMsgMap.CHARGING_205


    JFGDevice() {
    }

    final JFGDevice setDevice(com.cylan.entity.jniCall.JFGDevice device) {
        this.alias = device.alias;
        this.uuid = device.uuid;
        this.sn = device.sn;
        this.shareAccount = device.shareAccount;
        this.pid = device.pid;

        //因为JFGDevice也被当做DataPoint对待的,所以把JFGDevice的pid当做他的id,
        // 把最后对他的修改当做他的version,seq暂时无用
        this.id = device.pid;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof JFGDevice) {
            return TextUtils.equals(uuid, ((JFGDevice) o).uuid);
        } else {
            return false;
        }
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
    }

    JFGDevice(Parcel in) {
        this.uuid = in.readString();
        this.sn = in.readString();
        this.alias = in.readString();
        this.shareAccount = in.readString();
        this.pid = in.readInt();
    }
}
