package com.cylan.jiafeigou.utils;

import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.cylan.jiafeigou.worker.ConfigWIfiWorker;
import com.cylan.jiafeigou.worker.EnableWifiWorker;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-12-22
 * Time: 13:48
 */

public class WifiUtils {
    private static final String TAG = "WifiUtils";

    public static void configWifi(ConfigWIfiWorker worker, Handler mHandler) {
        if (Build.VERSION.SDK_INT >= 23) {
            startWorkThread(worker);
            Log.d(TAG, "config wifi by thread");
        } else {
            mHandler.post(worker);
            Log.d(TAG, "config wifi by main thread");
        }
    }

    public static void recoveryWifi(EnableWifiWorker worker, Handler mHandler) {
        if (Build.VERSION.SDK_INT >= 23) {
            startWorkThread(worker);
            Log.d(TAG, "recovery wifi by thread");
        } else {
            mHandler.post(worker);
            Log.d(TAG, "recovery wifi by main thread");
        }
    }

    public static void startWorkThread(Runnable run) {
        Thread thread = new Thread(run);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }
}
