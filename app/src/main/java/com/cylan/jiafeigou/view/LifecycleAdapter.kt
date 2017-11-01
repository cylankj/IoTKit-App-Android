package com.cylan.jiafeigou.view

import com.trello.rxlifecycle.LifecycleProvider
import com.trello.rxlifecycle.android.FragmentEvent

/**
 * Created by yanzhendong on 2017/10/28.
 */
interface LifecycleAdapter {
    fun attachToLifecycle(provider: LifecycleProvider<FragmentEvent>)

    fun start()

    fun pause()

    fun stop()

    fun detachToLifecycle()

}