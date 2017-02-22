package com.cylan.jiafeigou.misc.live;


import com.cylan.entity.jniCall.JFGMsgVideoRtcp;

import org.junit.Test;

import java.util.Random;

/**
 * Created by cylan-hunt on 17-2-22.
 */
public class LiveFrameRateMonitorTest implements IFeedRtcp.MonitorListener {

    private IFeedRtcp feedRtcp;

    @Test
    public void feed() throws Exception {
        feedRtcp = new LiveFrameRateMonitor();
        feedRtcp.setMonitorListener(this);
        System.out.println(feedRtcp);
        new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                JFGMsgVideoRtcp rtcp = new JFGMsgVideoRtcp();
                rtcp.frameRate = new Random().nextInt(4);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                feedRtcp.feed(rtcp);
                System.out.println("i: " + i + "-->" + rtcp.frameRate);
            }
        }).start();
        Thread.sleep(5000);
    }

    @Test
    public void stop() throws Exception {

    }


    @Override
    public void onFrameFailed() {
        System.out.println("onFrameRateFailed");
    }

    @Override
    public void onFrameRate(boolean slow) {
        System.out.println("onFrameRate:" + slow);
    }
}