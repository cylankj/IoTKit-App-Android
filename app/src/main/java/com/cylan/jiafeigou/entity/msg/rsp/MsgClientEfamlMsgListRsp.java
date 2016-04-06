package com.cylan.jiafeigou.entity.msg.rsp;

import com.cylan.jiafeigou.entity.msg.EfamlMsg;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:32
 */
@Message
public class MsgClientEfamlMsgListRsp extends RspMsgHeader {
    @Index(5)
    public List<EfamlMsg> msgList;
}
