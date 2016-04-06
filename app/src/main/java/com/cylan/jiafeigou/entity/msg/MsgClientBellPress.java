package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:29
 */

@Message
public  class MsgClientBellPress extends MsgpackMsg.MsgHeader {
    @Index(3)
    public long time;
    @Index(4)
    public String url;
}