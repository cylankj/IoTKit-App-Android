package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:19
 */
@Message
public  class MsgEfamilyListReq extends MsgpackMsg.MsgHeader {
    public MsgEfamilyListReq(String callee) {
        msgId = MsgpackMsg.CLIENT_EFAML_LIST_REQ;
        this.callee = callee;
    }
}
