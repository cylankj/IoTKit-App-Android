@file:Suppress("ArrayInDataClass")

package com.cylan.jiafeigou.module

import android.os.Parcel
import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import org.msgpack.annotation.Ignore
import org.msgpack.annotation.Index
import org.msgpack.annotation.Message
import org.msgpack.annotation.Optional
import kotlin.reflect.KProperty

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
        @io.objectbox.annotation.Index var phone: String,
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
    private operator fun <T> getValue(device: Device, property: KProperty<*>) = DeviceSupervisor.getValue<T>(device, property)
}

data class Account(val box: AccountBox)

@Message
open class DP(@Ignore val msgId: Int = 0,
              @Ignore var version: Long = 0)


@Message
open class DPPrimary<out T>(val value: T, msgId: Int = 0, version: Long = 0) : DP(msgId, version)

@Message
class DPMessage(bytes: ByteArray, msgId: Int = 0, version: Long = 0) : DPPrimary<ByteArray>(bytes), Parcelable {
    constructor(source: Parcel) : this(
            source.createByteArray()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeByteArray(value)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DPMessage> = object : Parcelable.Creator<DPMessage> {
            override fun createFromParcel(source: Parcel): DPMessage = DPMessage(source)
            override fun newArray(size: Int): Array<DPMessage?> = arrayOfNulls(size)
        }
    }
}

@Message
class DPList : ArrayList<DPMessage>()

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

@Message
data class DPNotificationInfo(
        @field:Index(0) @JvmField var notification: Int = 0,
        @field:Index(1) @JvmField var duration: Int = 0
) : DP(PropertyTypes.CAMERA_ALARM_NOTIFICATION_504)

@Message
data class DPTimeLapse(
        @field:Index(0) @JvmField var timeStart: Int = 0,
        @field:Index(1) @JvmField var timePeriod: Int = 0,
        @field:Index(2) @JvmField var timeDuration: Int = 0,
        @field:Index(3) @JvmField var status: Int = 0
) : DP(PropertyTypes.CAMERA_TIME_LAPSE_PHOTOGRAPHY_506)

@Message
data class DPCamCoord(
        @field:Index(0) @JvmField var x: Int = 0,
        @field:Index(1) @JvmField var y: Int = 0,
        @field:Index(2) @JvmField var r: Int = 0
) : DP()

@Message
data class DPBellCallRecord(
        @field:Index(0) @JvmField var isOK: Int = 0,
        @field:Index(1) @JvmField var time: Int = 0,
        @field:Index(2) @JvmField var duration: Int = 0,
        @field:Index(3) @JvmField var type: Int = 0,
        @field:Index(4) @JvmField @field:Optional var isRecording: Int = -1,//默认为- 1吧,//0为假:1为真
        @field:Index(5) @JvmField @field:Optional var fileIndex: Int = -1//默认为-1 ,因为听说新版本默认为零
) : DP(PropertyTypes.BELL_CALL_STATE_401)

@Message
data class DPWonderItem(
        @field:Index(0) @JvmField var cid: String = "",
        @field:Index(1) @JvmField var time: Int = 0,
        @field:Index(2) @JvmField var msgType: Int = 0,
        @field:Index(3) @JvmField var regionType: Int = 0,
        @field:Index(4) @JvmField var fileName: String = "",
        @field:Index(5) @JvmField var place: String = ""
) : DP(PropertyTypes.ACCOUNT_WONDERFUL_MSG_602)

@Message
data class DPMineMessage(
        @field:Index(0) @JvmField var cid: String = "",
        @field:Index(1) @JvmField var isDone: Boolean = false,
        @field:Index(2) @JvmField @field:Optional var account: String = "",
        @field:Index(3) @JvmField @field:Optional var sn: String = "",
        @field:Index(4) @JvmField @field:Optional var pid: Int = 0
) : DP(PropertyTypes.ACCOUNT_STATE_601)

@Message
data class DPSystemMessage(
        @field:Index(0) @JvmField var title: String = "",
        @field:Index(1) @JvmField var content: String = ""
) : DP(PropertyTypes.SYS_PUSH_MESSAGE_701)

@Message
data class DPUnreadCount(
        @field:Index(0) @JvmField var id: Int = 0,
        @field:Index(1) @JvmField var time: Long = 0,
        @field:Index(2) @JvmField var count: Int = 0
) : DP()

@Message
data class DPShareItem(
        @field:Index(0) @JvmField var cid: String = "",
        @field:Index(1) @JvmField var time: Int = 0,
        @field:Index(2) @JvmField var msgType: Int = 0,
        @field:Index(3) @JvmField var regionType: Int = 0,
        @field:Index(4) @JvmField var fileName: String = "",
        @field:Index(5) @JvmField var desc: String = "",
        @field:Index(6) @JvmField var url: String = ""
) : DP(PropertyTypes.ACCOUNT_WONDERV2_MSG_606)

@Message
data class DPCoordinate(
        @field:Index(0) @JvmField var x: Int = 0,
        @field:Index(1) @JvmField var y: Int = 0,
        @field:Index(2) @JvmField var r: Int = 0,
        @field:Index(3) @JvmField var w: Int = 0,
        @field:Index(4) @JvmField var h: Int = 0
) : DP(PropertyTypes.CAMERA_COORDINATE_510)

@Message
data class DPBellDeepSleep(
        @field:Index(0) @JvmField var enable: Boolean = false,
        @field:Index(1) @JvmField var startTime: Int = 0,
        @field:Index(2) @JvmField var endTime: Int = 0
) : DP(PropertyTypes.BELL_DEEP_SLEEP_404)

@Message
data class DPCameraLiveRtmpCtrl(
        @field:Index(0) @JvmField var url: String = "",
        @field:Index(1) @JvmField var enable: Int = 0,
        @field:Index(2) @JvmField var liveType: Int = 0
) : DP(PropertyTypes.CAMERA_LIVE_RTMP_CTRL_516)

@Message
data class DPCameraLiveRtmpStatus(
        @field:Index(0) @JvmField var liveType: Int = 0,//直播类型：1 facebook; 2 youtube; 3 weibo; 4 rtmp
        @field:Index(1) @JvmField var url: String = "",//rtmp推流地址。示例：rtmp://a.rtmp.youtube.com/live2
        @field:Index(2) @JvmField var flag: Int = 0,//状态特征值： 1 准备直播； 2 直播中； 3 直播结束；
        @field:Index(3) @JvmField var timestamp: Int = 0,//开始直播的时间戳，其它情况置位0
        @field:Index(4) @JvmField var error: Int = 0//错误特征值： 0 正确； 1 错误；
) : DP(PropertyTypes.CAMERA_LIVE_RTMP_STATUS_517)

@Message
data class DPAIService(
        @field:Index(0) @JvmField var vid: String = "",
        @field:Index(1) @JvmField var service_key: String = "",
        @field:Index(2) @JvmField var service_key_seceret: String = ""
) : DP(PropertyTypes.NO_ID)

@Message
data class DPOssRegion(
        @field:Index(0) @JvmField var cid: String = "",
        @field:Index(1) @JvmField var regionType: Int = 0
) : DP(PropertyTypes.NO_ID)

@Message
data class DPOssService(
        @field:Index(0) @JvmField var vid: String = "",
        @field:Index(1) @JvmField var service_key: String = "",
        @field:Index(2) @JvmField var service_key_seceret: String = ""
) : DP(PropertyTypes.NO_ID)

@Message
data class DPVisitorList(
        @field:Index(0) @JvmField var total: Int = 0,
        @field:Index(1) @JvmField var visitorList: List<DPVisitor> = listOf()
) : DP(PropertyTypes.NO_ID), Parcelable {
    constructor(source: Parcel) : this(
            source.readInt(),
            source.createTypedArrayList(DPVisitor.CREATOR)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(total)
        writeTypedList(visitorList)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DPVisitorList> = object : Parcelable.Creator<DPVisitorList> {
            override fun createFromParcel(source: Parcel): DPVisitorList = DPVisitorList(source)
            override fun newArray(size: Int): Array<DPVisitorList?> = arrayOfNulls(size)
        }
    }
}

@Message
data class DPVisitorDetail(
        @field:Index(0) @JvmField var faceId: String = "",
        @field:Index(1) @JvmField var imgUrl: String = "",
        @field:Index(2) @JvmField var ossType: Int = 0
) : DP(PropertyTypes.NO_ID), Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(faceId)
        writeString(imgUrl)
        writeInt(ossType)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DPVisitorDetail> = object : Parcelable.Creator<DPVisitorDetail> {
            override fun createFromParcel(source: Parcel): DPVisitorDetail = DPVisitorDetail(source)
            override fun newArray(size: Int): Array<DPVisitorDetail?> = arrayOfNulls(size)
        }
    }
}

@Message
data class DPVisitor(
        @field:Index(0) @JvmField var objectType: Int = 0,
        @field:Index(1) @JvmField var personId: String = "",
        @field:Index(2) @JvmField var personName: String = "",
        @field:Index(3) @JvmField var lastTime: Long = 0,
        @field:Index(4) @JvmField var detailList: List<DPVisitorDetail> = listOf()
) : DP(PropertyTypes.NO_ID), Parcelable {
    constructor(source: Parcel) : this(
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readLong(),
            ArrayList<DPVisitorDetail>().apply { source.readList(this, DPVisitorDetail::class.java.classLoader) }
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(objectType)
        writeString(personId)
        writeString(personName)
        writeLong(lastTime)
        writeList(detailList)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DPVisitor> = object : Parcelable.Creator<DPVisitor> {
            override fun createFromParcel(source: Parcel): DPVisitor = DPVisitor(source)
            override fun newArray(size: Int): Array<DPVisitor?> = arrayOfNulls(size)
        }
    }
}

@Message
data class DPRequestContent(
        @field:Index(0) @JvmField var uuid: String = "",
        @field:Index(1) @JvmField var timeSec: Long = 0
) : DP(PropertyTypes.NO_ID)

@Message
data class DPStrangerVisitor(
        @field:Index(0) @JvmField var faceId: String = "",
        @field:Index(1) @JvmField var image_url: String = "",
        @field:Index(2) @JvmField var ossType: Int = 0,
        @field:Index(3) @JvmField var lastTime: Long = 0
) : DP(PropertyTypes.NO_ID), Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readLong()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(faceId)
        writeString(image_url)
        writeInt(ossType)
        writeLong(lastTime)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DPStrangerVisitor> = object : Parcelable.Creator<DPStrangerVisitor> {
            override fun createFromParcel(source: Parcel): DPStrangerVisitor = DPStrangerVisitor(source)
            override fun newArray(size: Int): Array<DPStrangerVisitor?> = arrayOfNulls(size)
        }
    }
}

@Message
data class DPStrangerVisitorList(
        @field:Index(0) @JvmField var total: Int = 0,
        @field:Index(1) @JvmField var strangerVisitors: List<DPStrangerVisitor> = listOf()
) : DP(PropertyTypes.NO_ID), Parcelable {
    constructor(source: Parcel) : this(
            source.readInt(),
            source.createTypedArrayList(DPStrangerVisitor.CREATOR)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(total)
        writeTypedList(strangerVisitors)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DPStrangerVisitorList> = object : Parcelable.Creator<DPStrangerVisitorList> {
            override fun createFromParcel(source: Parcel): DPStrangerVisitorList = DPStrangerVisitorList(source)
            override fun newArray(size: Int): Array<DPStrangerVisitorList?> = arrayOfNulls(size)
        }
    }
}

@Message
data class DPFetchMsgListRsp(//rsp=msgpack(cid, type, id, timeMsec, [505?, 505?, ...])
        @field:Index(0) @JvmField var cid: String = "",
        @field:Index(1) @JvmField var msgType: Int = 0,
        @field:Index(2) @JvmField var faceId: String = "",
        @field:Index(3) @JvmField var timeMS: Int = 0,
        @field:Index(4) @JvmField var dataList: DPList = DPList()
) : DP(PropertyTypes.NO_ID), Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readInt(),
            source.readString(),
            source.readInt(),
            source.readDPList()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int): kotlin.Unit = with(dest) {
        writeString(cid)
        writeInt(msgType)
        writeString(faceId)
        writeInt(timeMS)
        writeDPList(dataList)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DPFetchMsgListRsp> = object : Parcelable.Creator<DPFetchMsgListRsp> {
            override fun createFromParcel(source: Parcel): DPFetchMsgListRsp = DPFetchMsgListRsp(source)
            override fun newArray(size: Int): Array<DPFetchMsgListRsp?> = arrayOfNulls(size)
        }
    }
}

private fun Parcel.readDPList(): DPList {
    val dpList = DPList()
    readTypedList(dpList, DPMessage.CREATOR)
    return dpList
}

private fun Parcel.writeDPList(dataList: DPList) = writeTypedList(dataList)


@Message
data class FetchMsgListReq(  //req=msgpack(cid, type, id, timeMsec)
        @field:Index(0) @JvmField var cid: String = "",
        @field:Index(1) @JvmField var msgId: Int = 0,
        @field:Index(2) @JvmField var faceId: String = "",
        @field:Index(3) @JvmField var seq: Long = 0
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readInt(),
            source.readString(),
            source.readLong()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(cid)
        writeInt(msgId)
        writeString(faceId)
        writeLong(seq)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<FetchMsgListReq> = object : Parcelable.Creator<FetchMsgListReq> {
            override fun createFromParcel(source: Parcel): FetchMsgListReq = FetchMsgListReq(source)
            override fun newArray(size: Int): Array<FetchMsgListReq?> = arrayOfNulls(size)
        }
    }
}

@Message
data class VisitsTimesRsp(
        @field:Index(0) @JvmField var cid: String = "",
        @field:Index(1) @JvmField var msgType: Int = 0,
        @field:Index(2) @JvmField var faceId: String = "",
        @field:Index(3) @JvmField var count: Int = 0
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readInt(),
            source.readString(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(cid)
        writeInt(msgType)
        writeString(faceId)
        writeInt(count)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VisitsTimesRsp> = object : Parcelable.Creator<VisitsTimesRsp> {
            override fun createFromParcel(source: Parcel): VisitsTimesRsp = VisitsTimesRsp(source)
            override fun newArray(size: Int): Array<VisitsTimesRsp?> = arrayOfNulls(size)
        }
    }
}

@Message
data class VisitsTimesReq(
        @field:Index(0) @JvmField var cid: String = "",
        @field:Index(1) @JvmField var msgType: Int = 0,
        @field:Index(2) @JvmField var faceId: String = ""
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readInt(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(cid)
        writeInt(msgType)
        writeString(faceId)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VisitsTimesReq> = object : Parcelable.Creator<VisitsTimesReq> {
            override fun createFromParcel(source: Parcel): VisitsTimesReq = VisitsTimesReq(source)
            override fun newArray(size: Int): Array<VisitsTimesReq?> = arrayOfNulls(size)
        }
    }
}

@Message
data class DelVisitorReq(
        @field:Index(0) @JvmField var cid: String = "",
        @field:Index(1) @JvmField var type: Int = 0,
        @field:Index(2) @JvmField var id: String = ""
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readInt(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(cid)
        writeInt(type)
        writeString(id)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DelVisitorReq> = object : Parcelable.Creator<DelVisitorReq> {
            override fun createFromParcel(source: Parcel): DelVisitorReq = DelVisitorReq(source)
            override fun newArray(size: Int): Array<DelVisitorReq?> = arrayOfNulls(size)
        }
    }
}

@Message
data class Unit(
        @field:Index(0) @JvmField var video: Short = 0,
        @field:Index(1) @JvmField var mode: Short = 0
)

@Message
data class UniversalDataBaseRsp(
        @field:Index(0) @JvmField var id: Int = 0,
        @field:Index(1) @JvmField var caller: String = "",
        @field:Index(2) @JvmField var callee: String = "",
        @field:Index(3) @JvmField var seq: Long = 0,
        @field:Index(4) @JvmField var way: Int = 0,
        @field:Index(5) @JvmField var dataMap: Map<Int, List<Unit>> = mapOf()
)

@Message
data class AcquaintanceItem(
        @field:Index(0) @JvmField var faceId: String = "",
        @field:Index(1) @JvmField var imageUrl: String = "",
        @field:Index(2) @JvmField var ossType: Int = 0
)

@Message
data class AcquaintanceListRsp(
        @field:Index(0) @JvmField var cid: String = "",
        @field:Index(1) @JvmField var personId: String = "",
        @field:Index(2) @JvmField var acquaintanceItems: List<AcquaintanceItem> = listOf()
)

@Message
data class AcquaintanceListReq(
        @field:Index(0) var cid: String,
        @field:Index(1) var person_id: String
)

@Message
data class GetRobotServerReq(
        @field:Index(0) @JvmField var cid: String = "",
        @field:Index(1) @JvmField var vid: String = ""
)

@Message
data class GetRobotServerRsp(
        @field:Index(0) @JvmField var host: String = "",
        @field:Index(1) @JvmField var port: Int = 0
)

@Message
data class Rect4F(
        @field:Index(0) @JvmField var left: Float = 0F,
        @field:Index(1) @JvmField var top: Float = 0F,
        @field:Index(2) @JvmField var right: Float = 0F,
        @field:Index(3) @JvmField var bottom: Float = 0F
)

@Message
data class DPCameraWarnArea(
        @field:Index(0) @JvmField var enable: Boolean = false,
        @field:Index(1) @JvmField var rects: List<Rect4F> = listOf()
) : DP(PropertyTypes.CAMERA_WARNAREA_519)


@Message
data class DPSetFaceIdStatus(
        @field:Index(0) @JvmField var faceId: String = "",
        @field:Index(1) @JvmField var enable: Boolean = false
) : DP()

@Message
data class DPChangeLockPassword(
        @field:Index(0) @JvmField var oldPassWord: String = "",
        @field:Index(1) @JvmField var newPassWord: String = ""

) : DP()

@Message
data class DPChangeLockStatusReq(
        @field:Index(0) @JvmField var password: String = "",
        @field:Index(1) @JvmField var status: Int = 0
) : DP()


@Message
class DPCameraTakePictureRsp : DP() {
    @Index(0)
    var ret: Int = 0
    @Index(1)
    var ossType: Int = 0
    @Index(2)
    var time: Long = 0
}