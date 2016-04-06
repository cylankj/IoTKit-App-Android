package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:22
 */
@Message
public  class MsgRelayServer extends MsgpackMsg.MsgHeader {
    @Index(3)
    public List<String> iplist;
    @Index(4)
    public List<Integer> portlist;
}
