package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:29
 * 获取中控关联门铃列表
 * 将callee填充为中控cid
 */

@Message
public  class MsgClientEfamlGetBellsReq extends MsgpackMsg.MsgHeader {
    public MsgClientEfamlGetBellsReq(String callee) {
        this.callee = callee;
        msgId = MsgpackMsg.CLIENT_EFAML_GET_BELLS_REQ;
    }

}
