package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:07
 */
@Message
public  class MsgCidSetReq extends MsgpackMsg.MsgHeader {
    public MsgCidSetReq() {
        msgId = MsgpackMsg.CLIENT_CIDSET_REQ;
    }

    @Index(3)
    public String cid;
    @Index(4)
    public int warn_enable;
    @Index(5)
    public int warn_begin_time;
    @Index(6)
    public int warn_end_time;
    @Index(7)
    public int warn_week;
    @Index(8)
    public int led;
    @Index(9)
    public int sound;
    @Index(10)
    public int direction;
    @Index(11)
    public String timezonestr;
    @Index(12)
    public int sound_long;
    @Index(13)
    public int auto_record;
    @Index(14)
    public int location;
    @Index(15)
    public int isNTSC;
    @Index(16)
    public int isMobile;
    @Index(17)
    public int sensitivity;

}
