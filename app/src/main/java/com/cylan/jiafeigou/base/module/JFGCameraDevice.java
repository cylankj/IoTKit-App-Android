// APT自动生成的文件,请勿修改!!!!
package com.cylan.jiafeigou.base.module;

import com.cylan.ext.annotations.DPType;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;

import static com.cylan.jiafeigou.dp.DpMsgDefine.DPNet;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPSdStatus;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPSdcardSummary;

public class JFGCameraDevice extends Device {
    @DProperty(type = DPNet.class)
    public static final int NET = 201;

    @DProperty(type = DPSdStatus.class)
    public static final int SDCARD_STORAGE = 204;

    @DProperty(type = DPSdcardSummary.class, dpType = DPType.TYPE_SET)
    public static final int SDCARD_SUMMARY = 222;

    @DProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int BATTERY = 206;

    @DProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int UP_TIME = 210;

    @DProperty(type = DpMsgDefine.DPTimeZone.class)
    public static final int DEVICE_TIME_ZONE = 214;

    @DProperty(type = Boolean.class, dpType = DPType.TYPE_PRIMARY)
    public static final int DEVICE_RTMP = 215;

    @DProperty(type = Boolean.class, dpType = DPType.TYPE_PRIMARY)
    public static final int DEVICE_VOLTAGE = 216;

    @DProperty(type = Boolean.class, dpType = DPType.TYPE_PRIMARY)
    public static final int DEVICE_MOBILE_NET_PRIORITY = 217;

    @DProperty(type = DpMsgDefine.DpSdcardFormatRsp.class)
    public static final int DEVICE_FORMAT_SDCARD = 218;

    @DProperty(type = DpMsgDefine.DPBindLog.class)
    public static final int DEVICE_BIND_LOG = 219;

    @DProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int MOBILE_NET = 223;

    @DProperty(type = Boolean.class, dpType = DPType.TYPE_PRIMARY)
    public static final int DEVICE_MIC = 301;

    @DProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int DEVICE_SPEAKER = 302;

    @DProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int DEVICE_AUTO_VIDEO_RECORD = 303;

    @DProperty(type = Boolean.class, dpType = DPType.TYPE_PRIMARY)
    public static final int CAMERA_ALARM_FLAG = 501;

    @DProperty(type = DpMsgDefine.DPAlarmInfo.class)
    public static final int CAMERA_ALARM_INFO = 502;

    @DProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int CAMERA_ALARM_SENSITIVITY = 503;

    @DProperty(type = DpMsgDefine.DPNotificationInfo.class)
    public static final int CAMERA_ALARM_NOTIFICATION = 504;

    @DProperty(type = DpMsgDefine.DPAlarm.class, dpType = DPType.TYPE_SET)
    public static final int CAMERA_ALARM_MSG = 505;

    @DProperty(type = DpMsgDefine.DPTimeLapse.class)
    public static final int CAMERA_TIME_LAPSE_PHOTOGRAPHY = 506;

    @DProperty(type = DpMsgDefine.DPStandby.class)
    public static final int CAMERA_STANDBY = 508;

    @DProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int CAMERA_MOUNT_MODE = 509;

    @DProperty(type = Boolean.class, dpType = DPType.TYPE_PRIMARY)
    public static final int CAMERA_COORDINATE = 510;

    @DProperty(type = DpMsgDefine.DPAlarm.class, dpType = DPType.TYPE_SET)
    public static final int CAMERA_ALARM_MSG_V3 = 512;

}
