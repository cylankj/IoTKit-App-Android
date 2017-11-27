package com.cylan.jiafeigou.module

import rx.Subscription

/**
 * Created by yanzhendong on 2017/10/30.
 */
interface ISubscriptionManager {

    fun subscribe(category: String?, level: String? = "stop", subscription: Subscription)
    fun unsubscribe(category: String?, level: String? = "stop")
}