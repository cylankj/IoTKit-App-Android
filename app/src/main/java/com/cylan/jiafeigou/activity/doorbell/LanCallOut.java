package com.cylan.jiafeigou.activity.doorbell;

import com.cylan.publicApi.JniPlay;
import com.cylan.jiafeigou.engine.ClientUDP;
import com.cylan.jiafeigou.utils.Utils;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-12-28
 * Time: 18:18
 */

public class LanCallOut implements IPlayOrStop {

    private ClientUDP mScanUdpManager;
    private ClientUDP.JFG_F_PONG mPong;

    public LanCallOut(ClientUDP manager, ClientUDP.JFG_F_PONG pong) {
        this.mScanUdpManager = manager;
        this.mPong = pong;
    }

    @Override
    public void makeCall() {
        int randomPort = Utils.getRandom(10000, 20000);
        mScanUdpManager.sendFPlay(mPong, String.valueOf(randomPort));
        JniPlay.StartFactoryMediaRecv(randomPort);
    }

    @Override
    public void stop() {
        mScanUdpManager.sendFStop(mPong);
        JniPlay.DisconnectFromPeer();
    }
}
