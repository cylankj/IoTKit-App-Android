package com.cylan.jiafeigou.entity.msg.rsp;

import com.cylan.jiafeigou.entity.msg.RspMsgHeader;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:03
 */
@Message
public class MsgMsgDeleteRsp extends RspMsgHeader {
    @Index(5)
    public String cid;
    @Index(6)
    public List<Long> timelist;
    @Index(7)
    public int delete;
}
