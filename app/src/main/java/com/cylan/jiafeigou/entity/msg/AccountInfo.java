package com.cylan.jiafeigou.entity.msg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:56
 */
@Message
public class AccountInfo extends RspMsgHeader implements Serializable {
    @Index(5)
    public int vid;
    @Index(6)
    public long register_time;
    @Index(7)
    public String sms_phone;
    @Index(8)
    public String alias;
    @Index(9)
    public int push_enable;
    @Index(10)
    public int vibrate;
    @Index(11)
    public int sound;
    @Index(12)
    public String email;
}
