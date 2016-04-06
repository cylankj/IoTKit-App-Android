package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:04
 */
@Message
public  class MsgShareReq extends MsgpackMsg.MsgHeader {
    public MsgShareReq() {
        msgId = MsgpackMsg.CLIENT_SHARE_REQ;
    }

    @Index(3)
    public String cid;
    @Index(4)
    public String account;
}

