package com.cylan.jiafeigou.entity.msg.rsp;

import com.cylan.jiafeigou.entity.msg.MsgSystemData;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:02
 */
@Message
public  class MsgMsgSystemRsp extends RspMsgHeader {
    @Index(5)
    public String cid;
    @Index(6)
    public List<MsgSystemData> data;
}
