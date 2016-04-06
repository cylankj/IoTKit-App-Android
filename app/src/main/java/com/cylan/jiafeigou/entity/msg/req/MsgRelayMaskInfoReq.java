package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:23
 */
@Message
public class MsgRelayMaskInfoReq extends MsgpackMsg.MsgHeader {

    public MsgRelayMaskInfoReq(String callee) {
        this.callee = callee;
        msgId = MsgpackMsg.ID_RELAY_MASK_INFO_REQ;
    }

}
