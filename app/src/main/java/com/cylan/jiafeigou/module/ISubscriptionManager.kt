package com.cylan.jiafeigou.module

import com.trello.rxlifecycle.LifecycleProvider
import com.trello.rxlifecycle.android.FragmentEvent
import rx.Observable

/**
 * Created by yanzhendong on 2017/10/30.
 */
interface ISubscriptionManager {
    /**
     * 在方法没执行完成前只有最后一次调用会生效,之前的会被取消订阅
     * */
    fun atomicMethod(): Observable<String>

    fun bind(lifecycleProvider: LifecycleProvider<FragmentEvent>)

    fun unbind()

    fun stop(): Observable<String>

    fun destroy(): Observable<String>

}