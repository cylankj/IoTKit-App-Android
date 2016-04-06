package com.cylan.jiafeigou.entity.msg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:35
 */

@Message
public  class HttpResult {
    @Index(0)
    public int ret;
    @Index(1)
    public int requestId;
    @Index(2)
    public String result;
}
