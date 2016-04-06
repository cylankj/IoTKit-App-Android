package com.cylan.jiafeigou.entity.msg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:25
 */

@Message
public class MsgServer implements Serializable {
    @Index(0)
    public String ip;
    @Index(1)
    public int port;
}