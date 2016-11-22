package com.cylan.jiafeigou.n.mvp.model.param;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpParameters;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public class CamParam extends BaseParam {

    @Override
    public ArrayList<JFGDPMsg> queryParameters(Map<Integer, Long> mapVersion) {
        DpParameters.Builder builder = new DpParameters.Builder();
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.NET_201), getVersion(mapVersion, 201));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.SDCARD_STATE_203), getVersion(mapVersion, 203));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.SDCARD_STORAGE_204), getVersion(mapVersion, 204));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CHARGING_205), getVersion(mapVersion, 205));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.BATTERY_206), getVersion(mapVersion, 206));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.LED_INDICATOR_209), getVersion(mapVersion, 209));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.UP_TIME_210), getVersion(mapVersion, 210));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_TIME_ZONE_214), getVersion(mapVersion, 214));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_RTMP_215), getVersion(mapVersion, 215));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_VOLTAGE_216), getVersion(mapVersion, 216));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_MOBILE_NET_PRIORITY_217), getVersion(mapVersion, 217));

        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_MIC_301), getVersion(mapVersion, 301));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_SPEAKER_302), getVersion(mapVersion, 302));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_AUTO_VIDEO_RECORD_303), getVersion(mapVersion, 303));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.DEVICE_CAMERA_ROTATE_304), getVersion(mapVersion, 304));

        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_ALARM_FLAG_501), getVersion(mapVersion, 501));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_ALARM_DURATION_502), getVersion(mapVersion, 502));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_ALARM_SENSITIVITY_503), getVersion(mapVersion, 503));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_ALARM_SOUND_EFFECT_504), getVersion(mapVersion, 504));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_ALARM_MSG_505), getVersion(mapVersion, 505));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_TIME_LAPSE_PHOTOGRAPHY_506), getVersion(mapVersion, 506));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_STANDBY_FLAG_508), getVersion(mapVersion, 508));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_MOUNT_MODE_509), getVersion(mapVersion, 509));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.CAMERA_COORDINATE_510), getVersion(mapVersion, 510));
        builder.addAll(getBaseList(mapVersion));
        AppLogger.i("req:" + builder.toString());
        return builder.build();
    }

}
