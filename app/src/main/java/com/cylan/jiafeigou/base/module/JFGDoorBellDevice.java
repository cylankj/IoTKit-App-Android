// APT自动生成的文件,请勿修改!!!!
package com.cylan.jiafeigou.base.module;

import com.cylan.ext.annotations.DPProperty;
import com.cylan.jiafeigou.cache.db.module.Device;

import static com.cylan.jiafeigou.dp.DpMsgDefine.DPBellCallRecord;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPBindLog;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPNet;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPPrimary;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPSet;
import static com.cylan.jiafeigou.dp.DpMsgDefine.DPTimeZone;

public class JFGDoorBellDevice extends Device {
    @DPProperty(msgId = 201)
    public DPNet net;

    @DPProperty(msgId = 206)
    public DPPrimary<Integer> battery;

    @DPProperty(msgId = 210)
    public DPPrimary<Integer> up_time;

    @DPProperty(msgId = 214)
    public DPTimeZone device_time_zone;

    @DPProperty(msgId = 216)
    public DPPrimary<Boolean> device_voltage;

    @DPProperty(msgId = 219)
    public DPBindLog device_bind_log;

    @DPProperty(msgId = 301)
    public DPPrimary<Boolean> device_mic;

    @DPProperty(msgId = 302)
    public DPPrimary<Integer> device_speaker;

    @DPProperty(msgId = 401)
    public DPSet<DPBellCallRecord> bell_call_state;

    @DPProperty(msgId = 402)
    public DPPrimary<Integer> bell_voice_msg;

    @Override
    public JFGDoorBellDevice $() {
        return this;
    }
}
