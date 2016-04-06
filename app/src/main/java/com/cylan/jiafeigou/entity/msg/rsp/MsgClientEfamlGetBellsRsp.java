package com.cylan.jiafeigou.entity.msg.rsp;


import com.cylan.jiafeigou.entity.msg.RspMsgHeader;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:29
 * 返回关联门铃列表
 */

@Message
public  class MsgClientEfamlGetBellsRsp extends RspMsgHeader {

    @Index(5)
    public List<String> array;

}
