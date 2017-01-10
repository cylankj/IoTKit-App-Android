package com.cylan.jiafeigou.base.module;

import android.os.Parcel;

import com.cylan.annotation.DPProperty;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpParameters;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.Map;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_201_NET;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_401_BELL_CALL_STATE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_402_BELL_VOICE_MSG;

/*
 *  @项目名：  JFGAndroid 
 *  @包名：    com.cylan.jiafeigou.base.module
 *  @文件名:   EfamilyDevice
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/8 11:36
 *  @描述：    TODO
 */
public class EfamilyDevice extends JFGDevice {
    @DPProperty(msgId = ID_201_NET)
    public DpMsgDefine.MsgNet net;
    @DPProperty(msgId = ID_401_BELL_CALL_STATE, isSetType = true)
    public DpMsgDefine.BellCallState bell_call_state;
    @DPProperty(msgId = ID_402_BELL_VOICE_MSG)
    public DpMsgDefine.DPPrimary<Integer> bell_voice_msg;

    public EfamilyDevice() {
    }

    @Override
    public ArrayList<JFGDPMsg> queryParameters(Map<Integer, Long> mapVersion) {
        DpParameters.Builder builder = new DpParameters.Builder();
        builder.addAll(super.queryParameters(mapVersion));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.NET_201), getVersion(mapVersion, 201));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.BELL_CALL_STATE_401), getVersion(mapVersion, 401));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.BELL_VOICE_MSG_402), getVersion(mapVersion, 402));
        AppLogger.i("req:" + builder.toString());
        return builder.build();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.net, flags);
        dest.writeParcelable(this.bell_call_state, flags);
    }

    protected EfamilyDevice(Parcel in) {
        super(in);
        this.net = in.readParcelable(DpMsgDefine.MsgNet.class.getClassLoader());
        this.bell_call_state = in.readParcelable(DpMsgDefine.BellCallState.class.getClassLoader());
    }

    public static final Creator<EfamilyDevice> CREATOR = new Creator<EfamilyDevice>() {
        @Override
        public EfamilyDevice createFromParcel(Parcel source) {
            return new EfamilyDevice(source);
        }

        @Override
        public EfamilyDevice[] newArray(int size) {
            return new EfamilyDevice[size];
        }
    };
}
