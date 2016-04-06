package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:54
 */
@Message
public class MsgSetCidAliasReq extends MsgpackMsg.MsgHeader {
    public MsgSetCidAliasReq() {
        msgId = MsgpackMsg.CLIENT_SETCIDALIAS_REQ;
    }

    @Index(3)
    public String cid;
    @Index(4)
    public String alias;
}

