package com.cylan.jiafeigou.entity.msg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;

@Message
public class MsgCidGetSetParent extends RspMsgHeader implements Serializable {
    @Index(5)
    public int nret;
    @Index(6)
    public int vid;
    @Index(7)
    public String cid;
    @Index(8)
    public String timezonestr;
    //    warn_enable, warn_begin_time, warn_end_time, warn_week, led, sound, direction, timezone, pushflow, sound_long, auto_record
    @Index(9)
    public int warn_enable;
    @Index(10)
    public int warn_begin_time;
    @Index(11)
    public int warn_end_time;
    @Index(12)
    public int warn_week;
    @Index(13)
    public int led;
    @Index(14)
    public int sound;
    @Index(15)
    public int direction;
    @Index(16)
    public int timezone;
    @Index(17)
    public int pushflow;
    @Index(18)
    public int sound_long;
    @Index(19)
    public int auto_record;
    @Index(20)
    public int location;
    @Index(21)
    public int isNTSC;
    @Index(22)
    public int isMobile;
    @Index(23)
    public int sensitivity;
}
