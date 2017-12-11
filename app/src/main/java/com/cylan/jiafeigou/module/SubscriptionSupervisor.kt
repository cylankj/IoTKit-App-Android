package com.cylan.jiafeigou.module

import android.text.TextUtils
import android.util.Log
import rx.Subscription

/**
 * Created by yanzhendong on 2017/12/5.
 */
object SubscriptionSupervisor {
    private val TAG = SubscriptionSupervisor::class.java.simpleName
    private val subscriptions = mutableMapOf<String, SubscriptionTarget>()
    const val CATEGORY_STOP = "CATEGORY_STOP"
    const val CATEGORY_DESTROY = "CATEGORY_DESTROY"
    const val CATEGORY_DEFAULT = "CATEGORY_DEFAULT"
    @JvmStatic
    fun subscribe(target: Any, category: String, tag: String, subscription: Subscription) {
        Log.d(TAG, "subscribe for:target:${target.javaClass.name},category:$category,tag:$tag,subscription:$subscription")
        var subscriptionTarget = subscriptions[target.javaClass.name]
        if (subscriptionTarget == null) {
            synchronized(SubscriptionSupervisor::class) {
                if (subscriptionTarget == null) {
                    val s = SubscriptionTarget()
                    subscriptions[target.javaClass.name] = s
                    subscriptionTarget = s
                }
            }
        }
        subscriptionTarget!!.subscribe(category, tag, subscription)
    }

    @JvmStatic
    fun unsubscribe(target: Any, category: String?, tag: String?) {
        Log.d(TAG, "unsubscribe for:target:${target.javaClass.name},category:$category,tag:$tag")
        subscriptions[target.javaClass.name]?.apply {
            synchronized(SubscriptionSupervisor::class) {
                if (TextUtils.isEmpty(category)) {
                    subscriptions.remove(target.javaClass.name)
                }
                unsubscribe(category, tag)
            }
        }
    }

    @JvmStatic
    fun get(target: Any, category: String, tag: String): Subscription? = synchronized(SubscriptionSupervisor::class) {
        subscriptions[target.javaClass.name]?.get(category, tag)
    }

    private class SubscriptionTarget {
        private val subscriptions = mutableMapOf<String, SubscriptionCategory>()

        fun subscribe(category: String, tag: String, subscription: Subscription) {
            var subscriptionCategory = subscriptions[category]
            if (subscriptionCategory == null) {
                synchronized(SubscriptionTarget::class) {
                    if (subscriptionCategory == null) {
                        val s = SubscriptionCategory()
                        subscriptions[category] = s
                        subscriptionCategory = s
                    }
                }
            }
            subscriptionCategory!!.subscribe(tag, subscription)
        }

        fun unsubscribe(category: String?, tag: String?) {
            subscriptions
                    .filter { TextUtils.isEmpty(category) || TextUtils.equals(it.key, category) }
                    .forEach {
                        synchronized(SubscriptionTarget::class) {
                            if (TextUtils.isEmpty(category)) {
                                subscriptions.remove(it.key)
                            }
                            it.value.unsubscribe(tag)
                        }
                    }
        }

        fun get(category: String, tag: String): Subscription? = subscriptions[category]?.get(tag)
    }

    private class SubscriptionCategory {
        private val subscriptions = mutableMapOf<String, Subscription>()
        fun subscribe(tag: String, subscription: Subscription) {
            val subscription1 = subscriptions[tag]
            if (subscription1 != null) {
                synchronized(SubscriptionCategory::class) {
                    if (!subscription1.isUnsubscribed) {
                        subscription1.unsubscribe()
                    }
                }
            }
            subscriptions[tag] = subscription
        }

        fun unsubscribe(tag: String?) {
            subscriptions.filter { TextUtils.isEmpty(tag) || TextUtils.equals(it.key, tag) }
                    .forEach {
                        synchronized(SubscriptionCategory::class) {
                            subscriptions.remove(it.key)
                            val subscription = it.value
                            if (!subscription.isUnsubscribed) {
                                subscription.unsubscribe()
                            }
                        }
                    }
        }

        fun get(tag: String): Subscription? = subscriptions[tag]
    }

}