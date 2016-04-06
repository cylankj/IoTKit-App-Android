package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-21
 * Time: 14:26
 */
@Message
public  class MsgClientMagStatusListReq extends MsgpackMsg.MsgHeader{
    public MsgClientMagStatusListReq(String callee) {
        this.callee = callee;
        msgId = MsgpackMsg.CLIENT_MAG_STATUS_LIST_REQ;
    }

    @Index(3)
    public long timeBegin;
    @Index(4)
    public long timeEnd;
}
