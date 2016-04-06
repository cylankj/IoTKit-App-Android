package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:44
 */
@Message
public class MsgChangePassReq extends ClientLoginReq {
    public MsgChangePassReq() {
        msgId = MsgpackMsg.CLIENT_CHANGEPASS_REQ;
    }
}
