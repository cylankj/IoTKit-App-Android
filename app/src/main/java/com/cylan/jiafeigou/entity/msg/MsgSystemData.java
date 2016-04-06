package com.cylan.jiafeigou.entity.msg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:03
 */
@Message
public class MsgSystemData {
    @Index(0)
    public int push_type;
    @Index(1)
    public String title;
    @Index(2)
    public String cnt;
    @Index(3)
    public long time;
    @Index(4)
    public int os;
}
