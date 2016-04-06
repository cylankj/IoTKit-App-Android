package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:58
 */
@Message
public  class MsgMsgCountReq extends MsgpackMsg.MsgHeader {
    public MsgMsgCountReq() {
        msgId = MsgpackMsg.CLIENT_MSGCOUNT_REQ;
    }
}
