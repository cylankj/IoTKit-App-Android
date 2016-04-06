package com.cylan.jiafeigou.entity.msg.rsp;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:42
 */
@Message
public  class MsgCidLoginRsp extends MsgpackMsg.MsgHeader {
    @Index(3)
    public int ret;
    @Index(4)
    public String msg;
    @Index(5)
    public String sessid;
    @Index(6)
    public int os;
    @Index(7)
    public String version;
}
