package com.cylan.jiafeigou.module

import android.util.Log
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.log.AppLogger
import com.trello.rxlifecycle.LifecycleProvider
import com.trello.rxlifecycle.android.FragmentEvent
import rx.Observable
import rx.Subscription
import rx.subscriptions.SerialSubscription
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/10/30.
 */
class SubscriptionManager @Inject constructor() : ISubscriptionManager {
    private var lifecycleProviderMap = ConcurrentHashMap<String, LifecycleProvider<FragmentEvent>>()
    override fun bind(name: String, lifecycleProvider: LifecycleProvider<FragmentEvent>) {
        AppLogger.w("bind:$name")
        lifecycleProviderMap.put(name, lifecycleProvider)
    }

    override fun unbind(name: String) {
        lifecycleProviderMap.remove(name)
    }

    @Volatile private var subscriptions: ConcurrentHashMap<String, SerialSubscription> = ConcurrentHashMap()

    override fun stop(): Observable<String> {
        val traceElement = Thread.currentThread().stackTrace[3]
        val method = "${traceElement.fileName}(L:${traceElement.lineNumber}):${traceElement.className}.${traceElement.methodName}"
        val lifecycleProvider = lifecycleProviderMap[traceElement.className]
        Log.i(JConstant.CYLAN_TAG, "stop:method:$method")
        return atomicMethod(method).compose(lifecycleProvider?.bindUntilEvent(FragmentEvent.STOP) ?: Observable.Transformer {
            Log.i(JConstant.CYLAN_TAG, "lifecycle 不存在, bind to stop 失败了")
            it
        })
    }

    override fun destroy(): Observable<String> {
        val traceElement = Thread.currentThread().stackTrace[3]
        val method = "${traceElement.fileName}(L:${traceElement.lineNumber}):${traceElement.className}.${traceElement.methodName}"
        val lifecycleProvider = lifecycleProviderMap[traceElement.className]
        Log.i(JConstant.CYLAN_TAG, "destroy:method:$method")
        return atomicMethod(method).compose(lifecycleProvider?.bindUntilEvent(FragmentEvent.DESTROY) ?: Observable.Transformer {
            Log.i(JConstant.CYLAN_TAG, "lifecycle 不存在, bind to destroy 失败了")
            it
        })
    }


    @Synchronized override fun atomicMethod(): Observable<String> {
        val traceElement = Thread.currentThread().stackTrace[2]
        val method = "${traceElement.fileName}(L:${traceElement.lineNumber}):${traceElement.className}.${traceElement.methodName}"
        return atomicMethod(method)
    }

    private fun atomicMethod(method: String): Observable<String> {
        val subscribe = Observable.OnSubscribe<String> { subscriber ->
            var serialSubscription = subscriptions[method]
            if (serialSubscription == null || serialSubscription.isUnsubscribed) {
                serialSubscription = SerialSubscription()
                subscriptions[method] = serialSubscription
            }
            serialSubscription.set(subscriber)
            subscriber.add(object : AbstractSubscription() {
                override fun onUnsubscribe() {
                    if (serialSubscription!!.get().isUnsubscribed) {
                        subscriptions.remove(method)
                        Log.i(JConstant.CYLAN_TAG, "method finished:$method")
                    }
                }
            })
            subscriber.onNext(method)
            subscriber.onCompleted()
        }
        return Observable.create(subscribe)
    }

    internal abstract class AbstractSubscription : Subscription {

        private val unsubscribed = AtomicBoolean()

        override fun isUnsubscribed(): Boolean {
            return unsubscribed.get()
        }

        override fun unsubscribe() {
            if (unsubscribed.compareAndSet(false, true)) {
                onUnsubscribe()
            }
        }

        protected abstract fun onUnsubscribe()
    }
}