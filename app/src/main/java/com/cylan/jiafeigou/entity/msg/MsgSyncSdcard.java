package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:09
 */
@Message
public  class MsgSyncSdcard extends MsgpackMsg.MsgHeader {
    @Index(3)
    public int sdcard;
    @Index(4)
    public long storage;
    @Index(5)
    public long used;
    @Index(6)
    public int err;
}
