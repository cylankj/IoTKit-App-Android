package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:00
 */
@Message
public class MsgMsgClearReq extends MsgpackMsg.MsgHeader {
    public MsgMsgClearReq() {
        msgId = MsgpackMsg.CLIENT_MSGCLEAR_REQ;
    }

    @Index(3)
    public String cid;
}
