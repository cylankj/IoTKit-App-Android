@file:Suppress("UNCHECKED_CAST")

package com.cylan.jiafeigou.module

/**
 * Created by yanzhendong on 2017/12/5.
 */
object HookerSupervisor : Supervisor {
    private val TAG = HookerSupervisor::class.java.simpleName
    private val hookers = mutableMapOf<String, MutableMap<String, MutableList<Supervisor.Hooker<Supervisor.Parameter>>>>()

    @JvmStatic
    fun <T : Supervisor.Parameter> addHooker(target: Any, parameterType: Class<T>, hooker: Supervisor.Hooker<T>) {
        var hookerTarget = hookers[target.javaClass.name]
        if (hookerTarget == null) {
            synchronized(HookerSupervisor::class) {
                if (hookerTarget == null) {
                    val map = mutableMapOf<String, MutableList<Supervisor.Hooker<Supervisor.Parameter>>>()
                    hookers[target.javaClass.name] = map
                    hookerTarget = map
                }
            }
        }
        var hooker1 = hookerTarget!![parameterType.javaClass.name]
        if (hooker1 == null) {
            synchronized(HookerSupervisor::class) {
                if (hooker1 == null) {
                    val list = mutableListOf<Supervisor.Hooker<Supervisor.Parameter>>()
                    hookerTarget!![parameterType.javaClass.name] = list
                    hooker1 = list
                }
            }
        }
        hooker1!!.add(hooker as Supervisor.Hooker<Supervisor.Parameter>)
    }

    @JvmStatic
    fun <T : Supervisor.Parameter> removeHooker(target: Any, parameterType: Class<T>, hooker: Supervisor.Hooker<T>) {
        hookers[target.javaClass.name]?.apply {
            this[parameterType.javaClass.name]?.apply {
                this.remove(hooker as Supervisor.Hooker<Supervisor.Parameter>)
            }
        }
    }

    @JvmStatic
    fun <T : Supervisor.Parameter> performHooker(target: Any, action: Supervisor.Action<T>, parameter: T): T? {
        return HookerAction<T>(target::javaClass.name).hook(action, parameter)
    }

    private class HookerAction<T : Supervisor.Parameter>(
            private val tag: String
    ) : Supervisor.Action<T>, Supervisor.Hooker<T> {
        private var action: Supervisor.Action<T>? = null
        private var parameter: T? = null
        private var index: Int = 0
        override fun hook(action: Supervisor.Action<T>, parameter: T): T? {
            this.action = if (this.action == null) action else this.action
            this.parameter = if (this.action == null) parameter else this.parameter
            val hookerTarget = hookers[tag]
            val hooker = hookerTarget?.get(parameter.javaClass.name)
            val hooker1 = hooker?.getOrNull(index++)
            return if (hooker1 != null) {
                hooker1.hook(this as Supervisor.Action<Supervisor.Parameter>, parameter) as T
            } else {
                action.process(parameter)
            }
        }

        override fun process(parameter: T): T? {
            return hook(this, parameter)
        }
    }
}