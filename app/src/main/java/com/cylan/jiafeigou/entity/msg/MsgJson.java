package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:46
 */
@Message
public  class MsgJson extends MsgpackMsg.MsgHeader{
    public MsgJson(String caller, String callee) {
        msgId = MsgpackMsg.ID_JSON;
        this.caller = caller;
        this.callee = callee;
    }

    public MsgJson() {
        msgId = MsgpackMsg.ID_JSON;
    }

    @Index(3)
    public String mJson;
}
