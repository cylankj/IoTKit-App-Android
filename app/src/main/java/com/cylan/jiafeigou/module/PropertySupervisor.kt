@file:Suppress("unused")

package com.cylan.jiafeigou.module

import android.support.v4.util.LongSparseArray
import android.util.Log
import org.msgpack.MessagePack
import org.msgpack.packer.Packer
import org.msgpack.template.AbstractTemplate
import org.msgpack.unpacker.Unpacker
import java.util.*

@Suppress("ArrayInDataClass")
/**
 * Created by yzd on 17-12-2.
 */
object PropertySupervisor : Supervisor {
    private val TAG = PropertySupervisor::class.java.simpleName
    private val properties = LongSparseArray<DP>()
    private val msgPack = MessagePack()

    private var hashGenerator: HashGenerator = DefaultHashGenerator()

    init {
        HookerSupervisor.addHooker(NullPropertyHooker())
        msgPack.register(DPPrimary::class.java, DPPrimaryTemplate())
        msgPack.register(DPList::class.java, DPListTemplate())
    }


    private class NullPropertyHooker : PropertyHooker() {
        private val TAG = NullPropertyHooker::class.java.simpleName

        override fun doGetHooker(action: Supervisor.Action, parameter: GetParameter) {
            if (parameter.retValue == null) {
                Log.d(TAG, "get value is null, uuid is :${parameter.uuid},msgId is:${parameter.msgId}")
            }
            super.doGetHooker(action, parameter)
        }

        override fun doSetHooker(action: Supervisor.Action, parameter: SetParameter) {
            if (parameter.bytes == null) {
                Log.d(TAG, "set value bytes is null,uuid is:${parameter.uuid},msgId is:${parameter.msgId},version is:${parameter.version}")
            }
            super.doSetHooker(action, parameter)
        }
    }


    @JvmStatic
    fun <T : DP> getValue(uuid: String, msgId: Int): T? {
        return (HookerSupervisor.performHooker(PropertyAction(GetParameter(uuid, msgId))) as? GetParameter)?.retValue as? T
    }

    @JvmStatic
    fun setValue(uuid: String, msgId: Int, version: Long, value: ByteArray?) {
        HookerSupervisor.performHooker(PropertyAction(SetParameter(uuid, msgId, version, value)))
    }

    abstract class PropertyHooker : Supervisor.Hooker {
        override fun parameterType(): Array<Class<*>> = arrayOf(GetParameter::class.java, SetParameter::class.java)

        override fun hooker(action: Supervisor.Action, parameter: Any) {
            when (parameter) {
                is GetParameter -> doGetHooker(action, parameter)
                is SetParameter -> doSetHooker(action, parameter)
                else -> action.process()
            }
        }

        open protected fun doGetHooker(action: Supervisor.Action, parameter: GetParameter) = action.process()


        open protected fun doSetHooker(action: Supervisor.Action, parameter: SetParameter) = action.process()

    }

    data class GetParameter(var uuid: String, var msgId: Int, var retValue: DP? = null)

    data class SetParameter(var uuid: String, var msgId: Int, var version: Long, var bytes: ByteArray?)

    private class PropertyAction<out T : Any>(val parameter: T) : Supervisor.Action {
        override fun parameter() = parameter

        override fun process(): T? {
            when (parameter) {
                is GetParameter -> {
                    val hash = hashGenerator.generate(parameter.uuid, parameter.msgId)
                    if (parameter.retValue == null) {
                        parameter.retValue = properties[hash]
                    }
                    if (parameter.retValue == null) {
                        Log.d(TAG, "PropertySupervisor.getValue:memory cache for property is miss for key:$hash," +
                                "uuid:${parameter.uuid},msgId:${parameter.msgId},trying to get from disk.")
                        val property = DBSupervisor.getProperty(hash)
                        val type = PropertyTypes.getType(parameter.msgId)
                        if (property != null && type != null) {
                            try {
                                val readValue = msgPack.read(property.bytes, type)
                                parameter.retValue = readValue as?DP ?: DPPrimary(readValue, property.msgId, property.version)
                            } catch (e: Exception) {
                                Log.d(TAG, "read msgpack value error:${e.message},uuid:${parameter.uuid},msgId:${parameter.msgId},property:$property,type:$type")
                            }
                        }
                    }
                }

                is SetParameter -> {
                    val hash = hashGenerator.generate(parameter.uuid, parameter.msgId, parameter.version)
                    val type = PropertyTypes.getType(parameter.msgId)
                    try {
                        val readValue = msgPack.read(parameter.bytes, type)
                        val property = readValue as? DP ?: DPPrimary(readValue, parameter.msgId, parameter.version)
                        properties.put(hash, property)
                        DBSupervisor.putProperty(PropertyBox(hash, parameter.uuid, parameter.msgId, parameter.version, parameter.bytes))
                    } catch (e: Exception) {
                        Log.d(TAG, "read msgpack value error:${e.message},uuid:${parameter.uuid},msgId:${parameter.msgId},version:${parameter.version},value:${Arrays.toString(parameter.bytes)}")
                    }
                }
            }
            return parameter
        }
    }

    @JvmStatic
    fun packValue(value: Any): ByteArray? = try {
        msgPack.write(value)
    } catch (e: Exception) {
        Log.d(TAG, "packValue error:${e.message},value:$value")
        null
    }

    @JvmStatic
    fun <T> unpackValue(bytes: ByteArray, type: Class<T>): T? = try {
        msgPack.read(bytes, type)
    } catch (e: Exception) {
        Log.d(TAG, "unpackValue error:${e.message},bytes:${Arrays.toString(bytes)},type:$type")
        null
    }

    interface HashGenerator {
        fun generate(uuid: String, msgId: Int, version: Long = 0): Long
    }

    private class DefaultHashGenerator : HashGenerator {
        override fun generate(uuid: String, msgId: Int, version: Long): Long {
            return "$uuid:$msgId".hashCode().toLong()
        }
    }


    private class DPPrimaryTemplate : AbstractTemplate<DPPrimary<*>>() {
        override fun read(unPacker: Unpacker, to: DPPrimary<*>?, required: Boolean): DPPrimary<*> {
            return DPPrimary(unPacker.readByteArray(), 0, 0)
        }

        override fun write(packer: Packer, v: DPPrimary<*>, required: Boolean) {
            packer.write(v.value)
        }
    }

    private class DPListTemplate : AbstractTemplate<DPList>() {
        override fun read(unPacker: Unpacker, to: DPList?, required: Boolean): DPList {
            val count = unPacker.readArrayBegin()
            val dpList = DPList()
            for (x in 1..count) {
                unPacker.readArrayBegin()
                val msgId = unPacker.readInt()
                val version = unPacker.readLong()
                val bytes = unPacker.readByteArray()
                val message = DPMessage(bytes, msgId, version)
                dpList.add(message)
                unPacker.readArrayEnd()
            }
            unPacker.readArrayEnd()
            return dpList
        }

        override fun write(packer: Packer, v: DPList, required: Boolean) {
            packer.writeArrayBegin(v.size)
            for (dpMessage in v) {
                packer.writeArrayBegin(3)
                packer.write(dpMessage.msgId)
                packer.write(dpMessage.version)
                packer.write(dpMessage.value)
                packer.writeArrayEnd()
            }
            packer.writeArrayEnd()
        }

    }
}