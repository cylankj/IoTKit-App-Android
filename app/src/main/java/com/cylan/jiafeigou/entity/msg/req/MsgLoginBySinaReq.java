package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:06
 */
@Message
public class MsgLoginBySinaReq extends ClientLoginReq {
    public MsgLoginBySinaReq() {
        msgId = MsgpackMsg.CLIENT_LOGINBYSINA_REQ;
    }

}
