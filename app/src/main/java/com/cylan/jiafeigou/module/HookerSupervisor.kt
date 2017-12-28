@file:Suppress("UNCHECKED_CAST")

package com.cylan.jiafeigou.module

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by yanzhendong on 2017/12/5.
 */
object HookerSupervisor : Supervisor {
    private val hookers = ConcurrentHashMap<Class<*>, MutableList<Supervisor.Hooker>>()

    @JvmStatic
    fun addHooker(hooker: Supervisor.Hooker) {
        val parameterType = hooker.parameterType()
        parameterType.forEach {
            var hookers = hookers[it]
            if (hookers == null) {
                synchronized(HookerSupervisor::class) {
                    if (hookers == null) {
                        hookers = mutableListOf()
                        this.hookers[it] = hookers!!
                    }
                }
            }
            hookers!!.add(hooker)
        }
    }

    @JvmStatic
    fun removeHooker(hooker: Supervisor.Hooker) {
        hooker.parameterType().forEach { this.hookers[it]?.remove(hooker) }
    }

    @JvmStatic
    fun performHooker(action: Supervisor.Action): Any? {
        return HookerAction(HookerActionParameter(action)).process()
    }

    open class ActionHooker : Supervisor.Hooker {
        override fun parameterType(): Array<Class<*>> = arrayOf(HookerActionParameter::class.java)
        override fun hooker(action: Supervisor.Action): Any? {
            val parameter = action.parameter()
            return when (parameter) {
                is HookerActionParameter -> doHookerActionHooker(action, parameter)
                else -> action.process()
            }
        }

        open protected fun doHookerActionHooker(action: Supervisor.Action, parameter: HookerActionParameter): Any? = action.process()
    }

    private class HookerAction(val hookerParameter: HookerActionParameter) : Supervisor.Action {
        private var parameter: Any = hookerParameter
        private var index = 0
        override fun parameter(): Any = parameter
        override fun process(): Any? {
            var hooker = hookers[parameter::class.java]?.getOrNull(index++)
            if (hooker == null && parameter is HookerActionParameter) {
                Log.d("HookerAction", "No Hooker Action")
                index = 0
                parameter = (parameter as HookerActionParameter).action.parameter()
                hooker = hookers[parameter::class.java]?.getOrNull(index++)
            }
            return if (hooker != null) {
                Log.d("HookerAction", "doHooker:" + hooker + "parameter:" + parameter)
                return hooker.hooker(this)
            } else {
                hookerParameter.action.process()
            }
        }

        override fun toString(): String {
            return "HookerAction(hookerParameter=$hookerParameter)"
        }

    }

    data class HookerActionParameter(val action: Supervisor.Action)
}