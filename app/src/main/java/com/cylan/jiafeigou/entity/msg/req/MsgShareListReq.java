package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:05
 */
@Message
public  class MsgShareListReq extends MsgpackMsg.MsgHeader {
    public MsgShareListReq() {
        msgId = MsgpackMsg.CLIENT_SHARELIST_REQ;
    }

    @Index(3)
    public String cid;
}
