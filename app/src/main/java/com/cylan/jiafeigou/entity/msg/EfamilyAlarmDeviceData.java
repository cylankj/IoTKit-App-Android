package com.cylan.jiafeigou.entity.msg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:18
 */
@Message
public class EfamilyAlarmDeviceData implements Serializable {
    @Index(0)
    public String cid;
    @Index(1)
    public int os;
    @Index(2)
    public String alias;
    @Index(3)
    public int warn_enable;
}

