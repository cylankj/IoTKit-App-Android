package com.cylan.jiafeigou.base.module;

import com.cylan.ext.annotations.DPMessage;
import com.cylan.ext.annotations.DpAnnotation;
import com.cylan.jiafeigou.dp.DpMsgDefine;

import static com.cylan.ext.annotations.DPTarget.ACCOUNT;
import static com.cylan.ext.annotations.DPTarget.CAMERA;
import static com.cylan.ext.annotations.DPTarget.DEVICE;
import static com.cylan.ext.annotations.DPTarget.DOORBELL;
import static com.cylan.ext.annotations.DPTarget.EFAMILY;
import static com.cylan.ext.annotations.DPTarget.MAGNETOMETER;
import static com.cylan.ext.annotations.DPType.TYPE_PRIMARY;
import static com.cylan.ext.annotations.DPType.TYPE_SET;


/**
 * Created by yzd on 17-1-14.
 */

public class DPConstant {
    @DpAnnotation(msgId = 201, clazz = DpMsgDefine.DPNet.class)
    @DPMessage(type = DpMsgDefine.DPNet.class, target = {CAMERA, DOORBELL, EFAMILY})
    public static final int NET = 201;

    @DpAnnotation(msgId = 202, clazz = String.class)
    @DPMessage(type = String.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int MAC = 202;

    @DpAnnotation(msgId = 204, clazz = DpMsgDefine.DPSdStatus.class)
    @DPMessage(type = DpMsgDefine.DPSdStatus.class, target = CAMERA)
    public static final int SDCARD_STORAGE = 204;

    @DpAnnotation(msgId = 222, clazz = DpMsgDefine.DPSdcardSummary.class)
    @DPMessage(type = DpMsgDefine.DPSdcardSummary.class, target = CAMERA, dpType = TYPE_SET)
    public static final int SDCARD_SUMMARY = 222;

    @DpAnnotation(msgId = 205, clazz = boolean.class)
    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int CHARGING = 205;//充电中。。。

    @DpAnnotation(msgId = 206, clazz = int.class)
    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL, EFAMILY})
    public static final int BATTERY = 206;

    @DpAnnotation(msgId = 207, clazz = String.class)
    @DPMessage(type = String.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int DEVICE_VERSION = 207;

    @DpAnnotation(msgId = 208, clazz = String.class)
    @DPMessage(type = String.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int DEVICE_SYS_VERSION = 208;

    @DpAnnotation(msgId = 209, clazz = boolean.class)
    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int LED_INDICATOR = 209;

    @DpAnnotation(msgId = 210, clazz = int.class)
    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL, EFAMILY})
    public static final int UP_TIME = 210;

    @DpAnnotation(msgId = 211, clazz = int.class)
    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY, target = {DEVICE})
    public static final int APP_UPLOAD_LOG = 211;

    @DpAnnotation(msgId = 212, clazz = String.class)
    @DPMessage(type = String.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int DEVICE_UPLOAD_LOG = 212;

    @DpAnnotation(msgId = 213, clazz = int.class)
    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY, target = {DEVICE})
    public static final int DEVICE_P2P_VERSION = 213;

    @DpAnnotation(msgId = 214, clazz = DpMsgDefine.DPTimeZone.class)
    @DPMessage(type = DpMsgDefine.DPTimeZone.class, target = {CAMERA, DOORBELL, EFAMILY})
    public static final int DEVICE_TIME_ZONE = 214;

    @DpAnnotation(msgId = 215, clazz = boolean.class)
    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int DEVICE_RTMP = 215;

    @DpAnnotation(msgId = 216, clazz = boolean.class)
    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL})
    public static final int DEVICE_VOLTAGE = 216;

    @DpAnnotation(msgId = 217, clazz = boolean.class)
    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY)
    public static final int DEVICE_MOBILE_NET_PRIORITY = 217;

    @DpAnnotation(msgId = 218, clazz = int.class)
    @DPMessage(type = int.class, dpType = TYPE_PRIMARY, target = {CAMERA})
    public static final int DEVICE_FORMAT_SDCARD = 218;

    @DpAnnotation(msgId = 219, clazz = DpMsgDefine.DPBindLog.class)
    @DPMessage(type = DpMsgDefine.DPBindLog.class, target = {CAMERA, DOORBELL})
    public static final int DEVICE_BIND_LOG = 219;


    @DpAnnotation(msgId = 220, clazz = String.class)
    @DPMessage(type = String.class, dpType = TYPE_PRIMARY, target = {DEVICE})
    public static final int SDK_VERSION = 220;


    @DpAnnotation(msgId = 301, clazz = boolean.class)
    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL})
    public static final int DEVICE_MIC = 301;//301


    @DpAnnotation(msgId = 302, clazz = int.class)
    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY, target = {CAMERA, DOORBELL})
    public static final int DEVICE_SPEAKER = 302;//302


    @DpAnnotation(msgId = 303, clazz = int.class)
    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY, target = CAMERA)
    public static final int DEVICE_AUTO_VIDEO_RECORD = 303;//303


    @DpAnnotation(msgId = 304, clazz = int.class)
    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY, target = DEVICE)
    public static final int DEVICE_CAMERA_ROTATE = 304;//304


    @DpAnnotation(msgId = 401, clazz = DpMsgDefine.DPBellCallRecord.class)
    @DPMessage(type = DpMsgDefine.DPBellCallRecord.class, dpType = TYPE_SET, target = {DOORBELL, MAGNETOMETER})
    public static final int BELL_CALL_STATE = 401;//门铃呼叫状态

    @DpAnnotation(msgId = 402, clazz = int.class)
    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY, target = {DOORBELL, MAGNETOMETER})
    public static final int BELL_VOICE_MSG = 402;//门铃呼叫状态

    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY, target = CAMERA)
    @DpAnnotation(msgId = 501, clazz = boolean.class)
    public static final int CAMERA_ALARM_FLAG = 501;

    @DPMessage(type = DpMsgDefine.DPAlarmInfo.class, target = CAMERA)
    @DpAnnotation(msgId = 502, clazz = DpMsgDefine.DPAlarmInfo.class)
    public static final int CAMERA_ALARM_INFO = 502;

    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY, target = CAMERA)
    @DpAnnotation(msgId = 503, clazz = int.class)
    public static final int CAMERA_ALARM_SENSITIVITY = 503;

    @DPMessage(type = DpMsgDefine.DPNotificationInfo.class, target = CAMERA)
    @DpAnnotation(msgId = 504, clazz = DpMsgDefine.DPNotificationInfo.class)
    public static final int CAMERA_ALARM_NOTIFICATION = 504;//报警音效

    @DPMessage(type = DpMsgDefine.DPAlarm.class, dpType = TYPE_SET, target = CAMERA)
    @DpAnnotation(msgId = 505, clazz = DpMsgDefine.DPAlarm.class)
    public static final int CAMERA_ALARM_MSG = 505;//

    @DPMessage(type = DpMsgDefine.DPTimeLapse.class)
    @DpAnnotation(msgId = 506, clazz = DpMsgDefine.DPTimeLapse.class)
    public static final int CAMERA_TIME_LAPSE_PHOTOGRAPHY = 506;//

    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY)
    @DpAnnotation(msgId = 508, clazz = boolean.class)
    public static final int CAMERA_STANDBY_FLAG = 508;//是否开启直播，待机模式

    @DPMessage(type = Integer.class, dpType = TYPE_PRIMARY)
    @DpAnnotation(msgId = 509, clazz = int.class)
    public static final int CAMERA_MOUNT_MODE = 509;//针对全景摄像头，吊顶，挂壁

    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY)
    @DpAnnotation(msgId = 510, clazz = boolean.class)
    public static final int CAMERA_COORDINATE = 510;//视频坐标

    //兼容3.0，服务端的锅。
    @DPMessage(type = DpMsgDefine.DPAlarm.class, dpType = TYPE_SET, target = CAMERA)
    @DpAnnotation(msgId = 512, clazz = DpMsgDefine.DPAlarm.class)
    public static final int CAMERA_ALARM_MSG_V3 = 512;//

    @DPMessage(type = String.class, dpType = TYPE_PRIMARY, target = ACCOUNT)
    @DpAnnotation(msgId = 601, clazz = String.class)
    public static final int ACCOUNT_STATE = 601;//绑定、解绑消息

    @DPMessage(type = DpMsgDefine.DPWonderItem.class, dpType = TYPE_SET, target = ACCOUNT)
    @DpAnnotation(msgId = 602, clazz = DpMsgDefine.DPWonderItem.class)
    public static final int ACCOUNT_WONDERFUL_MSG = 602;//每日精彩消息

    @DPMessage(type = Boolean.class, dpType = TYPE_PRIMARY)
    @DpAnnotation(msgId = 701, clazz = boolean.class)
    public static final int SYS_PUSH_FLAG = 701;
}
