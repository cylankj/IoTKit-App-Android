package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:27
 */
@Message
public class MsgAudioControl extends MsgpackMsg.MsgHeader {
    public MsgAudioControl(String callee) {
        msgId = MsgpackMsg.ID_AUDIO_CONTROL;
        this.callee = callee;
    }

    @Index(3)
    public boolean mSpeaker;
    @Index(4)
    public boolean mMike;

}
