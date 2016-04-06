package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:33
 */

@Message
public class MsgHistoryListReq extends MsgpackMsg.MsgHeader {
    public MsgHistoryListReq(String callee) {
        msgId = MsgpackMsg.ID_HISTORY_LIST;
        this.callee = callee;
    }
}
