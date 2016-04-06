package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:45
 */
@Message
public  class MsgGetCodeReq extends MsgpackMsg.MsgHeader {
    public MsgGetCodeReq() {
        msgId = MsgpackMsg.CLIENT_GET_CODE;
    }

    @Index(3)
    public int language_type;
    @Index(4)
    public String account;
    @Index(5)
    public int type;
    @Index(6)
    public String oem;
}
