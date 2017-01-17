package com.cylan.jiafeigou;

import com.cylan.annotation.DPMessage;
import com.cylan.jiafeigou.dp.DpMsgDefine;

import static com.cylan.annotation.DPTarget.ACCOUNT;
import static com.cylan.annotation.DPTarget.CAMERA;
import static com.cylan.annotation.DPTarget.DEVICE;
import static com.cylan.annotation.DPTarget.DOORBELL;
import static com.cylan.annotation.DPTarget.EFAMILY;
import static com.cylan.annotation.DPTarget.MAGNETOMETER;
import static com.cylan.annotation.DPType.TYPE_PRIMARY;
import static com.cylan.annotation.DPType.TYPE_SET;

/**
 * Created by yzd on 17-1-14.
 */

public class DPConstant {
    @DPMessage(name = "net", primaryType = DpMsgDefine.DPNet.class, target = {CAMERA, DOORBELL, EFAMILY})
    public static final int NET = 201;

    @DPMessage(name = "mac", primaryType = String.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int MAC = 202;

    @DPMessage(name = "sdcard_storage", primaryType = DpMsgDefine.DPSdStatus.class)
    public static final int SDCARD_STORAGE = 204;

    @DPMessage(name = "sdcard_summary", primaryType = DpMsgDefine.DPSdcardSummary.class)
    public static final int SDCARD_SUMMARY = 222;

    @DPMessage(name = "charging", primaryType = Boolean.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int CHARGING = 205;//充电中。。。

    @DPMessage(name = "battery", primaryType = Integer.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL, EFAMILY})
    public static final int BATTERY = 206;

    @DPMessage(name = "device_version", primaryType = String.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int DEVICE_VERSION = 207;

    @DPMessage(name = "device_sys_version", primaryType = String.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int DEVICE_SYS_VERSION = 208;

    @DPMessage(name = "led_indicator", primaryType = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int LED_INDICATOR = 209;

    @DPMessage(name = "up_time", primaryType = Integer.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL, EFAMILY})
    public static final int UP_TIME = 210;

    @DPMessage(name = "app_upload_log", primaryType = Integer.class, dpType = TYPE_PRIMARY, target = {DEVICE})
    public static final int APP_UPLOAD_LOG = 211;

    @DPMessage(name = "device_upload_log", primaryType = String.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int DEVICE_UPLOAD_LOG = 212;

    @DPMessage(name = "device_p2p_version", primaryType = Integer.class, dpType = TYPE_PRIMARY, target = {DEVICE})
    public static final int DEVICE_P2P_VERSION = 213;

    @DPMessage(name = "device_time_zone", primaryType = DpMsgDefine.DPTimeZone.class, target = {CAMERA, DOORBELL, EFAMILY})
    public static final int DEVICE_TIME_ZONE = 214;

    @DPMessage(name = "device_rtmp", primaryType = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int DEVICE_RTMP = 215;

    @DPMessage(name = "device_voltage", primaryType = Boolean.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL})
    public static final int DEVICE_VOLTAGE = 216;

    @DPMessage(name = "device_mobile_net_priority", primaryType = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int DEVICE_MOBILE_NET_PRIORITY = 217;

    @DPMessage(name = "device_format_sdcard", primaryType = Void.class, dpType = TYPE_PRIMARY)
    public static final int DEVICE_FORMAT_SDCARD = 218;

    @DPMessage(name = "device_bind_log", primaryType = DpMsgDefine.DPBindLog.class, target = {CAMERA, DOORBELL})
    public static final int DEVICE_BIND_LOG = 219;

    @DPMessage(name = "sdk_version", primaryType = String.class, dpType = TYPE_PRIMARY, target = {DEVICE})
    public static final int SDK_VERSION = 220;

    @DPMessage(name = "device_mic", primaryType = Boolean.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL})
    public static final int DEVICE_MIC = 301;//301

    @DPMessage(name = "device_speaker", primaryType = Integer.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL})
    public static final int DEVICE_SPEAKER = 302;//302

    @DPMessage(name = "device_auto_video_record", primaryType = Integer.class, dpType = TYPE_PRIMARY)
    public static final int DEVICE_AUTO_VIDEO_RECORD = 303;//303

    @DPMessage(name = "device_camera_rotate", primaryType = Integer.class, dpType = TYPE_PRIMARY)
    public static final int DEVICE_CAMERA_ROTATE = 304;//304

    @DPMessage(name = "bell_call_state", primaryType = DpMsgDefine.DPBellCallRecord.class, dpType = TYPE_SET, target = {DOORBELL, MAGNETOMETER})
    public static final int BELL_CALL_STATE = 401;//门铃呼叫状态

    @DPMessage(name = "bell_voice_msg", primaryType = Integer.class, dpType = TYPE_PRIMARY, target = {DOORBELL, MAGNETOMETER})
    public static final int BELL_VOICE_MSG = 402;//门铃呼叫状态

    @DPMessage(name = "camera_alarm_flag", primaryType = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int CAMERA_ALARM_FLAG = 501;

    @DPMessage(name = "camera_alarm_info", primaryType = DpMsgDefine.DPAlarmInfo.class)
    public static final int CAMERA_ALARM_INFO = 502;

    @DPMessage(name = "camera_alarm_sensitivity", primaryType = Integer.class, dpType = TYPE_PRIMARY)
    public static final int CAMERA_ALARM_SENSITIVITY = 503;

    @DPMessage(name = "camera_alarm_notification", primaryType = DpMsgDefine.DPNotificationInfo.class)
    public static final int CAMERA_ALARM_NOTIFICATION = 504;//报警音效

    @DPMessage(name = "camera_alarm_msg", primaryType = DpMsgDefine.DPAlarm.class, dpType = TYPE_SET)
    public static final int CAMERA_ALARM_MSG = 505;//

    @DPMessage(name = "camera_time_lapse_photography", primaryType = DpMsgDefine.DPTimeLapse.class)
    public static final int CAMERA_TIME_LAPSE_PHOTOGRAPHY = 506;//

    @DPMessage(name = "camera_standby_flag", primaryType = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int CAMERA_STANDBY_FLAG = 508;//是否开启直播，待机模式

    @DPMessage(name = "camera_mount_mode", primaryType = Integer.class, dpType = TYPE_PRIMARY)
    public static final int CAMERA_MOUNT_MODE = 509;//针对全景摄像头，吊顶，挂壁

    @DPMessage(name = "camera_coordinate", primaryType = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int CAMERA_COORDINATE = 510;//视频坐标

    @DPMessage(name = "account_state", primaryType = String.class, dpType = TYPE_PRIMARY, target = ACCOUNT)
    public static final int ACCOUNT_STATE = 601;//绑定、解绑消息

    @DPMessage(name = "account_wonderful_msg", primaryType = DpMsgDefine.DPWonderItem.class, dpType = TYPE_SET, target = ACCOUNT)
    public static final int ACCOUNT_WONDERFUL_MSG = 602;//每日精彩消息

    @DPMessage(name = "sys_push_flag", primaryType = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int SYS_PUSH_FLAG = 701;
}
