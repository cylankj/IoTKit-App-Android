package com.cylan.jiafeigou.entity.msg.rsp;

import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-21
 * Time: 14:18
 */
@Message
public  class MsgClientMagGetWarnRsp extends RspMsgHeader {

    public MsgClientMagGetWarnRsp() {
        msgId = MsgpackMsg.CLIENT_MAG_GET_WARN_RSP;
    }

    @Index(5)
    public int warn;
}