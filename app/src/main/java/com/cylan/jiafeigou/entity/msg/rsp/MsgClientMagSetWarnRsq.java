package com.cylan.jiafeigou.entity.msg.rsp;

import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-21
 * Time: 14:17
 */
@Message
public  class MsgClientMagSetWarnRsq extends RspMsgHeader {

    public MsgClientMagSetWarnRsq() {
        msgId = MsgpackMsg.CLIENT_MAG_SET_WARN_RSP;
    }
}
