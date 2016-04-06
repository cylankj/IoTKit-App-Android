package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:03
 */
@Message
public  class MsgMsgDeleteReq extends MsgpackMsg.MsgHeader {
    public MsgMsgDeleteReq() {
        msgId = MsgpackMsg.CLIENT_MSGDELETE_REQ;
    }

    @Index(3)
    public String cid;
    @Index(4)
    public List<Long> timelist;
    @Index(5)
    public int delete;//新增  1：正向删除，-1：反向删除
}
