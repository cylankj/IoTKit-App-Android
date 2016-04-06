package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:08
 */
@Message
public class MsgSyncCidOnline extends MsgpackMsg.MsgHeader {
    @Index(3)
    public String cid;
    @Index(4)
    public int net;
    @Index(5)
    public String name;
    @Index(6)
    public String version;
}

