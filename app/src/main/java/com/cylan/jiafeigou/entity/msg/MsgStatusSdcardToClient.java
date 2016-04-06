package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:11
 */
@Message
public class MsgStatusSdcardToClient extends MsgpackMsg.MsgHeader {
    @Index(3)
    public int net;
    @Index(4)
    public String name;
    @Index(5)
    public String version;
    @Index(6)
    public String sys_version;
    @Index(7)
    public String model;
    @Index(8)
    public long uptime;
    @Index(9)
    public int sdcard;
    @Index(10)
    public int err;
    @Index(11)
    public long storage;
    @Index(12)
    public long used;
    @Index(13)
    public float battery;
    @Index(14)
    public int power;
    @Index(15)
    public String mac;
}
