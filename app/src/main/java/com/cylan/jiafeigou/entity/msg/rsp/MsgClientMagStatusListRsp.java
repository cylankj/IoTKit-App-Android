package com.cylan.jiafeigou.entity.msg.rsp;

import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.entity.msg.MagStatusList;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-21
 * Time: 14:12
 */

@Message
public  class MsgClientMagStatusListRsp extends RspMsgHeader{
    public MsgClientMagStatusListRsp() {
        msgId = MsgpackMsg.CLIENT_MAG_STATUS_LIST_RSP;
    }

    @Index(5)
    public int curStatus;  //// 门磁当前状态：0-关闭 1-打开
    @Index(6)
    public List<MagStatusList> lists;

}

