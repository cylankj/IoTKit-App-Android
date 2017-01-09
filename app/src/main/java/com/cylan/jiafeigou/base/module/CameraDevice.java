package com.cylan.jiafeigou.base.module;

/*
 *  @项目名：  JFGAndroid 
 *  @包名：    com.cylan.jiafeigou.base.module
 *  @文件名:   CameraDevice
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/8 11:16
 *  @描述：    TODO
 */


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
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_204_SDCARD_STORAGE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_206_BATTERY;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_209_LED_INDICATOR;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_210_UP_TIME;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_214_DEVICE_TIME_ZONE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_215_DEVICE_RTMP;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_216_DEVICE_VOLTAGE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_222_SDCARD_SUMMARY;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_301_DEVICE_MIC;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_302_DEVICE_SPEAKER;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_501_CAMERA_ALARM_FLAG;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_502_CAMERA_ALARM_INFO;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_503_CAMERA_ALARM_SENSITIVITY;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_506_CAMERA_TIME_LAPSE_PHOTOGRAPHY;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_508_CAMERA_STANDBY_FLAG;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_509_CAMERA_MOUNT_MODE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_510_CAMERA_COORDINATE;

public class CameraDevice extends JFGDevice {
    @DPProperty(msgId = ID_201_NET)
    public DpMsgDefine.MsgNet net;
    @DPProperty(msgId = ID_222_SDCARD_SUMMARY)
    public DpMsgDefine.SdcardSummary sdcard_summary;//DpMsgMap.SDCARD_SUMMARY_222
    @DPProperty(msgId = ID_204_SDCARD_STORAGE)
    public DpMsgDefine.SdStatus sdcard_storage;//DpMsgMap.SDCARD_STORAGE_204
    @DPProperty(msgId = ID_206_BATTERY)
    public DpMsgDefine.DPPrimary<Integer> battery;//DpMsgMap.BATTERY_206
    @DPProperty(msgId = ID_209_LED_INDICATOR)
    public DpMsgDefine.DPPrimary<Integer> led_indicator;//DpMsgMap.LED_INDICATOR_209
    @DPProperty(msgId = ID_210_UP_TIME)
    public DpMsgDefine.DPPrimary<Integer> up_time;//DpMsgMap.UP_TIME_210
    @DPProperty(msgId = ID_214_DEVICE_TIME_ZONE)
    public DpMsgDefine.MsgTimeZone device_time_zone;//DpMsgMap.DEVICE_TIME_ZONE_214
    @DPProperty(msgId = ID_215_DEVICE_RTMP)
    public DpMsgDefine.DPPrimary<Boolean> device_rtmp;//DpMsgMap.DEVICE_RTMP_215
    @DPProperty(msgId = ID_216_DEVICE_VOLTAGE)
    public DpMsgDefine.DPPrimary<Boolean> device_voltage;//DpMsgMap.DEVICE_VOLTAGE_216
    @DPProperty(msgId = ID_217_DEVICE_MOBILE_NET_PRIORITY)
    public DpMsgDefine.DPPrimary<Boolean> device_mobile_net_priority;//DpMsgMap.DEVICE_MOBILE_NET_PRIORITY_217
    @DPProperty(msgId = ID_301_DEVICE_MIC)
    public DpMsgDefine.DPPrimary<Boolean> device_mic;//DpMsgMap.DEVICE_MIC_301
    @DPProperty(msgId = ID_302_DEVICE_SPEAKER)
    public DpMsgDefine.DPPrimary<Integer> device_speaker;//DpMsgMap.DEVICE_SPEAKER_302
    @DPProperty(msgId = ID_303_DEVICE_AUTO_VIDEO_RECORD)
    public DpMsgDefine.DPPrimary<Integer> device_auto_video_record;//DpMsgMap.DEVICE_AUTO_VIDEO_RECORD_303
    @DPProperty(msgId = ID_304_DEVICE_CAMERA_ROTATE)
    public DpMsgDefine.DPPrimary<Integer> device_camera_rotate;//DpMsgMap.DEVICE_CAMERA_ROTATE_304
    @DPProperty(msgId = ID_501_CAMERA_ALARM_FLAG)
    public DpMsgDefine.DPPrimary<Boolean> camera_alarm_flag;//DpMsgMap.CAMERA_ALARM_FLAG_501
    @DPProperty(msgId = ID_502_CAMERA_ALARM_INFO)
    public DpMsgDefine.AlarmInfo camera_alarm_info;//DpMsgMap.CAMERA_ALARM_INFO_502
    @DPProperty(msgId = ID_503_CAMERA_ALARM_SENSITIVITY)
    public DpMsgDefine.DPPrimary<Integer> camera_alarm_sensitivity;//DpMsgMap.CAMERA_ALARM_SENSITIVITY_503
    @DPProperty(msgId = ID_504_CAMERA_ALARM_NOTIFICATION)
    public DpMsgDefine.NotificationInfo camera_alarm_notification;//DpMsgMap.CAMERA_ALARM_NOTIFICATION_504
    @DPProperty(msgId = ID_506_CAMERA_TIME_LAPSE_PHOTOGRAPHY)
    public DpMsgDefine.TimeLapse camera_time_lapse_photography;//DpMsgMap.CAMERA_TIME_LAPSE_PHOTOGRAPHY_506
    @DPProperty(msgId = ID_508_CAMERA_STANDBY_FLAG)
    public DpMsgDefine.DPPrimary<Boolean> camera_standby_flag;//DpMsgMap.CAMERA_STANDBY_FLAG_508
    @DPProperty(msgId = ID_509_CAMERA_MOUNT_MODE)
    public DpMsgDefine.DPPrimary<Integer> camera_mount_mode;//DpMsgMap.CAMERA_MOUNT_MODE_509
    @DPProperty(msgId = ID_510_CAMERA_COORDINATE)
    public DpMsgDefine.DPPrimary<Boolean> camera_coordinate;//DpMsgMap.CAMERA_COORDINATE_510

    @Override
    public ArrayList<JFGDPMsg> queryParameters(Map<Integer, Long> mapVersion) {
        DpParameters.Builder builder = new DpParameters.Builder();
        builder.addAll(super.queryParameters(mapVersion));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.NET_201), getVersion(mapVersion, 201));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.SDCARD_SUMMARY_222), getVersion(mapVersion, 203));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.SDCARD_STORAGE_204), getVersion(mapVersion, 204));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CHARGING_205), getVersion(mapVersion, 205));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.BATTERY_206), getVersion(mapVersion, 206));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.LED_INDICATOR_209), getVersion(mapVersion, 209));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.UP_TIME_210), getVersion(mapVersion, 210));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_TIME_ZONE_214), getVersion(mapVersion, 214));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_RTMP_215), getVersion(mapVersion, 215));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_VOLTAGE_216), getVersion(mapVersion, 216));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_MOBILE_NET_PRIORITY_217), getVersion(mapVersion, 217));

        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_MIC_301), getVersion(mapVersion, 301));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_SPEAKER_302), getVersion(mapVersion, 302));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_AUTO_VIDEO_RECORD_303), getVersion(mapVersion, 303));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_CAMERA_ROTATE_304), getVersion(mapVersion, 304));

        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_ALARM_FLAG_501), getVersion(mapVersion, 501));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_ALARM_INFO_502), getVersion(mapVersion, 502));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_ALARM_SENSITIVITY_503), getVersion(mapVersion, 503));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_ALARM_NOTIFICATION_504), getVersion(mapVersion, 504));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_TIME_LAPSE_PHOTOGRAPHY_506), getVersion(mapVersion, 506));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_STANDBY_FLAG_508), getVersion(mapVersion, 508));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_MOUNT_MODE_509), getVersion(mapVersion, 509));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_COORDINATE_510), getVersion(mapVersion, 510));
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

    public CameraDevice() {
        super();
    }

    protected CameraDevice(Parcel in) {
        super(in);
    }

    public static final Creator<CameraDevice> CREATOR = new Creator<CameraDevice>() {
        @Override
        public CameraDevice createFromParcel(Parcel source) {
            return new CameraDevice(source);
        }

        @Override
        public CameraDevice[] newArray(int size) {
            return new CameraDevice[size];
        }
    };
}
