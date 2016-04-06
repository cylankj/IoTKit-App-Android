package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-21
 * Time: 14:33
 */

@Message
public class MsgClientMagSetWarnReq extends MsgpackMsg.MsgHeader{

    public MsgClientMagSetWarnReq(String caller, String callee) {
        this.caller = caller;
        this.callee = callee;
        msgId = MsgpackMsg.CLIENT_MAG_SET_WARN_REQ;
    }

    @Index(3)
    public int warn;
}

