package com.cylan.jiafeigou.utils

import android.text.TextUtils
import com.cylan.entity.jniCall.JFGDPMsg
import com.cylan.entity.jniCall.RobotoGetDataRsp
import com.cylan.ex.JfgException
import com.cylan.jiafeigou.BuildConfig
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.dp.DpMsgMap
import com.cylan.jiafeigou.dp.DpUtils
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.misc.JError
import com.cylan.jiafeigou.misc.JFGRules
import com.cylan.jiafeigou.misc.bind.UdpConstant
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.rx.RxBus
import com.cylan.jiafeigou.rx.RxEvent
import com.cylan.jiafeigou.support.OptionsImpl
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.udpMsgPack.JfgUdpMsg
import com.google.gson.Gson
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by yanzhendong on 2017/11/29.
 */
object BindHelper {
    private val TIME_OUT = (90 * 1000).toLong()
    private val INTERVAL = 3
    @JvmStatic
    fun sendWiFiConfig(uuid: String, mac: String, ssid: String, password: String, security: Int = 0): Observable<JfgUdpMsg.DoSetWifiAck> {
        return Observable.create<JfgUdpMsg.DoSetWifiAck> { subscriber ->
            //            RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg::class.java)
//                    .map {
//                        if (BuildConfig.DEBUG) {
//                            Log.i(JConstant.CYLAN_TAG, "正在解析 UDP 消息:" + Gson().toJson(it))
//                        }
//                        val secondaryHeard = unpackData<JfgUdpMsg.UdpSecondaryHeard>(it.data, JfgUdpMsg.UdpSecondaryHeard::class.java)
//                        return@map when {
//                            TextUtils.equals(secondaryHeard.cmd, UdpConstant.SET_WIFI_ACK) -> {
//                                val fPingAck = unpackData<JfgUdpMsg.DoSetWifiAck>(it.data, JfgUdpMsg.DoSetWifiAck::class.java)
//                            }
//                            else -> null
//                        }
//                    }
            val setWifi = JfgUdpMsg.DoSetWifi(uuid, mac, ssid, password)
            setWifi.security = security
            BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, setWifi.toBytes())
            BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, setWifi.toBytes())
            subscriber.onNext(JfgUdpMsg.DoSetWifiAck())
            subscriber.onCompleted()
        }
                .subscribeOn(Schedulers.io())
    }

    @JvmStatic
    fun sendServerConfig(uuid: String, mac: String, languageType: Int): Observable<Any> {
        return Observable.create<Any> { subscriber ->
            var serverAddress = OptionsImpl.getServer()
            val port = Integer.parseInt(serverAddress.substring(serverAddress.indexOf(":") + 1))
            serverAddress = serverAddress.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            if (TextUtils.isEmpty(serverAddress) && BuildConfig.DEBUG) {
                throw IllegalArgumentException("server address is empty")
            }
            //设置语言
            val setLanguage = JfgUdpMsg.SetLanguage(uuid, mac, languageType)
            //设置服务器
            val setServer = JfgUdpMsg.SetServer(uuid, mac, serverAddress, port, 80)
            //增加绑定随机数.
            val bindCode = BaseApplication.getAppComponent().getSourceManager().jfgAccount.account + System.currentTimeMillis()
            val code = JfgUdpMsg.FBindDeviceCode(uuid, mac, bindCode)
            for (i in 0..1) {
                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, code.toBytes())
                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, code.toBytes())
            }
            for (i in 0..2) {
                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, setServer.toBytes())
                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, setServer.toBytes())

                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, setLanguage.toBytes())
                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, setLanguage.toBytes())
            }
            val json = PreferencesUtils.getString(JConstant.BINDING_DEVICE)
            val gson = Gson()
            val devicePortrait = gson.fromJson<UdpConstant.UdpDevicePortrait>(json, UdpConstant.UdpDevicePortrait::class.java)
            devicePortrait.bindCode = MD5Util.lowerCaseMD5(bindCode)//cast to md5
            PreferencesUtils.putString(JConstant.BINDING_DEVICE, gson.toJson(devicePortrait))
            AppLogger.i(UdpConstant.BIND_TAG + "setServer: " + gson.toJson(setServer))
            AppLogger.i(UdpConstant.BIND_TAG + "setLanguage: " + gson.toJson(setLanguage))
            AppLogger.i(UdpConstant.BIND_TAG + "setCode: " + gson.toJson(code))
            subscriber.onNext("good")
            subscriber.onCompleted()
        }
    }

    @JvmStatic
    fun sendBindConfig(uuid: String, bindCode: String, mac: String, bindFlag: Int): Observable<Boolean> {
        return Observable.create<Boolean>({ subscriber: Subscriber<in Boolean> ->
            var subscribe = RxBus.getCacheInstance().toObservable(RxEvent.BindDeviceEvent::class.java)
                    .observeOn(Schedulers.io())
                    .filter { bindDeviceEvent ->
                        if (bindDeviceEvent.bindResult != JError.ErrorOK) {
                            subscriber.onError(RxEvent.HelperBreaker(bindDeviceEvent))
                        }
                        bindDeviceEvent.bindResult == JError.ErrorOK
                    }
                    .map { _ ->
                        var seq: Long = -1
                        try {
                            val params = ArrayList<JFGDPMsg>(1)
                            val msg = JFGDPMsg(201, 0)
                            params.add(msg)
                            seq = BaseApplication.getAppComponent().getCmd().robotGetData(uuid, params, 1, false, 0)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            AppLogger.e(e)
                        }

                        seq
                    }
                    .flatMap { seq -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp::class.java).filter { rsp -> rsp.seq == seq } }
                    .subscribe({ rsp ->
                        rsp?.map?.get(DpMsgMap.ID_201_NET)?.getOrNull(0)?.apply {
                            val dpNet = DpUtils.unpackDataWithoutThrow(this.packValue, DpMsgDefine.DPNet::class.java, DpMsgDefine.DPNet())
                            AppLogger.w("绑定查询的设备网络状态为:" + dpNet)
                            if (JFGRules.isDeviceOnline(dpNet)) {
                                DataSourceManager.getInstance().syncAllProperty(uuid)
                                subscriber.onNext(true)
                                subscriber.onCompleted()
                            }
                        }
                    }) { e ->
                        e.printStackTrace()
                        AppLogger.e(e)
                    }

            subscriber.add(subscribe)

            subscribe = Schedulers.io().createWorker().schedulePeriodically({
                try {
                    val ret = BaseApplication.getAppComponent().getCmd().bindDevice(uuid, bindCode, mac, bindFlag)
                    AppLogger.w("正在发送绑定请求:uuid:$uuid,bindCode:$bindCode,mac:$mac,bindFlag:$bindFlag,ret:$ret")
                } catch (e: JfgException) {
                    e.printStackTrace()
                    AppLogger.e(e)
                }
            }, 0, INTERVAL.toLong(), TimeUnit.SECONDS)
            subscriber.add(subscribe)
        })
                .timeout(TIME_OUT, TimeUnit.MILLISECONDS, Observable.just(false))
                .observeOn(AndroidSchedulers.mainThread())
    }

}