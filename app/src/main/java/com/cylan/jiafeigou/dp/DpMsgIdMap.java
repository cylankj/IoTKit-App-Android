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
import static com.cylan.jiafeigou.dp.DpMsgConstant.DP_ID_ACCOUNT_BEGIN;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DP_ID_BELL_BEGIN;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DP_ID_CAMERA_BEGIN;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DP_ID_JFG_BEGIN;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DP_ID_SYSTEM_BEGIN;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DP_ID_VIDEO_BEGIN;
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

public class DpMsgIdMap {
    public static final Map<String, Integer> IdMap = new HashMap<>();


    static {
        IdMap.put(NET, DP_ID_JFG_BEGIN + 1);
        IdMap.put(MAC, DP_ID_JFG_BEGIN + 2);
        IdMap.put(SDCARD_STATE, DP_ID_JFG_BEGIN + 3);
        IdMap.put(SDCARD_STORAGE, DP_ID_JFG_BEGIN + 4);
        IdMap.put(CHARGING, DP_ID_JFG_BEGIN + 5);
        IdMap.put(BATTERY, DP_ID_JFG_BEGIN + 6);
        IdMap.put(APP_VERSION, DP_ID_JFG_BEGIN + 7);
        IdMap.put(SYS_VERSION, DP_ID_JFG_BEGIN + 8);
        IdMap.put(LED_INDICATOR, DP_ID_JFG_BEGIN + 9);
        IdMap.put(UP_TIME, DP_ID_JFG_BEGIN + 10);
        IdMap.put(APP_UPLOAD_LOG, DP_ID_JFG_BEGIN + 11);
        IdMap.put(DEVICE_UPLOAD_LOG, DP_ID_JFG_BEGIN + 12);
        IdMap.put(DEVICE_P2P_VERSION, DP_ID_JFG_BEGIN + 13);
        IdMap.put(DEVICE_TIME_ZONE, DP_ID_JFG_BEGIN + 14);
        IdMap.put(DEVICE_RTMP, DP_ID_JFG_BEGIN + 15);
        IdMap.put(DEVICE_VOLTAGE, DP_ID_JFG_BEGIN + 16);
        IdMap.put(DEVICE_MOBILE_NET_PRIORITY, DP_ID_JFG_BEGIN + 17);
        IdMap.put(DEVICE_FORMAT_SDCARD, DP_ID_JFG_BEGIN + 18);
        IdMap.put(DEVICE_BIND_LOG, DP_ID_JFG_BEGIN + 19);
        IdMap.put(SDK_VERSION, DP_ID_JFG_BEGIN + 20);
        //300
        IdMap.put(DEVICE_MIC, DP_ID_VIDEO_BEGIN + 1);
        IdMap.put(DEVICE_SPEAKER, DP_ID_VIDEO_BEGIN + 2);
        IdMap.put(DEVICE_AUTO_VIDEO_RECORD, DP_ID_VIDEO_BEGIN + 3);
        IdMap.put(DEVICE_CAMERA_ROTATE, DP_ID_VIDEO_BEGIN + 4);
        //400
        IdMap.put(BELL_CALL_STATE, DP_ID_BELL_BEGIN + 1);
        IdMap.put(BELL_VOICE_MSG, DP_ID_BELL_BEGIN + 2);
        //500
        IdMap.put(CAMERA_ALARM_FLAG, DP_ID_CAMERA_BEGIN + 1);
        IdMap.put(CAMERA_ALARM_DURATION, DP_ID_CAMERA_BEGIN + 2);
        IdMap.put(CAMERA_ALARM_SENSITIVITY, DP_ID_CAMERA_BEGIN + 3);
        IdMap.put(CAMERA_ALARM_SOUND_EFFECT, DP_ID_CAMERA_BEGIN + 4);
        IdMap.put(CAMERA_ALARM_MSG, DP_ID_CAMERA_BEGIN + 5);
        IdMap.put(CAMERA_TIME_LAPSE_PHOTOGRAPHY, DP_ID_CAMERA_BEGIN + 6);
        IdMap.put(CAMERA_LIVE_FLAG, DP_ID_CAMERA_BEGIN + 7);
        IdMap.put(CAMERA_MOUNT_MODE, DP_ID_CAMERA_BEGIN + 8);
        IdMap.put(CAMERA_COORDINATE, DP_ID_CAMERA_BEGIN + 9);
        //600
        IdMap.put(ACCOUNT_STATE, DP_ID_ACCOUNT_BEGIN + 1);
        IdMap.put(ACCOUNT_WONDERFUL_MSG, DP_ID_ACCOUNT_BEGIN + 2);

        //700
        IdMap.put(SYS_PUSH_FLAG, DP_ID_SYSTEM_BEGIN + 1);
    }
    public static void main(String[] args) {
        System.out.println(Map.class.getName());
        System.out.println(Map.class.getGenericSuperclass());
    }
}
