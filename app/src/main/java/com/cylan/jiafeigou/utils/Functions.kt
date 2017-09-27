package com.cylan.jiafeigou.utils

import com.cylan.jiafeigou.BuildConfig

/**
 * Created by yanzhendong on 2017/9/27.
 */

object Functions {
    inline fun runOnDebug(runnable: () -> Unit?) {
        if (BuildConfig.DEBUG) {
            runnable.invoke()
        }
    }

    fun runOnDebug(runnable: Runnable) {
        if (BuildConfig.DEBUG) {
            runnable.run()
        }
    }

    fun runDebunce(runnable: Runnable, duration: Long) {

    }
}