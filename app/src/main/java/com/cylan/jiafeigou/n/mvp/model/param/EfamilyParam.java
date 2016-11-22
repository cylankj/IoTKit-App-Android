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

public class EfamilyParam extends BaseParam {

    @Override
    public ArrayList<JFGDPMsg> queryParameters(Map<Integer, Long> mapVersion) {
        DpParameters.Builder builder = new DpParameters.Builder();
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.NET_201), getVersion(mapVersion, 201));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.BELL_CALL_STATE_401), getVersion(mapVersion, 401));
        builder.addParam(DpMsgMap.NAME_2_ID_MAP.get(DpMsgMap.BELL_VOICE_MSG_402), getVersion(mapVersion, 402));
        builder.addAll(getBaseList(mapVersion));
        AppLogger.i("req:" + builder.toString());
        return builder.build();
    }
}
