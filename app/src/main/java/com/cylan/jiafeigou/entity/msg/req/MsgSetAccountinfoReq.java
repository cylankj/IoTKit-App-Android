package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:55
 */
@Message
public class MsgSetAccountinfoReq extends MsgpackMsg.MsgHeader{
    public MsgSetAccountinfoReq(String caller, String callee) {
        msgId = MsgpackMsg.CLIENT_SETACCOUNTINFO_REQ;
        this.caller = caller;
        this.callee = callee;
    }

    @Index(3)
    public String sms_phone;
    @Index(4)
    public String code;
    @Index(5)
    public String alias;
    @Index(6)
    public int push_enable;
    @Index(7)
    public int vibrate;
    @Index(8)
    public int sound;
    @Index(9)
    public String email;
}
