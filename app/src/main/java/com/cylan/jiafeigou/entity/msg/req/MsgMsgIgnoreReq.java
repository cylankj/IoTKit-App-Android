package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:01
 */
@Message
public class MsgMsgIgnoreReq extends MsgpackMsg.MsgHeader {
    public MsgMsgIgnoreReq() {
        msgId = MsgpackMsg.CLIENT_MSGIGNORE_REQ;
    }
}
