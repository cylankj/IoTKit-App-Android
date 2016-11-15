package com.cylan.jiafeigou.dp;

import com.cylan.annotation.DpAnnotation;

/**
 * Created by cylan-hunt on 16-11-8.
 */

public class DpMsgConstant {

    public static final int DP_ID_JFG_BEGIN = 200;
    public static final int DP_ID_VIDEO_BEGIN = 300;
    public static final int DP_ID_BELL_BEGIN = 400;
    public static final int DP_ID_CAMERA_BEGIN = 500;
    public static final int DP_ID_ACCOUNT_BEGIN = 600;
    public static final int DP_ID_SYSTEM_BEGIN = 700;

    @DpAnnotation(msgId = 201, clazz = String.class)
    public static final String NET = "NET";//201
    @DpAnnotation(msgId = 202, clazz = String.class)
    public static final String MAC = "MAC";
    @DpAnnotation(msgId = 203, clazz = boolean.class)
    public static final String SDCARD_STATE = "SDCARD_STATE";
    @DpAnnotation(msgId = 204, clazz = String.class)
    public static final String SDCARD_STORAGE = "SDCARD_STORAGE";

    @DpAnnotation(msgId = 205, clazz = boolean.class)
    public static final String CHARGING = "CHARGING";//充电中。。。

    @DpAnnotation(msgId = 206, clazz = int.class)
    public static final String BATTERY = "BATTERY";

    @DpAnnotation(msgId = 207, clazz = String.class)
    public static final String APP_VERSION = "APP_VERSION";

    @DpAnnotation(msgId = 208, clazz = String.class)
    public static final String SYS_VERSION = "SYS_VERSION";

    @DpAnnotation(msgId = 209, clazz = boolean.class)
    public static final String LED_INDICATOR = "LED_INDICATOR";

    @DpAnnotation(msgId = 210, clazz = boolean.class)
    public static final String UP_TIME = "UP_TIME";

    @DpAnnotation(msgId = 211, clazz = int.class)
    public static final String APP_UPLOAD_LOG = "APP_UPLOAD_LOG";

    @DpAnnotation(msgId = 212, clazz = String.class)
    public static final String DEVICE_UPLOAD_LOG = "DEVICE_UPLOAD_LOG";

    @DpAnnotation(msgId = 213, clazz = int.class)
    public static final String DEVICE_P2P_VERSION = "DEVICE_P2P_VERSION";

    @DpAnnotation(msgId = 214, clazz = int.class)
    public static final String DEVICE_TIME_ZONE = "DEVICE_TIME_ZONE";

    @DpAnnotation(msgId = 215, clazz = boolean.class)
    public static final String DEVICE_RTMP = "DEVICE_RTMP";

    @DpAnnotation(msgId = 216, clazz = boolean.class)
    public static final String DEVICE_VOLTAGE = "DEVICE_VOLTAGE";

    @DpAnnotation(msgId = 217, clazz = int.class)
    public static final String DEVICE_MOBILE_NET_PRIORITY = "DEVICE_MOBILE_NET_PRIORITY";

    @DpAnnotation(msgId = 218, clazz = Void.class)
    public static final String DEVICE_FORMAT_SDCARD = "DEVICE_FORMAT_SDCARD";

    @DpAnnotation(msgId = 219, clazz = String.class)
    public static final String DEVICE_BIND_LOG = "DEVICE_BIND_LOG";

    @DpAnnotation(msgId = 220, clazz = String.class)
    public static final String SDK_VERSION = "SDK_VERSION";


    @DpAnnotation(msgId = 301, clazz = boolean.class)
    public static final String DEVICE_MIC = "DEVICE_MIC";//301

    @DpAnnotation(msgId = 302, clazz = boolean.class)
    public static final String DEVICE_SPEAKER = "DEVICE_SPEAKER";//302

    @DpAnnotation(msgId = 303, clazz = int.class)
    public static final String DEVICE_AUTO_VIDEO_RECORD = "DEVICE_AUTO_VIDEO_RECORD";//303

    @DpAnnotation(msgId = 304, clazz = int.class)
    public static final String DEVICE_CAMERA_ROTATE = "DEVICE_CAMERA_ROTATE";//304

    @DpAnnotation(msgId = 401, clazz = String.class)
    public static final String BELL_CALL_STATE = "BELL_CALL_STATE";//门铃呼叫状态

    @DpAnnotation(msgId = 402, clazz = int.class)
    public static final String BELL_VOICE_MSG = "BELL_VOICE_MSG";//门铃呼叫状态


    @DpAnnotation(msgId = 501, clazz = boolean.class)
    public static final String CAMERA_ALARM_FLAG = "CAMERA_ALARM_FLAG";

    @DpAnnotation(msgId = 502, clazz = String.class)
    public static final String CAMERA_ALARM_DURATION = "CAMERA_ALARM_DURATION";

    @DpAnnotation(msgId = 503, clazz = int.class)
    public static final String CAMERA_ALARM_SENSITIVITY = "CAMERA_ALARM_SENSITIVITY";

    @DpAnnotation(msgId = 504, clazz = int.class)
    public static final String CAMERA_ALARM_SOUND_EFFECT = "CAMERA_ALARM_SOUND_EFFECT";//报警音效

    @DpAnnotation(msgId = 505, clazz = String.class)
    public static final String CAMERA_ALARM_MSG = "CAMERA_ALARM_MSG";//

    @DpAnnotation(msgId = 506, clazz = String.class)
    public static final String CAMERA_TIME_LAPSE_PHOTOGRAPHY = "CAMERA_TIME_LAPSE_PHOTOGRAPHY";//

    @DpAnnotation(msgId = 507, clazz = boolean.class)
    public static final String CAMERA_LIVE_FLAG = "CAMERA_LIVE_FLAG";//是否开启直播，待机模式

    @DpAnnotation(msgId = 508, clazz = int.class)
    public static final String CAMERA_MOUNT_MODE = "CAMERA_MOUNT_MODE";//针对全景摄像头，吊顶，挂壁

    @DpAnnotation(msgId = 509, clazz = boolean.class)
    public static final String CAMERA_COORDINATE = "CAMERA_COORDINATE";//视频坐标

    @DpAnnotation(msgId = 601, clazz = String.class)
    public static final String ACCOUNT_STATE = "ACCOUNT_STATE";//绑定、解绑消息

    @DpAnnotation(msgId = 602, clazz = String.class)
    public static final String ACCOUNT_WONDERFUL_MSG = "ACCOUNT_WONDERFUL_MSG";//每日精彩消息

    @DpAnnotation(msgId = 701, clazz = boolean.class)
    public static final String SYS_PUSH_FLAG = "SYS_PUSH_FLAG";

}
