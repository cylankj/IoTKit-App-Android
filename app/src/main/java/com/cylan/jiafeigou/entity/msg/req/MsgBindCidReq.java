package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:45
 */
@Message
public  class MsgBindCidReq extends MsgpackMsg.MsgHeader implements Serializable {
    public MsgBindCidReq(String callee) {
        this.callee = callee;
        this.msgId = MsgpackMsg.CLIENT_BINDCID_REQ;
    }

    @Index(3)
    public String cid;
    @Index(4)
    public int is_rebind;
    @Index(5)
    public String timezone;
    @Index(6)
    public String alias;
    @Index(7)
    public String mac;

}

