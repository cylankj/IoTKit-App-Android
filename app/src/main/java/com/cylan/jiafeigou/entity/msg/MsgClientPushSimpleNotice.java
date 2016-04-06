package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:31
 * 通知客户端
 */

@Message
public class MsgClientPushSimpleNotice extends MsgpackMsg.MsgHeader {

    @Ignore
    public static final int TYPE_EFAML_UNREAD=0x01;
    @Ignore
    public static final int TYPE_EFAML_UPDATE=0x02;

    @Index(3)
    public int push_type;
    @Index(4)
    public int count;
}
