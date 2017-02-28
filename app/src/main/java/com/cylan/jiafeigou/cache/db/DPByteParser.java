package com.cylan.jiafeigou.cache.db;

import android.util.SparseArray;


/**
 * Created by yanzhendong on 2017/2/27.
 */

public class DPByteParser {

    public static final SparseArray<Class<?>> mElementMap = new SparseArray<>();

    static {
        mElementMap.put(201, com.cylan.jiafeigou.dp.DpMsgDefine.DPNet.class);
        mElementMap.put(202, java.lang.String.class);
        mElementMap.put(204, com.cylan.jiafeigou.dp.DpMsgDefine.DPSdStatus.class);
        mElementMap.put(222, com.cylan.jiafeigou.dp.DpMsgDefine.DPSdcardSummary.class);
        mElementMap.put(205, boolean.class);
        mElementMap.put(206, int.class);
        mElementMap.put(207, java.lang.String.class);
        mElementMap.put(208, java.lang.String.class);
        mElementMap.put(209, boolean.class);
        mElementMap.put(210, int.class);
        mElementMap.put(211, int.class);
        mElementMap.put(212, java.lang.String.class);
        mElementMap.put(213, int.class);
        mElementMap.put(214, com.cylan.jiafeigou.dp.DpMsgDefine.DPTimeZone.class);
        mElementMap.put(215, boolean.class);
        mElementMap.put(216, boolean.class);
        mElementMap.put(217, boolean.class);
        mElementMap.put(218, java.lang.Void.class);
        mElementMap.put(219, com.cylan.jiafeigou.dp.DpMsgDefine.DPBindLog.class);
        mElementMap.put(220, java.lang.String.class);
        mElementMap.put(301, boolean.class);
        mElementMap.put(302, int.class);
        mElementMap.put(303, int.class);
        mElementMap.put(304, int.class);
        mElementMap.put(401, com.cylan.jiafeigou.dp.DpMsgDefine.DPBellCallRecord.class);
        mElementMap.put(402, int.class);
        mElementMap.put(501, boolean.class);
        mElementMap.put(502, com.cylan.jiafeigou.dp.DpMsgDefine.DPAlarmInfo.class);
        mElementMap.put(503, int.class);
        mElementMap.put(504, com.cylan.jiafeigou.dp.DpMsgDefine.DPNotificationInfo.class);
        mElementMap.put(505, com.cylan.jiafeigou.dp.DpMsgDefine.DPAlarm.class);
        mElementMap.put(506, com.cylan.jiafeigou.dp.DpMsgDefine.DPTimeLapse.class);
        mElementMap.put(508, boolean.class);
        mElementMap.put(509, int.class);
        mElementMap.put(510, boolean.class);
        mElementMap.put(601, java.lang.String.class);
        mElementMap.put(602, com.cylan.jiafeigou.dp.DpMsgDefine.DPWonderItem.class);
        mElementMap.put(701, boolean.class);
    }
}