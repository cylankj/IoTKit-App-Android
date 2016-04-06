package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:28
 * 中控关联门铃
 */

@Message
public  class MsgClientEfamlSetBellReq extends MsgpackMsg.MsgHeader {
    public MsgClientEfamlSetBellReq(String callee) {
        this.callee = callee;
        msgId = MsgpackMsg.CLIENT_EFAML_SET_BELL_REQ;
    }

    @Index(3)
    public List<String> list;
}
