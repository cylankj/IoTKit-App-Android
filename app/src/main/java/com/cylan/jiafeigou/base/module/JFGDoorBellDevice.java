// APT自动生成的文件,请勿修改!!!!
package com.cylan.jiafeigou.base.module;

import com.cylan.ext.annotations.DPType;
import com.cylan.jiafeigou.cache.db.module.Device;

import static com.cylan.jiafeigou.dp.DpMsgDefine.DPBellCallRecord;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPBindLog;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPNet;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPTimeZone;

public class JFGDoorBellDevice extends Device {
    @DPProperty(type = DPNet.class)
    public static final int NET = 201;

    @DPProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int BATTERY = 206;

    @DPProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int UP_TIME = 210;

    @DPProperty(type = DPTimeZone.class)
    public static final int DEVICE_TIME_ZONE = -214;

    @DPProperty(type = Boolean.class, dpType = DPType.TYPE_PRIMARY)
    public static final int DEVICE_VOLTAGE = 216;

    @DPProperty(type = DPBindLog.class)
    public static final int DEVICE_BIND_LOG = 219;

    @DPProperty(type = Boolean.class, dpType = DPType.TYPE_PRIMARY)
    public static final int DEVICE_MIC = 301;

    @DPProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int DEVICE_SPEAKER = 302;

    @DPProperty(type = DPBellCallRecord.class, dpType = DPType.TYPE_SET)
    public static final int BELL_CALL_STATE = 401;

    @DPProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int BELL_VOICE_MSG = 402;
}
