package com.cylan.jiafeigou.n.view.cam;

/**
 * Created by yanzhendong on 2017/12/26.
 */

public class CameraLiveViewModel implements CameraMessageSender.MessageObserver {
    private CameraMessageSender messageSender;

    public void attachMessageSender(CameraMessageSender sender) {
        this.messageSender = sender;
        this.messageSender.observeMessages(this);
    }

    @Override
    public void onReceiveMessage(CameraMessageSender.Message message) {

    }
}
