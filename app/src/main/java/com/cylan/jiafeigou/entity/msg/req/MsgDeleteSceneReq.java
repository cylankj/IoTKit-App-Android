package com.cylan.jiafeigou.entity.msg.req;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:57
 */

@Message
public class MsgDeleteSceneReq extends MsgpackMsg.MsgHeader {
    public MsgDeleteSceneReq(String caller) {
        msgId = MsgpackMsg.CLIENT_DELETESCENE_REQ;
        this.caller = caller;
    }

    @Index(3)
    public int scene_id;
}
