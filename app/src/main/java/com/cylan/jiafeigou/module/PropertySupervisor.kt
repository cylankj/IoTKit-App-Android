@file:Suppress("unused")

package com.cylan.jiafeigou.module

import android.support.v4.util.LongSparseArray
import android.util.Log
import org.msgpack.MessagePack
import org.msgpack.packer.Packer
import org.msgpack.template.AbstractTemplate
import org.msgpack.unpacker.Unpacker
import java.util.*

/**
 * Created by yzd on 17-12-2.
 */
object PropertySupervisor : Supervisor {
    private val TAG = PropertySupervisor::class.java.simpleName
    private val properties = LongSparseArray<DP>()
    private val msgPack = MessagePack()

    fun addHooker(hooker: PropertyHooker) {
        Log.d(TAG, "add hooker:$hooker")
        HookerSupervisor.addHooker(this, PropertyParameter::class.java, hooker)
    }

    fun removeHooker(hooker: PropertyHooker) {
        Log.d(TAG, "remove hooker:$hooker")
        HookerSupervisor.removeHooker(this, PropertyParameter::class.java, hooker)
    }

    init {
        addHooker(NullPropertyHooker())
        msgPack.register(DPPrimary::class.java, DPPrimaryTemplate())
        msgPack.register(DPList::class.java, DPListTemplate())
    }


    private class NullPropertyHooker : PropertyHooker {
        private val TAG = NullPropertyHooker::class.java.simpleName
        override fun hook(action: Supervisor.Action<PropertyParameter>, parameter: PropertyParameter): PropertyParameter? {
            if (parameter.value == null) {
                Log.d(TAG, "value is null, uuid is :${parameter.uuid},msgId is:${parameter.msgId},version is:${parameter.version},bytes is:${parameter.bytes}")
            }
            return action.process(parameter)
        }
    }


    @JvmStatic
    fun <T : DP> getValue(uuid: String, msgId: Int): T? {
        val key = "$uuid:$msgId".toLong()
        var retValue = properties[key]
        var version: Long = retValue?.version ?: 0
        if (retValue == null) {
            Log.d(TAG, "PropertySupervisor.getValue:memory cache for property is miss for key:$key," +
                    "uuid:$uuid,msgId:$msgId,trying to get from disk.")
            val property = DBSupervisor.getProperty(key)
            version = property?.version ?: 0
            val type = PropertyTypes.getType(msgId)
            if (property != null && type != null) {
                try {
                    val readValue = msgPack.read(property.bytes, type)
                    retValue = readValue as?DP ?: DPPrimary(readValue, property.msgId, property.version)
                } catch (e: Exception) {
                    Log.d(TAG, "read msgpack value error:${e.message},uuid:$uuid,msgId:$msgId,property:$property,type:$type")
                }
            }
        }
        return HookerSupervisor.performHooker(this, PropertyAction(), PropertyParameter(key, uuid, msgId, version, retValue, null)) as T
    }

    @JvmStatic
    fun setValue(uuid: String, msgId: Int, version: Long, value: ByteArray) {
        val key = 99L
        val type = PropertyTypes.getType(msgId)
        val property: DP? = try {
            val readValue = msgPack.read(value, type)
            readValue as? DP ?: DPPrimary(readValue, msgId, version)
        } catch (e: Exception) {
            Log.d(TAG, "read msgpack value error:${e.message},uuid:$uuid,msgId:$msgId,version:$version,value:${Arrays.toString(value)}")
            null
        }
        HookerSupervisor.performHooker(this, PropertyAction(false), PropertyParameter(key, uuid, msgId, version, property, value))
    }

    interface PropertyHooker : Supervisor.Hooker<PropertyParameter>

    data class PropertyParameter(var key: Long, var uuid: String, var msgId: Int, var version: Long, var value: DP?, var bytes: ByteArray?) : Supervisor.Parameter

    class PropertyAction(var get: Boolean = true) : Supervisor.Action<PropertyParameter> {
        override fun process(parameter: PropertyParameter): PropertyParameter? {
            properties.put(parameter.key, parameter.value)
            DBSupervisor.putProperty(parameter.key, parameter.uuid, parameter.msgId, parameter.version, parameter.bytes)
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