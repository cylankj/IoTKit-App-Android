package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:09
 */
@Message
public class MsgPush extends MsgpackMsg.MsgHeader {
    @Index(3)
    public int push_type;
    @Index(4)
    public String cid;
    @Index(5)
    public String account;
    @Index(6)
    public long time;
    @Index(7)
    public String version;
    @Index(8)
    public String binding_account;
    @Index(9)
    public int err;
    @Index(10)
    public String alias;
    @Index(11)
    public int count;
    @Index(12)
    public int type;
    @Index(13)
    public String title;
    @Index(14)
    public List<String> urllist;
    @Index(15)
    public String share_account;
    @Index(16)
    public int video_time;
    @Index(17)
    public int os;
}

