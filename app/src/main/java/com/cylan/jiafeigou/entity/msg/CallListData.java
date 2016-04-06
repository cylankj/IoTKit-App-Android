package com.cylan.jiafeigou.entity.msg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:14
 */
@Message
public class CallListData implements Serializable {
    @Index(0)
    public int isOK; //是否接听
    @Index(1)
    public long timeBegin; //呼叫开始时间
    @Index(2)
    public String url;
}
