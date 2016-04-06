package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:02
 */
@Message
public  class MsgMsgSystemReq extends MsgpackMsg.MsgHeader {
    public MsgMsgSystemReq() {
        msgId = MsgpackMsg.CLIENT_MSGSYSTEM_REQ;
    }
}
