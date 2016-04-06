package com.cylan.jiafeigou.entity.msg.rsp;

import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.entity.msg.MsgTimeData;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:33
 */


@Message
public class MsgHistoryListRsp extends MsgpackMsg.MsgHeader {
    @Index(3)
    public List<MsgTimeData> data;
}
