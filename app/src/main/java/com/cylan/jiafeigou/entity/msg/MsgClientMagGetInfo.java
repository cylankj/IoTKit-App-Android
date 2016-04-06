package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-21
 * Time: 14:29
 */
@Message
public class MsgClientMagGetInfo extends MsgpackMsg.MsgHeader {

    public MsgClientMagGetInfo(String caller, String callee) {
        this.caller = caller;
        this.callee = callee;
        msgId = MsgpackMsg.CLIENT_MAG_GET_INFO;
    }
}