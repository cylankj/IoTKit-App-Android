package com.cylan.jiafeigou.entity.msg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:34
 */
@Message
public class MsgTimeData implements Comparable<MsgTimeData> {
    @Index(0)
    public long begin;
    @Index(1)
    public int time;

    @Override
    public int compareTo(MsgTimeData another) {
        return this.begin < another.begin ? -1 : 1;
    }

    @Override
    public boolean equals(Object bean) {
        if (bean == null)
            return false;
        return !((this.begin != ((MsgTimeData) bean).begin) || (this.begin != ((MsgTimeData) bean).time));
    }


}

