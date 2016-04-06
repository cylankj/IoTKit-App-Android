package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:24
 */
@Message
public class MsgClientBellCallDeleteReq extends MsgpackMsg.MsgHeader {

    public MsgClientBellCallDeleteReq(String callee) {
        this.callee = callee;
        msgId = MsgpackMsg.CLIENT_BELL_CALL_DELETE_REQ;
    }

    @Index(3)
    public long timeBegin;

}
