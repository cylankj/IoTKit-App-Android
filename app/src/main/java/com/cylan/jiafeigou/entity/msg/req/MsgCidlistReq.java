package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:56
 */
@Message
public class MsgCidlistReq extends MsgpackMsg.MsgHeader {
    public MsgCidlistReq(String caller) {
        msgId = MsgpackMsg.CLIENT_CIDLIST_REQ;
        this.caller = caller;
    }

}
