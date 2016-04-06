package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:26
 */
@Message
public class MsgClientHasMobileReq extends MsgpackMsg.MsgHeader {
    public MsgClientHasMobileReq(String callee) {
        this.callee = callee;
        msgId = MsgpackMsg.CLIENT_HAS_MOBILE_REQ;
    }
}
