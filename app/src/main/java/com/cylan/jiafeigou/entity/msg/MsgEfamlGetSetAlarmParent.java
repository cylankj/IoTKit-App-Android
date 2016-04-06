package com.cylan.jiafeigou.entity.msg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;
import java.util.List;

@Message
public class MsgEfamlGetSetAlarmParent extends RspMsgHeader implements Serializable {

    @Index(5)
    public int nret;
    @Index(6)
    public int vid;
    @Index(7)
    public int warn_begin_time;
    @Index(8)
    public int warn_end_time;
    @Index(9)
    public int warn_week;
    @Index(10)
    public List<EfamilyAlarmDeviceData> data;
    @Index(11)
    public int location;
}
