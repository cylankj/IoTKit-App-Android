package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-11
 * Time: 16:30
 */

@Message
public class ClientDelEfamlMsgReq extends MsgpackMsg.MsgHeader{
    public ClientDelEfamlMsgReq(String callee) {
        msgId = MsgpackMsg.CLIENT_DEL_EFAML_MSG_REQ;
        this.callee = callee;
    }
}
