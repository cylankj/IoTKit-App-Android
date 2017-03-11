// APT自动生成的文件,请勿修改!!!!
package com.cylan.jiafeigou.base.module;

import com.cylan.ext.annotations.DPProperty;
import com.cylan.jiafeigou.cache.db.module.Device;

import static com.cylan.jiafeigou.dp.DpMsgDefine.DPBellCallRecord;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPPrimary;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPSet;

public class JFGMagnetometerDevice extends Device {
    @DPProperty(msgId = 401)
    public DPSet<DPBellCallRecord> bell_call_state;

    @DPProperty(msgId = 402)
    public DPPrimary<Integer> bell_voice_msg;

    @Override
    public JFGMagnetometerDevice $() {
        return this;
    }
}
