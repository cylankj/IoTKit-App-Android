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
public class MsgMsgListReq extends MsgpackMsg.MsgHeader {
    public MsgMsgListReq() {
        msgId = MsgpackMsg.CLIENT_MSGLIST_REQ;
    }

    @Index(3)
    public String cid;
    @Index(4)
    public long time;
}
