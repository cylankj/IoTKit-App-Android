package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.entity.msg.EfamilyAlarmDeviceInfo;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:18
 */
@Message
public  class MsgEfamilySetAlarmReq extends MsgpackMsg.MsgHeader {
    public MsgEfamilySetAlarmReq(String callee) {
        msgId = MsgpackMsg.CLIENT_EFAML_SET_ALARM_REQ;
        this.callee = callee;
    }

    @Index(3)
    public int warn_begin_time;
    @Index(4)
    public int warn_end_time;
    @Index(5)
    public int warn_week;
    @Index(6)
    public List<EfamilyAlarmDeviceInfo> data;
    @Index(7)
    public int location;
}