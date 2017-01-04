package com.cylan.jiafeigou.misc.efamily;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * Created by cylan-hunt on 17-1-3.
 */

@Message
public class RspMsgHeader extends MsgpackMsg.MsgHeader {
    @Index(4)
    public int ret;
    @Index(5)
    public String msg;

    @Override
    public String toString() {
        return "RspMsgHeader{" +
                "ret=" + ret +
                ", msg='" + msg + '\'' +
                '}';
    }
}