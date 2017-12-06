package com.cylan.jiafeigou.module

/**
 * Created by yanzhendong on 2017/12/5.
 */
interface Supervisor {
    interface Hooker {
        fun parameterType(): Array<Class<*>>
        fun hooker(action: Action, parameter: Any):Any?
    }


    interface Action {
        fun parameter(): Any
        fun process(): Any?
    }
}