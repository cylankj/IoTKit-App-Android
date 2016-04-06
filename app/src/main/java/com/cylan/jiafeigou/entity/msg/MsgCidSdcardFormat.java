package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:10
 */
@Message
public class MsgCidSdcardFormat extends MsgpackMsg.MsgHeader {
    public MsgCidSdcardFormat(String callee) {
        msgId = MsgpackMsg.CLIENT_SDCARD_FORMAT;
        this.callee = callee;
    }

}
