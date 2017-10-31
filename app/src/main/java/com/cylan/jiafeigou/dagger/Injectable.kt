package com.cylan.jiafeigou.dagger

/**
 * Created by yanzhendong on 2017/10/27.
 */
interface Injectable {

    fun useDaggerInject(): Boolean

    fun useButterKnifeInject(): Boolean
}