package com.cylan.jiafeigou.module

import android.util.Log
import com.cylan.jiafeigou.misc.bind.UdpConstant
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.udpMsgPack.JfgUdpMsg
import org.msgpack.annotation.Index
import org.msgpack.annotation.Message
import rx.Observable
import rx.subjects.PublishSubject

@Suppress("ArrayInDataClass", "unused")
/**
 * Created by yanzhendong on 2017/12/4.
 */
object UDPMessageSupervisor {
    private val TAG = UDPMessageSupervisor::class.java.simpleName
    private val publish = PublishSubject.create<Any>().toSerialized()

    init {
        monitorLocalMessage()
    }

    @Message
    data class FPingAckEvent(
            @field:Index(3) @JvmField var version: String = "",
            @field:Index(4) @JvmField var os: Int = 0
    ) : JfgUdpMsg.UdpSecondaryHeard()

    @Message
    data class PingAckEvent(
            @field:Index(2) @JvmField var net: Int = 0,
            @field:Index(3) @JvmField var pid: Int = 0
    ) : JfgUdpMsg.UdpRecvHeard()

    @Message
    data class DoorBellRingEvent(
            @field:Index(1) @JvmField var caller: String = "",
            @field:Index(2) @JvmField var mac: String = ""
    ) : JfgUdpMsg.UdpHeader()

    @Message
    data class ReportMsgEvent(
            @field:Index(2) @JvmField var bytes: ByteArray
    ) : JfgUdpMsg.UdpRecvHeard()

    private fun resolveLocalMessage(message: AppCallbackSupervisor.LocalMessageEvent) {
        Log.d(TAG, "resolveLocalMessage:")
        try {
            val udpHeader = PropertySupervisor.unpackValue(message.bytes, JfgUdpMsg.UdpHeader::class.java)
            val udpEvent = when (udpHeader?.cmd) {
                UdpConstant.F_PING_ACK -> PropertySupervisor.unpackValue(message.bytes, FPingAckEvent::class.java)
                UdpConstant.PING_ACK -> PropertySupervisor.unpackValue(message.bytes, PingAckEvent::class.java)
                UdpConstant.DOORBELL_RING -> PropertySupervisor.unpackValue(message.bytes, DoorBellRingEvent::class.java)
                UdpConstant.REPORT_MSG -> PropertySupervisor.unpackValue(message.bytes, ReportMsgEvent::class.java)
                else -> null
            }
            publish.onNext(udpEvent ?: message)
        } catch (e: Exception) {
            e.printStackTrace()
            AppLogger.e(e)
        }
    }

    private fun monitorLocalMessage() {
        AppCallbackSupervisor.observeLocalMessage().subscribe(UDPMessageSupervisor::resolveLocalMessage) {
            it.printStackTrace()
            AppLogger.e(it)
            monitorLocalMessage()
        }
    }

    @JvmStatic
    fun observeFpingAck(): Observable<FPingAckEvent> {
        return publish.ofType(FPingAckEvent::class.java)
    }

    @JvmStatic
    fun observePingAck(): Observable<PingAckEvent> {
        return publish.ofType(PingAckEvent::class.java)
    }

    @JvmStatic
    fun observeDoorBellRing(): Observable<DoorBellRingEvent> {
        return publish.ofType(DoorBellRingEvent::class.java)
    }

    @JvmStatic
    fun observeReportMsg(): Observable<ReportMsgEvent> {
        return publish.ofType(ReportMsgEvent::class.java)
    }

}