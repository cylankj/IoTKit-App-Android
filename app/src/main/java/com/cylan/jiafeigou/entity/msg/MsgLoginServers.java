package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:32
 */

@Message
public class MsgLoginServers extends MsgpackMsg.MsgHeader{
    @Index(3)
    public List<MsgServer> mServers;
}
