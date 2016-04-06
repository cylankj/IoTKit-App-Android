package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:41
 */
@Message
public class MsgSetPassReq extends ClientLoginReq {
    public MsgSetPassReq() {
        msgId = MsgpackMsg.CLIENT_SETPASS_REQ;
    }
}
