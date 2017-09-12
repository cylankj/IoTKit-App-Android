package com.cylan.jiafeigou.misc;

import com.cylan.jiafeigou.utils.RandomUtils;

import org.junit.Test;

/**
 * Created by hds on 17-9-8.
 */
public class PingDeviceTest {


    private final Object object = new Object();

    @Test
    public void testWait() {
        long start = System.currentTimeMillis();
        System.out.println("what the hell?");
        synchronized (object) {
            try {
                object.wait(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("done:" + (System.currentTimeMillis() - start));
        new Thread(() -> {
            int time = RandomUtils.getRandom(500);
            try {
                Thread.sleep(3100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            object.notifyAll();
        }).start();
    }

}