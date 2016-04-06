package com.cylan.jiafeigou.entity.msg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-21
 * Time: 14:15
 */
@Message
public  class MagStatusList implements Serializable {
    @Index(0)
    public int status;   // 门磁状态：0-关闭 1-打开
    @Index(1)
    public long time;    // 门磁状态变更时间
}
