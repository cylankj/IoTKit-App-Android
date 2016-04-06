package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:52
 */
@Message
public class MsgForgetPassByEmailReq extends MsgpackMsg.MsgHeader {
    public MsgForgetPassByEmailReq(String caller, String callee) {
        msgId = MsgpackMsg.CLIENT_FORGETPASSBYEMAIL_REQ;
        this.caller = caller;
        this.callee = callee;
    }

    @Index(3)
    public int language_type;
    @Index(4)
    public String account;
    @Index(5)
    public String oem;
}
