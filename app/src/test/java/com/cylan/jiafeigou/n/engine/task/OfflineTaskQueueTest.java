package com.cylan.jiafeigou.n.engine.task;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by cylan-hunt on 16-11-12.
 */
public class OfflineTaskQueueTest {
    @Test
    public void getInstance() throws Exception {
        OfflineTaskQueue offlineTaskQueue0 = OfflineTaskQueue.getInstance();
        OfflineTaskQueue offlineTaskQueue1 = OfflineTaskQueue.getInstance();
        assertTrue("==", offlineTaskQueue0 == offlineTaskQueue1);
        assertTrue("equal", offlineTaskQueue0.equals(offlineTaskQueue1));
    }

    @Test
    public void enqueue() throws Exception {
        final int count = 50 * 10000;
        final long time = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            OfflineTaskQueue.getInstance().enqueue(i, new Runnable() {
                @Override
                public void run() {
                    System.out.println("good: ");
                }
            });
        }
        System.out.println("count: " + OfflineTaskQueue.getInstance().getQueueMap().keySet().size());
        System.out.println("" + (System.currentTimeMillis() - time));
    }

    @Test
    public void getQueueMap() throws Exception {

    }

    @Test
    public void startRolling() throws Exception {
        enqueue();
        OfflineTaskQueue.getInstance().startRolling();
    }

}