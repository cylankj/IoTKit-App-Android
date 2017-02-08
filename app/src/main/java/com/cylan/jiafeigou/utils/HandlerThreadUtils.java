package com.cylan.jiafeigou.utils;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by hunt on 16-5-6.
 */
public class HandlerThreadUtils {

    public static Handler mHandler;

    static {
        HandlerThread mHandlerThread = new HandlerThread("worker-handler-thread");

        mHandlerThread.start();
        mHandler = new
                Handler(mHandlerThread.getLooper());
    }

    public static void post(Runnable runnable) {
        mHandler.post(runnable);
    }

    public static void postAtTime(Runnable runnable, long uptimeMillis) {
        mHandler.postAtTime(runnable, uptimeMillis);
    }

    public static void postAtFrontOfQueue(Runnable runnable) {
        mHandler.postAtFrontOfQueue(runnable);
    }

    public static void postDelay(Runnable runnable, long delayMillis) {
        mHandler.postDelayed(runnable, delayMillis);
    }

    public static void postAtTime(Runnable runnable, Object token, long uptimeMillis) {
        mHandler.postAtTime(runnable, token, uptimeMillis);
    }
}
