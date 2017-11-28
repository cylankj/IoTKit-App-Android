package com.cylan.jiafeigou.module

import android.util.Log
import rx.Subscription
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.reflect.KFunction

/**
 * Created by yanzhendong on 2017/10/30.
 * 提供对 subscription 的统一管理,如果 subscription
 * 放在 Presenter 中进行管理,则 Presenter 功能太杂,
 * 且无法监控统计 Subscription 的订阅取消情况
 */

object SubscriptionManager : ISubscriptionManager {
    private val subscriptions: ConcurrentMap<String, Subscription> = ConcurrentHashMap()
    var keyProducer: KeyProducer = object : KeyProducer {
        override fun generate(any: Any): String {
            Log.d(KeyProducer::class.java.name, "not support method,parameter is:$any")
            throw UnsupportedOperationException("not support method,parameter is:$any")
        }

        override fun generate(category: String?, level: String?): String {
            return "$category:$level"
        }
    }

    override fun subscribe(category: String?, level: String?, subscription: Subscription) {
        val key = keyProducer.generate(category, level)
        synchronized(SubscriptionManager) {
            val value = subscriptions[key]
            if (value != null && !value.isUnsubscribed) {
                value.unsubscribe()
            }
            Log.d(SubscriptionManager::class.java.name, "subscribe:key:$key")
            subscriptions[key] = subscription
        }
    }

    override fun unsubscribe(category: String?, level: String?) {
        val key = keyProducer.generate(category, level)
        val subscription = subscriptions.remove(key)
        if (subscription != null && !subscription.isUnsubscribed) {
            subscription.unsubscribe()
            Log.d(SubscriptionManager::class.java.name, "unsubscribe:key:$key")
        }
    }
}

interface KeyProducer {
    fun generate(category: String?, level: String?): String
    fun generate(any: Any): String
}

object CategoryProducer : KeyProducer {
    override fun generate(category: String?, level: String?): String {
        return "$category:$level"
    }

    override fun generate(any: Any): String {
        return when (any) {
            is KFunction<*> -> {
                any.toString()
            }
            else -> {
                ""
            }
        }
    }
}

fun Subscription.stop(category: String?) {
    SubscriptionManager.subscribe(category, "stop", this)
}

fun Subscription.destroy(category: String?) {
    SubscriptionManager.subscribe(category, "destroy", this)
}


