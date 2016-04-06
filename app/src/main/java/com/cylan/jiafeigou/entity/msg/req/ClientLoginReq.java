package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:38
 */
@Message
public  class ClientLoginReq extends MsgpackMsg.MsgHeader {
    public ClientLoginReq() {
        msgId = MsgpackMsg.CLIENT_LOGIN_REQ;
    }

    @Index(3)
    public int language_type;
    @Index(4)
    public String account;
    @Index(5)
    public String pass;
    @Index(6)
    public int os;
    @Index(7)
    public String version;
    @Index(8)
    public String sys_version;
    @Index(9)
    public String model;
    @Index(10)
    public int net;
    @Index(11)
    public String name;
    @Index(12)
    public long time;
    @Index(13)
    public String bundleId;
    @Index(14)
    public String device_token;
    @Index(15)
    public String alias;
    @Index(16)
    public int register_type;
    @Index(17)
    public String code;
    @Index(18)
    public String newpass;
    @Index(19)
    public String sessid;
    @Index(20)
    public String oem;

}
