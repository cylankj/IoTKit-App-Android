package com.cylan.jiafeigou.n.engine.task;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.MyTestRunner;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Created by cylan-hunt on 16-12-6.
 */
@RunWith(MyTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class OfflineTaskQueueTest {

    @Test
    public void getInstance() throws Exception {
        System.out.println("waht");
        Assert.assertNotNull("not null: ", OfflineTaskQueue.getInstance() != null);
    }

    @Test
    public void enqueue() throws Exception {
        OfflineTaskQueue queue = OfflineTaskQueue.getInstance();
        Assert.assertTrue(queue != null);
        queue.enqueue(2, new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("let's run");
            }
        });
//        queue.test();
    }

    @Test
    public void getQueueMap() throws Exception {

    }

    @Test
    public void startRolling() throws Exception {

    }

}