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
import java.util.Set;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_201_NET;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_206_BATTERY;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_301_DEVICE_MIC;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_302_DEVICE_SPEAKER;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_401_BELL_CALL_STATE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_402_BELL_VOICE_MSG;

/*
 *  @项目名：  JFGAndroid 
 *  @包名：    com.cylan.jiafeigou.base.module
 *  @文件名:   BellDevice
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/8 11:05
 *  @描述：    TODO
 */
public class BellDevice extends JFGDevice {

    @DPProperty(msgId = ID_201_NET)
    public DpMsgDefine.MsgNet net;//DpMsgMap.NET_201
    @DPProperty(msgId = ID_301_DEVICE_MIC)
    public DpMsgDefine.DPPrimary<Boolean> device_mic;//DpMsgMap.DEVICE_MIC_301
    @DPProperty(msgId = ID_302_DEVICE_SPEAKER)
    public DpMsgDefine.DPPrimary<Integer> device_speaker;//DpMsgMap.DEVICE_SPEAKER_302
    @DPProperty(msgId = ID_401_BELL_CALL_STATE, isSetType = true)
    public Set<DpMsgDefine.BellCallState> bell_call_state;//DpMsgMap.BELL_CALL_STATE_401 //集合类型的数据
    @DPProperty(msgId = ID_402_BELL_VOICE_MSG)
    public DpMsgDefine.DPPrimary<Integer> bell_voice_msg;//DpMsgMap.BELL_VOICE_MSG_402
    @DPProperty(msgId = ID_206_BATTERY)
    public DpMsgDefine.DPPrimary<Integer> battery;//DpMsgMap.BATTERY_206


    public BellDevice() {
    }

    @Override
    public ArrayList<JFGDPMsg> queryParameters(Map<Integer, Long> mapVersion) {
        DpParameters.Builder builder = new DpParameters.Builder();
        builder.addAll(super.queryParameters(mapVersion));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.NET_201), getVersion(mapVersion, 201));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_MIC_301), getVersion(mapVersion, 301));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_SPEAKER_302), getVersion(mapVersion, 302));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.BELL_CALL_STATE_401), getVersion(mapVersion, 401));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.BELL_VOICE_MSG_402), getVersion(mapVersion, 402));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.BATTERY_206), getVersion(mapVersion, 206));
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
    }

    protected BellDevice(Parcel in) {
        super(in);

    }

    public static final Creator<BellDevice> CREATOR = new Creator<BellDevice>() {
        @Override
        public BellDevice createFromParcel(Parcel source) {
            return new BellDevice(source);
        }

        @Override
        public BellDevice[] newArray(int size) {
            return new BellDevice[size];
        }
    };
}
