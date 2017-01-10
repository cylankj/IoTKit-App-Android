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
import com.cylan.jiafeigou.dp.DpMsgDefine;

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
    public DpMsgDefine.DPNet net;
    @DPProperty(msgId = ID_222_SDCARD_SUMMARY)
    public DpMsgDefine.DPSdcardSummary sdcard_summary;//DpMsgMap.SDCARD_SUMMARY_222
    @DPProperty(msgId = ID_204_SDCARD_STORAGE)
    public DpMsgDefine.DPSdStatus sdcard_storage;//DpMsgMap.SDCARD_STORAGE_204
    @DPProperty(msgId = ID_206_BATTERY)
    public DpMsgDefine.DPPrimary<Integer> battery;//DpMsgMap.BATTERY_206
    @DPProperty(msgId = ID_209_LED_INDICATOR)
    public DpMsgDefine.DPPrimary<Integer> led_indicator;//DpMsgMap.LED_INDICATOR_209
    @DPProperty(msgId = ID_210_UP_TIME)
    public DpMsgDefine.DPPrimary<Integer> up_time;//DpMsgMap.UP_TIME_210
    @DPProperty(msgId = ID_214_DEVICE_TIME_ZONE)
    public DpMsgDefine.DPTimeZone device_time_zone;//DpMsgMap.DEVICE_TIME_ZONE_214
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
    public DpMsgDefine.DPAlarmInfo camera_alarm_info;//DpMsgMap.CAMERA_ALARM_INFO_502
    @DPProperty(msgId = ID_503_CAMERA_ALARM_SENSITIVITY)
    public DpMsgDefine.DPPrimary<Integer> camera_alarm_sensitivity;//DpMsgMap.CAMERA_ALARM_SENSITIVITY_503
    @DPProperty(msgId = ID_504_CAMERA_ALARM_NOTIFICATION)
    public DpMsgDefine.DPNotificationInfo camera_alarm_notification;//DpMsgMap.CAMERA_ALARM_NOTIFICATION_504
    @DPProperty(msgId = ID_506_CAMERA_TIME_LAPSE_PHOTOGRAPHY)
    public DpMsgDefine.DPTimeLapse camera_time_lapse_photography;//DpMsgMap.CAMERA_TIME_LAPSE_PHOTOGRAPHY_506
    @DPProperty(msgId = ID_508_CAMERA_STANDBY_FLAG)
    public DpMsgDefine.DPPrimary<Boolean> camera_standby_flag;//DpMsgMap.CAMERA_STANDBY_FLAG_508
    @DPProperty(msgId = ID_509_CAMERA_MOUNT_MODE)
    public DpMsgDefine.DPPrimary<Integer> camera_mount_mode;//DpMsgMap.CAMERA_MOUNT_MODE_509
    @DPProperty(msgId = ID_510_CAMERA_COORDINATE)
    public DpMsgDefine.DPPrimary<Boolean> camera_coordinate;//DpMsgMap.CAMERA_COORDINATE_510

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
