package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:12
 */
@Message
public  class MsgBellCallListReq extends MsgpackMsg.MsgHeader {
    public MsgBellCallListReq(String callee) {
        msgId = MsgpackMsg.CLIENT_BELL_CALL_LIST_REQ;
        this.callee = callee;
    }
}
