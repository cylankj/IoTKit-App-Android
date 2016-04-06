package com.cylan.jiafeigou.entity.msg.rsp;

import com.cylan.jiafeigou.entity.msg.RspMsgHeader;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:55
 */
@Message
public class MsgSetCidAliasRsp extends RspMsgHeader {
    @Index(5)
    public String cid;
    @Index(6)
    public String alias;
}
