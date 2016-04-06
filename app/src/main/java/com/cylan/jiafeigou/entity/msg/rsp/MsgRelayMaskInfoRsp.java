package com.cylan.jiafeigou.entity.msg.rsp;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;
import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:24
 */
@Message
public  class MsgRelayMaskInfoRsp extends MsgpackMsg.MsgHeader implements Serializable {

    @Index(3)
    public List<Integer> mask_list;
}
