package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:25
 */
@Message
public  class MsgIdBellConnected extends MsgpackMsg.MsgHeader {
    public MsgIdBellConnected() {
        msgId = MsgpackMsg.ID_BELL_CONNECTED;
    }
}
