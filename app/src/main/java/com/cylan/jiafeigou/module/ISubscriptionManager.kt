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

    fun bind(target: Any, lifecycleProvider: LifecycleProvider<FragmentEvent>)

    fun unbind(target: Any)

    fun stop(target: Any): Observable<String>

    fun destroy(target: Any): Observable<String>

}