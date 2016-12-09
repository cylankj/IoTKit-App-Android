package com.cylan.jiafeigou.n.engine.task;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by cylan-hunt on 16-11-11.
 */

public class OfflineTaskQueue {

    //    private ExecutorService executors = Executors.newSingleThreadExecutor();
    private LinkedBlockingDeque<Runnable> queue = new LinkedBlockingDeque<>();

    private static OfflineTaskQueue instance;

    private static final String TAG = "OfflineTaskQueue:";
    private Handler handler;

    public static OfflineTaskQueue getInstance() {
        if (instance == null)
            instance = new OfflineTaskQueue();
        return instance;
    }

    private OfflineTaskQueue() {
        HandlerThread thread = new HandlerThread("offlineTask");
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    /**
     * @param key:这个key,需要自己组装,非常多坑.不同接口有不同的
     * @param runnable
     */
    public void enqueue(int key, Runnable runnable) {
        queue.offer(runnable);
    }

    /**
     * 后续需要更新
     */
    public void startRolling() {
        Runnable runnable = queue.poll();
        if (runnable != null) {
            handler.post(runnable);
        }
    }
}
