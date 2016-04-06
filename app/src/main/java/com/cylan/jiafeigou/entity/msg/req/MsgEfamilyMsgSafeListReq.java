package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:21
 */
@Message
public class MsgEfamilyMsgSafeListReq extends MsgpackMsg.MsgHeader {
    public MsgEfamilyMsgSafeListReq(String caller, String callee) {
        msgId = MsgpackMsg.CLIENT_EFAML_MSG_SAFE_LIST_REQ;
        this.caller = caller;
        this.callee = callee;
    }

    @Index(3)
    public long time;
}
