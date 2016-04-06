package com.cylan.jiafeigou.entity.msg.rsp;

import com.cylan.jiafeigou.entity.msg.EfamilyVoicemsgData;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:20
 */
@Message
public  class MsgEfamilyVoicemsgListRsp extends RspMsgHeader {
    @Index(5)
    public List<EfamilyVoicemsgData> data;
}
