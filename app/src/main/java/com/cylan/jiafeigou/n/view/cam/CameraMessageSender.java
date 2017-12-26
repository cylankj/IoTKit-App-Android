package com.cylan.jiafeigou.n.view.cam;

/**
 * Created by yanzhendong on 2017/12/26.
 */

public class CameraMessageSender {

    public static class Message {
        public int msgId;
        public Object message;
    }

    public interface MessageObserver {
        void onReceiveMessage(Message message);
    }

    public void sendMessage(Message message) {

    }

    public void observeMessage(int msgId) {

    }


    public void observeMessages(MessageObserver observer) {

    }

}
