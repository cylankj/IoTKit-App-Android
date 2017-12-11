@file:Suppress("ArrayInDataClass")

package com.cylan.jiafeigou.module

import com.example.yzd.helloworld.PropertySupervisor
import com.example.yzd.helloworld.PropertyTypes
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import org.msgpack.annotation.Ignore
import org.msgpack.annotation.Index
import org.msgpack.annotation.Message
import org.msgpack.annotation.Optional
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
        @field:Index(0) @JvmField var net: Int = 0,
        @field:Index(1) @JvmField var ssid: String = ""
) : DP(PropertyTypes.NET_201)

@Message
data class DPStandby(
        @field:Index(0) @JvmField var standby: Boolean = false,
        @field:Index(1) @JvmField var alarmEnable: Boolean = false,
        @field:Index(2) @JvmField var led: Boolean = false,
        @field:Index(3) @JvmField var autoRecord: Int = 0
) : DP(PropertyTypes.CAMERA_STANDBY_508)

@Message
data class DPTimeZone(
        @field:Index(0) @JvmField var timezone: String = "",
        @field:Index(1) @JvmField var offset: Int = 0
) : DP(PropertyTypes.TIME_ZONE_214)

@Message
data class DPBindLog(
        @field:Index(0) @JvmField var isBind: Boolean = false,
        @field:Index(1) @JvmField var account: String = "",
        @field:Index(2) @JvmField var oldAccount: String = ""
) : DP(0)

@Message
data class DPSdcardSummary(
        @field:Index(0) @JvmField var hasSdcard: Boolean = false,
        @field:Index(1) @JvmField var errorCode: Int = 0
) : DP(PropertyTypes.SDCARD_SUMMARY_222)

@Message
data class DPSdStatus(
        @field:Index(0) @JvmField var total: Long = 0,
        @field:Index(1) @JvmField var used: Long = 0,
        @field:Index(2) @JvmField var err: Int = -1,
        @field:Index(3) @JvmField var hasSdcard: Boolean = false
) : DP(PropertyTypes.SDCARD_STORAGE_204)

@Message
data class DPAlarmInfo(
        @field:Index(0) @JvmField var timeStart: Int = 0,
        @field:Index(1) @JvmField var timeEnd: Int = 0,
        @field:Index(2) @JvmField var day: Int = 0//每周的星期*， 从低位到高位代表周一到周日。如0b00000001代表周一，0b01000000代表周日
) : DP(PropertyTypes.CAMERA_ALARM_INFO_502)

@Message
data class DPAlarm(
        @field:Index(0) @JvmField var time: Int = 0,
        @field:Index(1) @JvmField var isRecording: Int = 0,
        @field:Index(2) @JvmField var fileIndex: Int = 0,
        @field:Index(3) @JvmField var ossType: Int = 0,
        @field:Index(4) @JvmField var tly: String = "",
        @field:Index(5) @JvmField @field:Optional var objects: IntArray = intArrayOf(),
        @field:Index(6) @JvmField @field:Optional var humanNum: Int = -1,
        @field:Index(7) @JvmField @field:Optional var face_ids: Array<String> = arrayOf()
) : DP(PropertyTypes.CAMERA_ALARM_MSG_505)