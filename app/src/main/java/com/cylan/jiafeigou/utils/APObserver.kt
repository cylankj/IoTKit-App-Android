package com.cylan.jiafeigou.utils

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import com.cylan.jiafeigou.BuildConfig
import com.cylan.jiafeigou.dp.DpUtils.unpackData
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.misc.bind.UdpConstant
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.rx.RxBus
import com.cylan.jiafeigou.rx.RxEvent
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.udpMsgPack.JfgUdpMsg
import com.google.gson.Gson
import org.msgpack.MessagePack
import rx.Observable
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by yanzhendong on 2017/11/22.
 */
object APObserver {
    private val observers: MutableMap<String, Any> = mutableMapOf()
    private val scanResult = mutableMapOf<String, ScanResult>()
    private val START_SCAN = 10000
    private val SCAN_PERIOD = 5000//五秒扫描
    fun addObserver(uuid: String) {

    }

    fun removeObserver(uuid: String) {
        observers.remove(uuid)
    }

    fun scan() {

    }



    @JvmStatic
    fun scanDogWiFi(): Observable<MutableList<ScanResult>> {
        return Observable.create<ScanResult> { subscriber ->
            var subscribe = RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg::class.java)
                    .map {
                        if (BuildConfig.DEBUG) {
                            Log.i(JConstant.CYLAN_TAG, "正在解析 UDP 消息:${Gson().toJson(it)},解压后数据为:${MessagePack().read(it.data)}")
                        }
                        return@map try {
                            val secondaryHeard = unpackData<JfgUdpMsg.UdpHeader>(it.data, JfgUdpMsg.UdpHeader::class.java)
                            when {
                                TextUtils.equals(secondaryHeard.cmd, UdpConstant.F_PING_ACK) -> {
                                    val fPingAck = unpackData<JfgUdpMsg.FPingAck>(it.data, JfgUdpMsg.FPingAck::class.java)
                                    ScanResult(
                                            uuid = fPingAck.cid,
                                            ip = it.ip,
                                            port = it.port,
                                            updateTime = it.time,
                                            mac = fPingAck.mac,
                                            version = fPingAck.version
                                    )
                                }
                                TextUtils.equals(secondaryHeard.cmd, UdpConstant.PING_ACK) -> {
                                    val pingAck = unpackData<JfgUdpMsg.PingAck>(it.data, JfgUdpMsg.PingAck::class.java)
                                    ScanResult(
                                            uuid = pingAck.cid,
                                            ip = it.ip,
                                            port = it.port,
                                            updateTime = it.time
                                    )
                                }
                                else -> null
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    .filter { it != null }
                    .subscribe({
                        AppLogger.w("scan result:$it")
                        subscriber.onNext(it)
                    }) {
                        it.printStackTrace()
                    }
            subscriber.add(subscribe)
            subscribe = Schedulers.io().createWorker().schedulePeriodically({

                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, JfgUdpMsg.FPing().toBytes())
                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, JfgUdpMsg.FPing().toBytes())

                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, JfgUdpMsg.Ping().toBytes())
                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, JfgUdpMsg.Ping().toBytes())

            }, 0, 1, TimeUnit.SECONDS)
            subscriber.add(subscribe)
        }
                .subscribeOn(Schedulers.io())
                .buffer(5, TimeUnit.SECONDS)
                .first()
                .map {
                    val map = mutableMapOf<String, ScanResult>()
                    for (result in it) {
                        map[result.uuid]?.apply {
                            result.ip = if (ip.isNotEmpty()) ip else result.ip
                            result.uuid = if (uuid.isNotEmpty()) uuid else result.uuid
                            result.port = if (port.toInt() != 0) port else result.port
                            result.updateTime = if (updateTime != 0L) updateTime else result.updateTime
                            result.os = if (os != 0) os else result.os
                            result.mac = if (mac.isNotEmpty()) mac else result.mac
                            result.version = if (version.isNotEmpty()) version else result.version
                            result.net = if (net != 0) net else result.net
                        }
                        map[result.uuid] = result
                    }
                    map.values.toMutableList()
                }
    }

    @JvmStatic
    fun scan(uuid: String): Observable<ScanResult?> {
        return scanDogWiFi()
                .map { it?.first { TextUtils.equals(it.uuid, uuid) } }
                .first { TextUtils.equals(it?.uuid, uuid) }
    }

    data class ScanResult(var uuid: String = "",
                          var ip: String = "",
                          var port: Short = 0,
                          var updateTime: Long = 0,
                          var os: Int = 0,
                          var mac: String = "",
                          var version: String = "",
                          var net: Int = 0
    ) : Parcelable {
        constructor(source: Parcel) : this(
                source.readString(),
                source.readString(),
                source.readInt().toShort(),
                source.readLong(),
                source.readInt(),
                source.readString(),
                source.readString(),
                source.readInt()
        )

        override fun describeContents() = 0

        override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
            writeString(uuid)
            writeString(ip)
            writeInt(port.toInt())
            writeLong(updateTime)
            writeInt(os)
            writeString(mac)
            writeString(version)
            writeInt(net)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<ScanResult> = object : Parcelable.Creator<ScanResult> {
                override fun createFromParcel(source: Parcel): ScanResult = ScanResult(source)
                override fun newArray(size: Int): Array<ScanResult?> = arrayOfNulls(size)
            }
        }
    }

}