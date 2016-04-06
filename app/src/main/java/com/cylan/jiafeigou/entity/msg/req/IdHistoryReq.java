package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:34
 */

@Message
public class IdHistoryReq extends MsgpackMsg.MsgHeader {
    public IdHistoryReq( String callee) {
        msgId = MsgpackMsg.ID_HISTORY;
        this.callee = callee;
    }

    @Index(3)
    public long time;
}
