// APT自动生成的文件,请勿修改!!!!
package com.cylan.jiafeigou.base.module;

import com.cylan.ext.annotations.DPType;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;

import static com.cylan.jiafeigou.dp.DpMsgDefine.DPNet;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPSdStatus;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPSdcardSummary;

public class JFGCameraDevice extends Device {
    @DPProperty(type = DPNet.class)
    public static final int NET = 201;

    @DPProperty(type = DPSdStatus.class)
    public static final int SDCARD_STORAGE = 204;

    @DPProperty(type = DPSdcardSummary.class, dpType = DPType.TYPE_SET)
    public static final int SDCARD_SUMMARY = 222;

    @DPProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int BATTERY = 206;

    @DPProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int UP_TIME = 210;

    @DPProperty(type = DpMsgDefine.DPTimeZone.class)
    public static final int DEVICE_TIME_ZONE = 214;

    @DPProperty(type = Boolean.class, dpType = DPType.TYPE_PRIMARY)
    public static final int DEVICE_RTMP = 215;

    @DPProperty(type = Boolean.class, dpType = DPType.TYPE_PRIMARY)
    public static final int DEVICE_VOLTAGE = 216;

    @DPProperty(type = Boolean.class, dpType = DPType.TYPE_PRIMARY)
    public static final int DEVICE_MOBILE_NET_PRIORITY = 217;

    @DPProperty(type = DpMsgDefine.DpSdcardFormatRsp.class)
    public static final int DEVICE_FORMAT_SDCARD = 218;

    @DPProperty(type = DpMsgDefine.DPBindLog.class)
    public static final int DEVICE_BIND_LOG = 219;

    @DPProperty(type = Boolean.class, dpType = DPType.TYPE_PRIMARY)
    public static final int DEVICE_MIC = 301;

    @DPProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int DEVICE_SPEAKER = 302;

    @DPProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int DEVICE_AUTO_VIDEO_RECORD = 303;

    @DPProperty(type = Boolean.class, dpType = DPType.TYPE_PRIMARY)
    public static final int CAMERA_ALARM_FLAG = 501;

    @DPProperty(type = DpMsgDefine.DPAlarmInfo.class)
    public static final int CAMERA_ALARM_INFO = 502;

    @DPProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int CAMERA_ALARM_SENSITIVITY = 503;

    @DPProperty(type = DpMsgDefine.DPNotificationInfo.class)
    public static final int CAMERA_ALARM_NOTIFICATION = 504;

    @DPProperty(type = DpMsgDefine.DPAlarm.class, dpType = DPType.TYPE_SET)
    public static final int CAMERA_ALARM_MSG = 505;

    @DPProperty(type = DpMsgDefine.DPTimeLapse.class)
    public static final int CAMERA_TIME_LAPSE_PHOTOGRAPHY = 506;

    @DPProperty(type = DpMsgDefine.DPStandby.class)
    public static final int CAMERA_STANDBY = 508;

    @DPProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int CAMERA_MOUNT_MODE = 509;

    @DPProperty(type = Boolean.class, dpType = DPType.TYPE_PRIMARY)
    public static final int CAMERA_COORDINATE = 510;

    @DPProperty(type = DpMsgDefine.DPAlarm.class, dpType = DPType.TYPE_SET)
    public static final int CAMERA_ALARM_MSG_V3 = 512;
}
