package com.cylan.jiafeigou.entity.msg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:21
 */

@Message
public class EfamilyVoicemsgData {
    @Index(0)
    public int timeBegin;
    @Index(1)
    public int timeDuration;
    @Index(2)
    public int isRead;// 非0已读
    @Index(3)
    public String url;// 在线地址
}
