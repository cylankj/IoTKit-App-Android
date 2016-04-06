package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-21
 * Time: 14:31
 */

@Message
public  class MsgClientMagGetWarnReq extends MsgpackMsg.MsgHeader{

    public MsgClientMagGetWarnReq(String caller, String callee) {
        this.caller = caller;
        this.callee = callee;
        msgId = MsgpackMsg.CLIENT_MAG_GET_WARN_REQ;
    }
}
