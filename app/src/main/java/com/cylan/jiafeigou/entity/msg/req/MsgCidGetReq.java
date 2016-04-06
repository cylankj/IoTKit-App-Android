package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:07
 */
@Message
public class MsgCidGetReq extends MsgpackMsg.MsgHeader {
    public MsgCidGetReq() {
        msgId = MsgpackMsg.CLIENT_CIDGET_REQ;
    }

    @Index(3)
    public String cid;
}
