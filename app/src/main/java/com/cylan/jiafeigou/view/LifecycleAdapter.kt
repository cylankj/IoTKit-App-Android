package com.cylan.jiafeigou.view

/**
 * Created by yanzhendong on 2017/10/28.
 */
interface LifecycleAdapter {
//    fun attachToLifecycle(provider: LifecycleProvider<FragmentEvent>)

    fun start()

    fun pause()

    fun stop()

    fun destroy()

}