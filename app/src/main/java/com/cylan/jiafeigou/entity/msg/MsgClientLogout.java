package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:28
 */
@Message
public  class MsgClientLogout extends MsgpackMsg.MsgHeader {
    public MsgClientLogout() {
        msgId = MsgpackMsg.CLIENT_LOGOUT;
    }
}
