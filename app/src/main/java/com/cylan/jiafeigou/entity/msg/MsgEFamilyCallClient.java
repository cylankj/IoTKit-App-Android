package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-21
 * Time: 14:10
 */

@Message
public class MsgEFamilyCallClient extends MsgpackMsg.MsgHeader {

    public MsgEFamilyCallClient(String caller, String callee) {
        this.caller = caller;
        this.callee = callee;
        msgId = MsgpackMsg.EFAML_CALL_CLIENT;
    }

    public MsgEFamilyCallClient() {
        msgId = MsgpackMsg.EFAML_CALL_CLIENT;
    }

    @Index(3)
    public int time;
    @Index(4)
    public String url;
}
