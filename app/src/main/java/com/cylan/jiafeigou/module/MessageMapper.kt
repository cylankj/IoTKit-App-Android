package com.cylan.jiafeigou.module

import com.example.yzd.helloworld.PropertySupervisor
import com.example.yzd.helloworld.PropertyTypes
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import org.msgpack.annotation.Ignore
import org.msgpack.annotation.Index
import org.msgpack.annotation.Message
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.jvmErasure

/**
 * Created by yzd on 17-12-2.
 */
@Suppress("ArrayInDataClass")
@Entity
data class PropertyBox(
        @Id(assignable = true) var hash: Long = 0,
        @io.objectbox.annotation.Index var uuid: String = "",
        @io.objectbox.annotation.Index var msgId: Int = 0,
        @io.objectbox.annotation.Index var version: Long = 0,
        var bytes: ByteArray = byteArrayOf()
)

@Entity
data class DeviceBox(
        @Id(assignable = true) var hash: Long = 0,
        @io.objectbox.annotation.Index var uuid: String = "",
        var sn: String = "",
        var alias: String = "",
        @io.objectbox.annotation.Index var shareAccount: String,
        @io.objectbox.annotation.Index var pid: Int = -1,
        var vid: String = "",
        var regionType: Int = 0
)

@Entity
data class AccountBox(
        @Id(assignable = true) var hash: Long = 0,
        var token: String,
        var alias: String,
        var enablePush: Int,
        var enableSound: Int,
        @io.objectbox.annotation.Index var email: String,
        var enableVibrate: Int,
        @io.objectbox.annotation.Index var photo: Int,
        var photoUrl: String,
        @io.objectbox.annotation.Index var account: String,
        var wxPush: Int,
        var wxopenid: String
)


@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class MsgId(val msgId: Int)


data class Device(val box: DeviceBox) {

    private operator fun <T> getValue(device: Device, property: KProperty<*>): T {
        val msgId = (property.annotations.first { it is MsgId } as MsgId).msgId
        val value = PropertySupervisor.getValue<DP>(box.uuid, msgId)
        return if (property.returnType.jvmErasure == value::class) {
            value as T
        } else {
            throw ClassCastException("getProperty from Device(uuid=${box.uuid}) for property id:$msgId," +
                    "property name:${property.name},property type:${property.returnType} failed." +
                    "value:$value cannot cast to ${property.returnType}")
        }
    }
}

data class Account(val box: AccountBox) {
}

@Message
open class DP(@Ignore val msgId: Int = 0,
              @Ignore var version: Long = 0)


@Message
class DPPrimary<out T>(val value: T, msgId: Int = 0, version: Long = 0) : DP(msgId, version)

@Message
class DPList : ArrayList<DPPrimary<ByteArray>>()

@Message
data class DPNet(
        @field:Index(0) var net: Int = 0,
        @field:Index(1) var ssid: String = ""
) : DP(PropertyTypes.NET_201)

@Message
data class DPStandby(
        @field:Index(0) var standby: Boolean = false,
        @field:Index(1) var alarmEnable: Boolean = false,
        @field:Index(2) var led: Boolean = false,
        @field:Index(3) var autoRecord: Int = 0
) : DP(0)