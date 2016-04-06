package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:11
 */
@Message
public  class MsgStatusSdcardToCid extends MsgpackMsg.MsgHeader {
    public MsgStatusSdcardToCid(String caller, String callee) {
        msgId = MsgpackMsg.CLIENT_STATUS;
        this.caller = caller;
        this.callee = callee;
    }

    @Index(3)
    public String cid;
}
