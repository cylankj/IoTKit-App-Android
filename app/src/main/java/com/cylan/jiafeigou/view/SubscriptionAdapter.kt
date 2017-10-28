package com.cylan.jiafeigou.view

import com.cylan.jiafeigou.cache.db.module.Device
import rx.Subscription

/**
 * Created by yanzhendong on 2017/10/28.
 */
@Deprecated("过渡性使用,不推荐在新的地方使用了")
interface SubscriptionAdapter {
    fun addSubscription(tag: String, s: Subscription)
    fun unSubscribe(tag: String): Boolean
    fun getDevice(): Device
}