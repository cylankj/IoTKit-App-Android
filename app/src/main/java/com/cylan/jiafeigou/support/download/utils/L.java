package com.cylan.jiafeigou.support.download.utils;

import android.util.Log;

/**
 * Created by hunt on 16-4-22.
 */
public class L {
    public static final String TAG = "MainActivity";
    public static boolean enable = true;

    public static void e(String msg) {
        if (enable)
            Log.e(TAG, msg);
    }

    public static void i(String msg) {
        if (enable)
            Log.i(TAG, msg);
    }

    public static void d(String msg) {
        if (enable)
            Log.d(TAG, msg);
    }

    public static void v(String msg) {
        if (enable) Log.println(Log.VERBOSE, TAG, msg);
    }

    public static void w(String msg) {
        if (enable) Log.println(Log.WARN, TAG, msg);
    }
}
