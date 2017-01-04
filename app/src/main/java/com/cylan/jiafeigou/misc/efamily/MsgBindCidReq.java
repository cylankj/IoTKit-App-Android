package com.cylan.jiafeigou.misc.efamily;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * 兼容中控绑定
 * Created by cylan-hunt on 17-1-3.
 */

@Message
public class MsgBindCidReq extends MsgpackMsg.MsgHeader {

    public MsgBindCidReq() {
    }

    public MsgBindCidReq(String callee) {
        this.callee = callee;
        this.msgId = 16218;
    }

    @Index(4)
    public String cid;
    @Index(5)
    public int is_rebind;
    @Index(6)
    public String timezone;
    @Index(7)
    public String alias;
    @Index(8)
    public String mac;

    @Override
    public String toString() {
        return "MsgBindCidReq{" +
                "cid='" + cid + '\'' +
                ", msgId='" + msgId + '\'' +
                ", is_rebind=" + is_rebind +
                ", timezone='" + timezone + '\'' +
                ", alias='" + alias + '\'' +
                ", mac='" + mac + '\'' +
                '}';
    }
}