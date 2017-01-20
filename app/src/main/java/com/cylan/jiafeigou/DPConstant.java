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
    @DPMessage(type = DpMsgDefine.DPNet.class, target = {CAMERA, DOORBELL, EFAMILY})
    public static final int NET = 201;

    @DPMessage(type = String.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int MAC = 202;

    @DPMessage(type = DpMsgDefine.DPSdStatus.class)
    public static final int SDCARD_STORAGE = 204;

    @DPMessage(type = DpMsgDefine.DPSdcardSummary.class)
    public static final int SDCARD_SUMMARY = 222;

    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int CHARGING = 205;//充电中。。。

    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL, EFAMILY})
    public static final int BATTERY = 206;

    @DPMessage(type = String.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int DEVICE_VERSION = 207;

    @DPMessage(type = String.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int DEVICE_SYS_VERSION = 208;

    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int LED_INDICATOR = 209;

    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL, EFAMILY})
    public static final int UP_TIME = 210;

    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY, target = {DEVICE})
    public static final int APP_UPLOAD_LOG = 211;

    @DPMessage(type = String.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int DEVICE_UPLOAD_LOG = 212;

    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY, target = {DEVICE})
    public static final int DEVICE_P2P_VERSION = 213;

    @DPMessage(type = DpMsgDefine.DPTimeZone.class, target = {CAMERA, DOORBELL, EFAMILY})
    public static final int DEVICE_TIME_ZONE = 214;

    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int DEVICE_RTMP = 215;

    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL})
    public static final int DEVICE_VOLTAGE = 216;

    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int DEVICE_MOBILE_NET_PRIORITY = 217;

    @DPMessage(type = Void.class, dpType = TYPE_PRIMARY)
    public static final int DEVICE_FORMAT_SDCARD = 218;

    @DPMessage(type = DpMsgDefine.DPBindLog.class, target = {CAMERA, DOORBELL})
    public static final int DEVICE_BIND_LOG = 219;

    @DPMessage(type = String.class, dpType = TYPE_PRIMARY, target = {DEVICE})
    public static final int SDK_VERSION = 220;

    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL})
    public static final int DEVICE_MIC = 301;//301

    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL})
    public static final int DEVICE_SPEAKER = 302;//302

    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY)
    public static final int DEVICE_AUTO_VIDEO_RECORD = 303;//303

    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY)
    public static final int DEVICE_CAMERA_ROTATE = 304;//304

    @DPMessage(type = DpMsgDefine.DPBellCallRecord.class, dpType = TYPE_SET, target = {DOORBELL, MAGNETOMETER})
    public static final int BELL_CALL_STATE = 401;//门铃呼叫状态

    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY, target = {DOORBELL, MAGNETOMETER})
    public static final int BELL_VOICE_MSG = 402;//门铃呼叫状态

    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int CAMERA_ALARM_FLAG = 501;

    @DPMessage(type = DpMsgDefine.DPAlarmInfo.class)
    public static final int CAMERA_ALARM_INFO = 502;

    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY)
    public static final int CAMERA_ALARM_SENSITIVITY = 503;

    @DPMessage(type = DpMsgDefine.DPNotificationInfo.class)
    public static final int CAMERA_ALARM_NOTIFICATION = 504;//报警音效

    @DPMessage(type = DpMsgDefine.DPAlarm.class, dpType = TYPE_SET)
    public static final int CAMERA_ALARM_MSG = 505;//

    @DPMessage(type = DpMsgDefine.DPTimeLapse.class)
    public static final int CAMERA_TIME_LAPSE_PHOTOGRAPHY = 506;//

    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int CAMERA_STANDBY_FLAG = 508;//是否开启直播，待机模式

    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY)
    public static final int CAMERA_MOUNT_MODE = 509;//针对全景摄像头，吊顶，挂壁

    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int CAMERA_COORDINATE = 510;//视频坐标

    @DPMessage(type = String.class, dpType = TYPE_PRIMARY, target = ACCOUNT)
    public static final int ACCOUNT_STATE = 601;//绑定、解绑消息

    @DPMessage(type = DpMsgDefine.DPWonderItem.class, dpType = TYPE_SET, target = ACCOUNT)
    public static final int ACCOUNT_WONDERFUL_MSG = 602;//每日精彩消息

    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int SYS_PUSH_FLAG = 701;
}
