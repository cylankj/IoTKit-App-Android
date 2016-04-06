package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:17
 */
@Message
public class MsgEfamilyGetAlarmReq extends MsgpackMsg.MsgHeader {
    public MsgEfamilyGetAlarmReq(String callee) {
        msgId = MsgpackMsg.CLIENT_EFAML_GET_ALARM_REQ;
        this.callee = callee;
    }
}
