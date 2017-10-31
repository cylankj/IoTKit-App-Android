package com.cylan.jiafeigou.module

import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.trello.rxlifecycle.LifecycleProvider
import com.trello.rxlifecycle.android.FragmentEvent

/**
 * Created by yanzhendong on 2017/10/31.
 */
class LifecycleAdapterDecorator<T>(var presenter: T, var provider: LifecycleProvider<FragmentEvent>) {

    fun attachToLifecycle() {
        (presenter as?BasePresenter<*>)?.apply { attachToLifecycle(provider) }
    }

    fun start() {
        (presenter as? BasePresenter<*>)?.apply { start() }
    }

    fun pause() {
        (presenter as? BasePresenter<*>)?.apply { pause() }
    }

    fun stop() {
        (presenter as? BasePresenter<*>)?.apply { stop() }
    }

    fun detachToLifecycle() {
        (presenter as? BasePresenter<*>)?.apply { detachToLifecycle() }
    }
}