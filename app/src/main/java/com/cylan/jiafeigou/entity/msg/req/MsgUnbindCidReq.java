package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:53
 */
@Message
public class MsgUnbindCidReq extends MsgpackMsg.MsgHeader {
    public MsgUnbindCidReq(String caller, String callee) {
        msgId = MsgpackMsg.CLIENT_UNBINDCID_REQ;
        this.caller = caller;
        this.callee = callee;
    }

    @Index(3)
    public String cid;
}