package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:39
 */
@Message
public class ClientReloginReq extends ClientLoginReq {
    public ClientReloginReq() {
        msgId = MsgpackMsg.CLIENT_RELOGIN_REQ;
    }

}
