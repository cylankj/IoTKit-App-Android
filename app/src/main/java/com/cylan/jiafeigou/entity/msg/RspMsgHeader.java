package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:36
 */
@Message
public  class RspMsgHeader extends MsgpackMsg.MsgHeader {
    @Index(3)
    public int ret;
    @Index(4)
    public String msg;
}

