package com.cylan.jiafeigou.utils

import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import com.cylan.jiafeigou.BuildConfig
import com.cylan.jiafeigou.dp.DpUtils.unpackData
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.misc.bind.UdpConstant
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.rx.RxBus
import com.cylan.jiafeigou.rx.RxEvent
import com.cylan.udpMsgPack.JfgUdpMsg
import com.google.gson.Gson
import rx.Observable
import rx.schedulers.Schedulers

/**
 * Created by yanzhendong on 2017/11/22.
 */
object APObserver {
    private val observers: MutableMap<String, Any> = mutableMapOf()
    private val scanResult = mutableMapOf<String, ScanResult>()
    private val handler: Handler = LoopHandler()
    private val START_SCAN = 10000
    private val SCAN_PERIOD = 5000//五秒扫描
    fun addObserver(uuid: String) {

    }

    fun removeObserver(uuid: String) {
        observers.remove(uuid)
    }

    fun scan() {

    }

    fun scan(uuid: String): Observable<ScanResult> {
        return Observable.create<ScanResult> { subscriber ->
            RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg::class.java)
                    .map {
                        if (BuildConfig.DEBUG) {
                            Log.i(JConstant.CYLAN_TAG, "正在解析 UDP 消息:" + Gson().toJson(it))
                        }

                        val secondaryHeard = unpackData<JfgUdpMsg.UdpSecondaryHeard>(it.data, JfgUdpMsg.UdpSecondaryHeard::class.java)
                        ScanResult(secondaryHeard.cid, it.ip, it.port, it.time, secondaryHeard)
                    }
                    .first { TextUtils.equals(it.uuid, uuid) && TextUtils.equals(it.header.cmd, UdpConstant.F_PING_ACK) }
                    .subscribe({
                        subscriber.onNext(it)
                        subscriber.onCompleted()
                    }) {
                        subscriber.onError(it)
                    }

            BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, JfgUdpMsg.FPing().toBytes())
            BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, JfgUdpMsg.FPing().toBytes())
            BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, JfgUdpMsg.FPing().toBytes())
            BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, JfgUdpMsg.FPing().toBytes())
        }
                .subscribeOn(Schedulers.io())
    }

    data class ScanResult(var uuid: String, var ip: String, var port: Short, var updateTime: Long, var header: JfgUdpMsg.UdpSecondaryHeard)

    private class LoopHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                START_SCAN -> {
                    if (observers.isNotEmpty()) {
                        scan()
                    }
                }
            }
        }
    }
}