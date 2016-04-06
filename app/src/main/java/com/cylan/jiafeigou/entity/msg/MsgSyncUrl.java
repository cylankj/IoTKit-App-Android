package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:12
 */
@Message
public  class MsgSyncUrl extends MsgpackMsg.MsgHeader {
    @Index(3)
    public int is_upgrade;
    @Index(4)
    public String url;
}

