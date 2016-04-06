package com.cylan.jiafeigou.listener;

import com.cylan.jiafeigou.entity.msg.HttpResult;
import com.cylan.publicApi.MsgpackMsg;

/**
 * Created by hebin on 2015/8/28.
 */
public interface ServerMessage {

    void connectServer();

    void disconnectServer();

    void httpDone(HttpResult rsult);

    void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg);

}
