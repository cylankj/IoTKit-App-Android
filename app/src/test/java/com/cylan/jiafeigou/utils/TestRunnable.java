package com.cylan.jiafeigou.utils;

import android.util.Log;

/**
 * Created by hunt on 16-5-6.
 */
public class TestRunnable implements Runnable {
    int index;

    public TestRunnable(int index) {
        this.index = index;
    }

    @Override
    public void run() {
        Log.d("hunt", "hunt; " + index);
//        System.out.println("what: " + index);
    }
}
