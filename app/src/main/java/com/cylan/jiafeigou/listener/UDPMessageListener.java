package com.cylan.jiafeigou.listener;

import com.cylan.jiafeigou.engine.ClientUDP;

/**
 * Created by hebin on 2015/9/25.
 */
public interface UDPMessageListener {
    void JfgMsgPong(ClientUDP.JFG_PONG jfg);

    void JfgMsgSetWifiRsp(ClientUDP.JFG_RESPONSE rsp);

    void JfgMsgFPong(ClientUDP.JFG_F_PONG req);

    void JfgMsgFAck(ClientUDP.JFG_F_ACK ack);

    void JfgMsgBellPress(ClientUDP.JFGCFG_HEADER data);

}
