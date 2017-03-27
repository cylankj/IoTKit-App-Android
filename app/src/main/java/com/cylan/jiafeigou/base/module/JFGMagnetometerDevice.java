// APT自动生成的文件,请勿修改!!!!
package com.cylan.jiafeigou.base.module;

import com.cylan.ext.annotations.DPType;
import com.cylan.jiafeigou.cache.db.module.Device;

import static com.cylan.jiafeigou.dp.DpMsgDefine.DPBellCallRecord;

public class JFGMagnetometerDevice extends Device {
    @DProperty(type = DPBellCallRecord.class, dpType = DPType.TYPE_SET)
    public static final int BELL_CALL_STATE = 401;

    @DProperty(type = Integer.class, dpType = DPType.TYPE_PRIMARY)
    public static final int BELL_VOICE_MSG = 402;



}
