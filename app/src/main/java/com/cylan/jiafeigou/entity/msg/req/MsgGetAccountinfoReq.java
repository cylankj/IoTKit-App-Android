package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:55
 */
@Message
public class MsgGetAccountinfoReq extends MsgpackMsg.MsgHeader {
    public MsgGetAccountinfoReq() {
        msgId = MsgpackMsg.CLIENT_GETACCOUNTINFO_REQ;
    }
}
