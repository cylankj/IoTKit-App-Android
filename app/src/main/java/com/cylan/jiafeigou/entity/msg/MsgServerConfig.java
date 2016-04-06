package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:31
 */
@Message
public class MsgServerConfig extends MsgServers {

    public MsgServerConfig() {
        msgId = MsgpackMsg.SERVER_CONFIG;
    }

    @Index(4)
    public int heartbeat;
}
