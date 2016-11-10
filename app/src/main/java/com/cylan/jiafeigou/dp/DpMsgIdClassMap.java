package com.cylan.jiafeigou.dp;

import java.util.HashMap;
import java.util.Map;

import static com.cylan.jiafeigou.dp.DpMsgClassMap.CLASS_TYPE_MAP;
import static com.cylan.jiafeigou.dp.DpMsgConstant.ACCOUNT_STATE;
import static com.cylan.jiafeigou.dp.DpMsgConstant.ACCOUNT_WONDERFUL_MSG;
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
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_AUTO_VIDEO_RECORD;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_CAMERA_ROTATE;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_MIC;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DEVICE_SPEAKER;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DP_ID_ACCOUNT_BEGIN;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DP_ID_BELL_BEGIN;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DP_ID_CAMERA_BEGIN;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DP_ID_JFG_BEGIN;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DP_ID_SYSTEM_BEGIN;
import static com.cylan.jiafeigou.dp.DpMsgConstant.DP_ID_VIDEO_BEGIN;
import static com.cylan.jiafeigou.dp.DpMsgConstant.SYS_PUSH_FLAG;

/**
 * Created by cylan-hunt on 16-11-8.
 */

public class DpMsgIdClassMap {
    public static final Map<Integer, Class<?>> Id2ClassMap = new HashMap<>();


    static {
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 1, DpMsgDefine.MsgNet.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 2, String.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 3, boolean.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 4, String.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 5, boolean.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 6, int.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 7, String.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 8, String.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 9, boolean.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 10, long.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 11, int.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 12, String.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 13, int.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 14, int.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 15, boolean.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 16, boolean.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 17, int.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 18, Void.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 19, String.class);
        Id2ClassMap.put(DP_ID_JFG_BEGIN + 20, String.class);
        //300
        Id2ClassMap.put(DP_ID_VIDEO_BEGIN + 1, CLASS_TYPE_MAP.get(DEVICE_MIC));
        Id2ClassMap.put(DP_ID_VIDEO_BEGIN + 2, CLASS_TYPE_MAP.get(DEVICE_SPEAKER));
        Id2ClassMap.put(DP_ID_VIDEO_BEGIN + 3, CLASS_TYPE_MAP.get(DEVICE_AUTO_VIDEO_RECORD));
        Id2ClassMap.put(DP_ID_VIDEO_BEGIN + 4, CLASS_TYPE_MAP.get(DEVICE_CAMERA_ROTATE));
        //400
        Id2ClassMap.put(DP_ID_BELL_BEGIN + 1, CLASS_TYPE_MAP.get(BELL_CALL_STATE));
        Id2ClassMap.put(DP_ID_BELL_BEGIN + 2, CLASS_TYPE_MAP.get(BELL_VOICE_MSG));
        //500
        Id2ClassMap.put(DP_ID_CAMERA_BEGIN + 1, CLASS_TYPE_MAP.get(CAMERA_ALARM_FLAG));
        Id2ClassMap.put(DP_ID_CAMERA_BEGIN + 2, CLASS_TYPE_MAP.get(CAMERA_ALARM_DURATION));
        Id2ClassMap.put(DP_ID_CAMERA_BEGIN + 3, CLASS_TYPE_MAP.get(CAMERA_ALARM_SENSITIVITY));
        Id2ClassMap.put(DP_ID_CAMERA_BEGIN + 4, CLASS_TYPE_MAP.get(CAMERA_ALARM_SOUND_EFFECT));
        Id2ClassMap.put(DP_ID_CAMERA_BEGIN + 5, CLASS_TYPE_MAP.get(CAMERA_ALARM_MSG));
        Id2ClassMap.put(DP_ID_CAMERA_BEGIN + 6, CLASS_TYPE_MAP.get(CAMERA_TIME_LAPSE_PHOTOGRAPHY));
        Id2ClassMap.put(DP_ID_CAMERA_BEGIN + 7, CLASS_TYPE_MAP.get(CAMERA_LIVE_FLAG));
        Id2ClassMap.put(DP_ID_CAMERA_BEGIN + 8, CLASS_TYPE_MAP.get(CAMERA_MOUNT_MODE));
        Id2ClassMap.put(DP_ID_CAMERA_BEGIN + 9, CLASS_TYPE_MAP.get(CAMERA_COORDINATE));
        //600
        Id2ClassMap.put(DP_ID_ACCOUNT_BEGIN + 1, CLASS_TYPE_MAP.get(ACCOUNT_STATE));
        Id2ClassMap.put(DP_ID_ACCOUNT_BEGIN + 2, CLASS_TYPE_MAP.get(ACCOUNT_WONDERFUL_MSG));

        //700
        Id2ClassMap.put(DP_ID_SYSTEM_BEGIN + 1, CLASS_TYPE_MAP.get(SYS_PUSH_FLAG));
    }


}
