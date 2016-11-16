package com.cylan.jiafeigou.dp;

import java.util.HashMap;
import java.util.Map;

import static com.cylan.jiafeigou.dp.DpMsgConstant.ACCOUNT_STATE;
import static com.cylan.jiafeigou.dp.DpMsgConstant.ACCOUNT_WONDERFUL_MSG;
import static com.cylan.jiafeigou.dp.DpMsgConstant.APP_UPLOAD_LOG;
import static com.cylan.jiafeigou.dp.DpMsgConstant.APP_VERSION;
import static com.cylan.jiafeigou.dp.DpMsgConstant.BATTERY;
import static com.cylan.jiafeigou.dp.DpMsgConstant.BELL_CALL_STATE;
import static com.cylan.jiafeigou.dp.DpMsgConstant.BELL_VOICE_MSG;
import static com.cylan.jiafeigou.dp.DpMsgConstant.CAMERA_ALARM_DURATION;
import static com.cylan.jiafeigou.dp.DpMsgConstant.CAMERA_ALARM_FLAG;
import static com.cylan.jiafeigou.dp.DpMsgConstant.CAMERA_ALARM_MSG;
import static com.cylan.jiafeigou.dp.DpMsgConstant.CAMERA_ALARM_SENSITIVITY;
import static com.cylan.jiafeigou.dp.DpMsgConstant.CAMERA_ALARM_SOUND_EFFECT;
import static com.cylan.jiafeigou.dp.DpMsgConstant.CAMERA_COORDINATE;
import static com.cylan.jiafeigou.dp.DpMsgConstant.CAMERA_LIVE_FLAG;
import static com.cylan.jiafeigou.dp.DpMsgConstant.CAMERA_MOUNT_MODE;
import static com.cylan.jiafeigou.dp.DpMsgConstant.CAMERA_TIME_LAPSE_PHOTOGRAPHY;
import static com.cylan.jiafeigou.dp.DpMsgConstant.CHARGING;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_AUTO_VIDEO_RECORD;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_BIND_LOG;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_CAMERA_ROTATE;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_FORMAT_SDCARD;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_MIC;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_MOBILE_NET_PRIORITY;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_P2P_VERSION;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_RTMP;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_SPEAKER;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_TIME_ZONE;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_UPLOAD_LOG;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_VOLTAGE;
import static com.cylan.jiafeigou.dp.DpMsgConstant.LED_INDICATOR;
import static com.cylan.jiafeigou.dp.DpMsgConstant.MAC;
import static com.cylan.jiafeigou.dp.DpMsgConstant.NET;
import static com.cylan.jiafeigou.dp.DpMsgConstant.SDCARD_STATE;
import static com.cylan.jiafeigou.dp.DpMsgConstant.SDCARD_STORAGE;
import static com.cylan.jiafeigou.dp.DpMsgConstant.SDK_VERSION;
import static com.cylan.jiafeigou.dp.DpMsgConstant.SYS_PUSH_FLAG;
import static com.cylan.jiafeigou.dp.DpMsgConstant.SYS_VERSION;
import static com.cylan.jiafeigou.dp.DpMsgConstant.UP_TIME;

/**
 * Created by cylan-hunt on 16-11-8.
 */

public class DpMsgClassMap {
    public static Map<String, Class<?>> CLASS_TYPE_MAP = new HashMap<>();

    static {
        CLASS_TYPE_MAP.put(NET, String.class);
        CLASS_TYPE_MAP.put(MAC, String.class);
        CLASS_TYPE_MAP.put(SDCARD_STATE, boolean.class);
        CLASS_TYPE_MAP.put(SDCARD_STORAGE, String.class);
        CLASS_TYPE_MAP.put(CHARGING, boolean.class);
        CLASS_TYPE_MAP.put(BATTERY, int.class);
        CLASS_TYPE_MAP.put(APP_VERSION, String.class);
        CLASS_TYPE_MAP.put(SYS_VERSION, String.class);
        CLASS_TYPE_MAP.put(LED_INDICATOR, boolean.class);
        CLASS_TYPE_MAP.put(UP_TIME, long.class);
        CLASS_TYPE_MAP.put(APP_UPLOAD_LOG, int.class);
        CLASS_TYPE_MAP.put(DEVICE_UPLOAD_LOG, String.class);
        CLASS_TYPE_MAP.put(DEVICE_P2P_VERSION, int.class);
        CLASS_TYPE_MAP.put(DEVICE_TIME_ZONE, int.class);
        CLASS_TYPE_MAP.put(DEVICE_RTMP, boolean.class);
        CLASS_TYPE_MAP.put(DEVICE_VOLTAGE, boolean.class);
        CLASS_TYPE_MAP.put(DEVICE_MOBILE_NET_PRIORITY, boolean.class);
        CLASS_TYPE_MAP.put(DEVICE_FORMAT_SDCARD, Void.class);
        CLASS_TYPE_MAP.put(DEVICE_BIND_LOG, String.class);
        CLASS_TYPE_MAP.put(SDK_VERSION, String.class);


        CLASS_TYPE_MAP.put(DEVICE_MIC, boolean.class);
        CLASS_TYPE_MAP.put(DEVICE_SPEAKER, boolean.class);
        CLASS_TYPE_MAP.put(DEVICE_AUTO_VIDEO_RECORD, int.class);
        CLASS_TYPE_MAP.put(DEVICE_CAMERA_ROTATE, int.class);


        CLASS_TYPE_MAP.put(BELL_CALL_STATE, String.class);
        CLASS_TYPE_MAP.put(BELL_VOICE_MSG, int.class);


        CLASS_TYPE_MAP.put(CAMERA_ALARM_FLAG, boolean.class);
        CLASS_TYPE_MAP.put(CAMERA_ALARM_DURATION, String.class);
        CLASS_TYPE_MAP.put(CAMERA_ALARM_SENSITIVITY, int.class);
        CLASS_TYPE_MAP.put(CAMERA_ALARM_SOUND_EFFECT, int.class);
        CLASS_TYPE_MAP.put(CAMERA_ALARM_MSG, String.class);
        CLASS_TYPE_MAP.put(CAMERA_TIME_LAPSE_PHOTOGRAPHY, String.class);
        CLASS_TYPE_MAP.put(CAMERA_LIVE_FLAG, boolean.class);
        CLASS_TYPE_MAP.put(CAMERA_MOUNT_MODE, int.class);
        CLASS_TYPE_MAP.put(CAMERA_COORDINATE, String.class);


        CLASS_TYPE_MAP.put(SYS_PUSH_FLAG, String.class);


        CLASS_TYPE_MAP.put(ACCOUNT_STATE, String.class);
        CLASS_TYPE_MAP.put(ACCOUNT_WONDERFUL_MSG, String.class);
    }

}
