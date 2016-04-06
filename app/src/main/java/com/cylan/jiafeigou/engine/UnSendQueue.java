package com.cylan.jiafeigou.engine;

import com.cylan.publicApi.JniPlay;

import java.util.LinkedList;
import java.util.Queue;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-11-25
 * Time: 09:41
 */

public class UnSendQueue {
    private Queue<byte[]> mQueue = new LinkedList<>();

    private UnSendQueue() {
    }

    public static UnSendQueue getInstance() {
        return Holder.mQueue;
    }

    private static class Holder {
        private static UnSendQueue mQueue = new UnSendQueue();
    }

    public void clear() {
        mQueue.clear();
    }

    public void addQuene(byte[] str) {
        if (!mQueue.contains(str))
            mQueue.offer(str);
    }

    public void sendQuenueMsg() {
        byte[] request;
        while ((request = mQueue.poll()) != null) {
            JniPlay.SendBytes(request);
        }
    }
}
