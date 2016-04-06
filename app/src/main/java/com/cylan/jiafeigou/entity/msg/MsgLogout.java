package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:43
 */
@Message
public  class MsgLogout extends MsgpackMsg.MsgHeader{
    public MsgLogout(String caller, String callee) {
        msgId = MsgpackMsg.LOGOUT;
        this.caller = caller;
        this.callee = callee;
    }
}
