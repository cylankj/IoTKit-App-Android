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
public  class MsgSyncCidOffline extends MsgpackMsg.MsgHeader {
    @Index(3)
    public String cid;
}
