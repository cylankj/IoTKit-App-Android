package com.cylan.jiafeigou.misc.efamily;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * Created by cylan-hunt on 17-1-3.
 */

@Message
public class MsgBindCidRsp extends RspMsgHeader {
    @Index(6)
    public String cid;
}

