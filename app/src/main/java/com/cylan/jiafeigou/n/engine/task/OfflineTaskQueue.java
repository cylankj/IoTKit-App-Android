package com.cylan.jiafeigou.n.engine.task;

import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cylan-hunt on 16-11-11.
 */

public class OfflineTaskQueue {
    private static final String TAG = "OfflineTaskQueue";
    private ExecutorService executors = Executors.newSingleThreadExecutor();
    private static OfflineTaskQueue instance;
    private volatile boolean isWorking;

    public static OfflineTaskQueue getInstance() {
        if (instance == null)
            instance = new OfflineTaskQueue();
        return instance;
    }

    private OfflineTaskQueue() {
    }

    private Map<Integer, Runnable> queueMap = new HashMap<>();

    /**
     * @param key:这个key,需要自己组装,非常多坑.不同接口有不同的
     * @param runnable
     */
    public void enqueue(int key, Runnable runnable) {
        if (queueMap == null)
            queueMap = new HashMap<>();
        queueMap.put(key, runnable);
    }

    public Map<Integer, Runnable> getQueueMap() {
        return queueMap;
    }

    /**
     * 后续需要更新
     */
    public void startRolling() {
        if (isWorking || queueMap.keySet().size() == 0)
            return;
        isWorking = true;
        Iterator<Integer> iterator = queueMap.keySet().iterator();
        while (iterator.hasNext()) {
            final int key = iterator.next();
            executors.submit(queueMap.get(key));
            AppLogger.i(TAG + "start rollll..." + queueMap.size());
        }
        //绝对有问题,应该是一个blockingQueue
        queueMap.clear();
        //work done
        isWorking = false;
    }
}
