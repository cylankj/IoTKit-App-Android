package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:40
 */
@Message
public  class MsgRegisterReq extends ClientLoginReq {
    public MsgRegisterReq() {
        msgId = MsgpackMsg.CLIENT_REGISTER_REQ;
    }
}
