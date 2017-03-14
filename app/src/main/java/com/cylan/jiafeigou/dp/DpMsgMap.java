package com.cylan.jiafeigou.dp;

import java.util.HashMap;
import java.util.Map;

public final class DpMsgMap {
    public static final Map<Integer, Class<?>> ID_2_CLASS_MAP = new HashMap<Integer, Class<?>>();

    public static final Map<String, Integer> NAME_2_ID_MAP = new HashMap<String, Integer>();

    public static final Map<Integer, String> ID_2_NAME_MAP = new HashMap<Integer, String>();

    public static final String APP_UPLOAD_LOG_211 = "app_upload_log";

    public static final String DEVICE_UPLOAD_LOG_212 = "device_upload_log";

    public static final String DEVICE_VOLTAGE_216 = "device_voltage";

    public static final String ACCOUNT_STATE_601 = "account_state";

    public static final String DEVICE_TIME_ZONE_214 = "device_time_zone";

    public static final String CAMERA_MOUNT_MODE_509 = "camera_mount_mode";

    public static final String CAMERA_ALARM_INFO_502 = "camera_alarm_info";

    public static final String CAMERA_ALARM_FLAG_501 = "camera_alarm_flag";

    public static final String CHARGING_205 = "charging";

    public static final String CAMERA_ALARM_MSG_505 = "camera_alarm_msg";

    public static final String DEVICE_VERSION_207 = "device_version";

    public static final String SDK_VERSION_220 = "sdk_version";

    public static final String DEVICE_P2P_VERSION_213 = "device_p2p_version";

    public static final String ACCOUNT_WONDERFUL_MSG_602 = "account_wonderful_msg";

    public static final String DEVICE_SPEAKER_302 = "device_speaker";

    public static final String CAMERA_ALARM_NOTIFICATION_504 = "camera_alarm_notification";

    public static final String BATTERY_206 = "battery";

    public static final String UP_TIME_210 = "up_time";

    public static final String DEVICE_CAMERA_ROTATE_304 = "device_camera_rotate";

    public static final String DEVICE_AUTO_VIDEO_RECORD_303 = "device_auto_video_record";

    public static final String BELL_VOICE_MSG_402 = "bell_voice_msg";

    public static final String CAMERA_COORDINATE_510 = "camera_coordinate";

    public static final String DEVICE_FORMAT_SDCARD_218 = "device_format_sdcard";

    public static final String DEVICE_BIND_LOG_219 = "device_bind_log";

    public static final String CAMERA_TIME_LAPSE_PHOTOGRAPHY_506 = "camera_time_lapse_photography";

    public static final String MAC_202 = "mac";

    public static final String DEVICE_SYS_VERSION_208 = "device_sys_version";

    public static final String DEVICE_MOBILE_NET_PRIORITY_217 = "device_mobile_net_priority";

    public static final String DEVICE_RTMP_215 = "device_rtmp";

    public static final String SDCARD_SUMMARY_222 = "sdcard_summary";

    public static final String DEVICE_MIC_301 = "device_mic";

    public static final String CAMERA_STANDBY_FLAG_508 = "camera_standby_flag";

    public static final String LED_INDICATOR_209 = "led_indicator";

    public static final String CAMERA_ALARM_MSG_V3_512 = "camera_alarm_msg_v3";

    public static final String BELL_CALL_STATE_401 = "bell_call_state";

    public static final String SYS_PUSH_FLAG_701 = "sys_push_flag";

    public static final String SDCARD_STORAGE_204 = "sdcard_storage";

    public static final String CAMERA_ALARM_SENSITIVITY_503 = "camera_alarm_sensitivity";

    public static final String NET_201 = "net";

    public static final int ID_211_APP_UPLOAD_LOG = 211;

    public static final int ID_212_DEVICE_UPLOAD_LOG = 212;

    public static final int ID_216_DEVICE_VOLTAGE = 216;

    public static final int ID_601_ACCOUNT_STATE = 601;

    public static final int ID_214_DEVICE_TIME_ZONE = 214;

    public static final int ID_509_CAMERA_MOUNT_MODE = 509;

    public static final int ID_502_CAMERA_ALARM_INFO = 502;

    public static final int ID_501_CAMERA_ALARM_FLAG = 501;

    public static final int ID_205_CHARGING = 205;

    public static final int ID_505_CAMERA_ALARM_MSG = 505;

    public static final int ID_207_DEVICE_VERSION = 207;

    public static final int ID_220_SDK_VERSION = 220;

    public static final int ID_213_DEVICE_P2P_VERSION = 213;

    public static final int ID_602_ACCOUNT_WONDERFUL_MSG = 602;

    public static final int ID_302_DEVICE_SPEAKER = 302;

    public static final int ID_504_CAMERA_ALARM_NOTIFICATION = 504;

    public static final int ID_206_BATTERY = 206;

    public static final int ID_210_UP_TIME = 210;

    public static final int ID_304_DEVICE_CAMERA_ROTATE = 304;

    public static final int ID_303_DEVICE_AUTO_VIDEO_RECORD = 303;

    public static final int ID_402_BELL_VOICE_MSG = 402;

    public static final int ID_510_CAMERA_COORDINATE = 510;

    public static final int ID_218_DEVICE_FORMAT_SDCARD = 218;

    public static final int ID_219_DEVICE_BIND_LOG = 219;

    public static final int ID_506_CAMERA_TIME_LAPSE_PHOTOGRAPHY = 506;

    public static final int ID_202_MAC = 202;

    public static final int ID_208_DEVICE_SYS_VERSION = 208;

    public static final int ID_217_DEVICE_MOBILE_NET_PRIORITY = 217;

    public static final int ID_215_DEVICE_RTMP = 215;

    public static final int ID_222_SDCARD_SUMMARY = 222;

    public static final int ID_301_DEVICE_MIC = 301;

    public static final int ID_508_CAMERA_STANDBY_FLAG = 508;

    public static final int ID_209_LED_INDICATOR = 209;

    public static final int ID_512_CAMERA_ALARM_MSG_V3 = 512;

    public static final int ID_401_BELL_CALL_STATE = 401;

    public static final int ID_701_SYS_PUSH_FLAG = 701;

    public static final int ID_204_SDCARD_STORAGE = 204;

    public static final int ID_503_CAMERA_ALARM_SENSITIVITY = 503;

    public static final int ID_201_NET = 201;

    static {
        NAME_2_ID_MAP.put("net", 201);
        NAME_2_ID_MAP.put("mac", 202);
        NAME_2_ID_MAP.put("sdcard_storage", 204);
        NAME_2_ID_MAP.put("sdcard_summary", 222);
        NAME_2_ID_MAP.put("charging", 205);
        NAME_2_ID_MAP.put("battery", 206);
        NAME_2_ID_MAP.put("device_version", 207);
        NAME_2_ID_MAP.put("device_sys_version", 208);
        NAME_2_ID_MAP.put("led_indicator", 209);
        NAME_2_ID_MAP.put("up_time", 210);
        NAME_2_ID_MAP.put("app_upload_log", 211);
        NAME_2_ID_MAP.put("device_upload_log", 212);
        NAME_2_ID_MAP.put("device_p2p_version", 213);
        NAME_2_ID_MAP.put("device_time_zone", 214);
        NAME_2_ID_MAP.put("device_rtmp", 215);
        NAME_2_ID_MAP.put("device_voltage", 216);
        NAME_2_ID_MAP.put("device_mobile_net_priority", 217);
        NAME_2_ID_MAP.put("device_format_sdcard", 218);
        NAME_2_ID_MAP.put("device_bind_log", 219);
        NAME_2_ID_MAP.put("sdk_version", 220);
        NAME_2_ID_MAP.put("device_mic", 301);
        NAME_2_ID_MAP.put("device_speaker", 302);
        NAME_2_ID_MAP.put("device_auto_video_record", 303);
        NAME_2_ID_MAP.put("device_camera_rotate", 304);
        NAME_2_ID_MAP.put("bell_call_state", 401);
        NAME_2_ID_MAP.put("bell_voice_msg", 402);
        NAME_2_ID_MAP.put("camera_alarm_flag", 501);
        NAME_2_ID_MAP.put("camera_alarm_info", 502);
        NAME_2_ID_MAP.put("camera_alarm_sensitivity", 503);
        NAME_2_ID_MAP.put("camera_alarm_notification", 504);
        NAME_2_ID_MAP.put("camera_alarm_msg", 505);
        NAME_2_ID_MAP.put("camera_time_lapse_photography", 506);
        NAME_2_ID_MAP.put("camera_standby_flag", 508);
        NAME_2_ID_MAP.put("camera_mount_mode", 509);
        NAME_2_ID_MAP.put("camera_coordinate", 510);
        NAME_2_ID_MAP.put("camera_alarm_msg_v3", 512);
        NAME_2_ID_MAP.put("account_state", 601);
        NAME_2_ID_MAP.put("account_wonderful_msg", 602);
        NAME_2_ID_MAP.put("sys_push_flag", 701);
    }

    static {
        ID_2_NAME_MAP.put(201, "net");
        ID_2_NAME_MAP.put(202, "mac");
        ID_2_NAME_MAP.put(204, "sdcard_storage");
        ID_2_NAME_MAP.put(222, "sdcard_summary");
        ID_2_NAME_MAP.put(205, "charging");
        ID_2_NAME_MAP.put(206, "battery");
        ID_2_NAME_MAP.put(207, "device_version");
        ID_2_NAME_MAP.put(208, "device_sys_version");
        ID_2_NAME_MAP.put(209, "led_indicator");
        ID_2_NAME_MAP.put(210, "up_time");
        ID_2_NAME_MAP.put(211, "app_upload_log");
        ID_2_NAME_MAP.put(212, "device_upload_log");
        ID_2_NAME_MAP.put(213, "device_p2p_version");
        ID_2_NAME_MAP.put(214, "device_time_zone");
        ID_2_NAME_MAP.put(215, "device_rtmp");
        ID_2_NAME_MAP.put(216, "device_voltage");
        ID_2_NAME_MAP.put(217, "device_mobile_net_priority");
        ID_2_NAME_MAP.put(218, "device_format_sdcard");
        ID_2_NAME_MAP.put(219, "device_bind_log");
        ID_2_NAME_MAP.put(220, "sdk_version");
        ID_2_NAME_MAP.put(301, "device_mic");
        ID_2_NAME_MAP.put(302, "device_speaker");
        ID_2_NAME_MAP.put(303, "device_auto_video_record");
        ID_2_NAME_MAP.put(304, "device_camera_rotate");
        ID_2_NAME_MAP.put(401, "bell_call_state");
        ID_2_NAME_MAP.put(402, "bell_voice_msg");
        ID_2_NAME_MAP.put(501, "camera_alarm_flag");
        ID_2_NAME_MAP.put(502, "camera_alarm_info");
        ID_2_NAME_MAP.put(503, "camera_alarm_sensitivity");
        ID_2_NAME_MAP.put(504, "camera_alarm_notification");
        ID_2_NAME_MAP.put(505, "camera_alarm_msg");
        ID_2_NAME_MAP.put(506, "camera_time_lapse_photography");
        ID_2_NAME_MAP.put(508, "camera_standby_flag");
        ID_2_NAME_MAP.put(509, "camera_mount_mode");
        ID_2_NAME_MAP.put(510, "camera_coordinate");
        ID_2_NAME_MAP.put(512, "camera_alarm_msg_v3");
        ID_2_NAME_MAP.put(601, "account_state");
        ID_2_NAME_MAP.put(602, "account_wonderful_msg");
        ID_2_NAME_MAP.put(701, "sys_push_flag");
    }

    static {
        ID_2_CLASS_MAP.put(201, DpMsgDefine.DPNet.class);
        ID_2_CLASS_MAP.put(202, String.class);
        ID_2_CLASS_MAP.put(204, DpMsgDefine.DPSdStatus.class);
        ID_2_CLASS_MAP.put(222, DpMsgDefine.DPSdcardSummary.class);
        ID_2_CLASS_MAP.put(205, boolean.class);
        ID_2_CLASS_MAP.put(206, int.class);
        ID_2_CLASS_MAP.put(207, String.class);
        ID_2_CLASS_MAP.put(208, String.class);
        ID_2_CLASS_MAP.put(209, boolean.class);
        ID_2_CLASS_MAP.put(210, int.class);
        ID_2_CLASS_MAP.put(211, int.class);
        ID_2_CLASS_MAP.put(212, String.class);
        ID_2_CLASS_MAP.put(213, int.class);
        ID_2_CLASS_MAP.put(214, DpMsgDefine.DPTimeZone.class);
        ID_2_CLASS_MAP.put(215, boolean.class);
        ID_2_CLASS_MAP.put(216, boolean.class);
        ID_2_CLASS_MAP.put(217, boolean.class);
        ID_2_CLASS_MAP.put(218, DpMsgDefine.DpSdcardFormatRsp.class);
        ID_2_CLASS_MAP.put(219, DpMsgDefine.DPBindLog.class);
        ID_2_CLASS_MAP.put(220, String.class);
        ID_2_CLASS_MAP.put(301, boolean.class);
        ID_2_CLASS_MAP.put(302, int.class);
        ID_2_CLASS_MAP.put(303, int.class);
        ID_2_CLASS_MAP.put(304, int.class);
        ID_2_CLASS_MAP.put(401, DpMsgDefine.DPBellCallRecord.class);
        ID_2_CLASS_MAP.put(402, int.class);
        ID_2_CLASS_MAP.put(501, boolean.class);
        ID_2_CLASS_MAP.put(502, DpMsgDefine.DPAlarmInfo.class);
        ID_2_CLASS_MAP.put(503, int.class);
        ID_2_CLASS_MAP.put(504, DpMsgDefine.DPNotificationInfo.class);
        ID_2_CLASS_MAP.put(505, DpMsgDefine.DPAlarm.class);
        ID_2_CLASS_MAP.put(506, DpMsgDefine.DPTimeLapse.class);
        ID_2_CLASS_MAP.put(508, boolean.class);
        ID_2_CLASS_MAP.put(509, int.class);
        ID_2_CLASS_MAP.put(510, boolean.class);
        ID_2_CLASS_MAP.put(512, DpMsgDefine.DPAlarm.class);
        ID_2_CLASS_MAP.put(601, String.class);
        ID_2_CLASS_MAP.put(602, DpMsgDefine.DPWonderItem.class);
        ID_2_CLASS_MAP.put(701, boolean.class);
    }
}
