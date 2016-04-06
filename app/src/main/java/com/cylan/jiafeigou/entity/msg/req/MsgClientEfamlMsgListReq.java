package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:31
 */
@Message
public class MsgClientEfamlMsgListReq extends MsgpackMsg.MsgHeader {
    public MsgClientEfamlMsgListReq( String callee) {
        this.callee = callee;
        msgId = MsgpackMsg.CLIENT_EFAML_MSG_LIST_REQ;
    }

    @Index(3)
    public long time;
}
