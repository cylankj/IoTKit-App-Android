package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:42
 */
@Message
public class MsgCidLogin extends MsgpackMsg.MsgHeader {
    @Index(3)
    public String cid;
    @Index(4)
    public int net;
    @Index(5)
    public String name;
    @Index(6)
    public int os;
    @Index(7)
    public String version;
    @Index(8)
    public String sys_version;
    @Index(9)
    public int uptime;
    @Index(10)
    public String mac;
}
