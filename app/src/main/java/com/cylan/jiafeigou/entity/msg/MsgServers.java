package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:30
 */


@Message
public class MsgServers extends MsgpackMsg.MsgHeader {
    public MsgServers() {
        msgId = MsgpackMsg.SERVER_CONFIG;
    }

    @Index(3)
    public List<MsgServer> mServers;

}