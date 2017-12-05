package com.cylan.jiafeigou.module

/**
 * Created by yanzhendong on 2017/12/5.
 */
interface Supervisor {
    interface Hooker<T : Parameter> {
        fun hook(action: Action<T>, parameter: T): T?
    }

    interface Parameter

    interface Action<T : Parameter> {
        fun process(parameter: T): T?
    }

}