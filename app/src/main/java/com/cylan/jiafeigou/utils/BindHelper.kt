package com.cylan.jiafeigou.utils

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
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
import com.cylan.jiafeigou.module.Command
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
    const val TAG = "BindHelper:"
    private val TIME_OUT = (90 * 1000).toLong()
    private val INTERVAL = 3


    data class BindContext(
            var errorCode: Int = 0,
            var errorMessage: String? = "",
            var uuid: String = "",
            var mac: String? = "",
            var ssid: String? = "",
            var password: String? = "",
            var security: Int = -1,
            var ipAddress: String? = "",
            var languageType: Int = -1,
            var serverAddress: String = "",
            var devicePort: Int = -1,
            var bindCode: String = "",
            var net: Int = 0,
            var pid: Int = 0,
            var os: Int = 0,
            var version: String? = ""
    ) : Parcelable {

        constructor(uuid: String) : this(uuid = uuid, errorCode = 0)

        constructor(source: Parcel) : this(
                source.readInt(),
                source.readString(),
                source.readString(),
                source.readString(),
                source.readString(),
                source.readString(),
                source.readInt(),
                source.readString(),
                source.readInt(),
                source.readString(),
                source.readInt(),
                source.readString(),
                source.readInt(),
                source.readInt(),
                source.readInt(),
                source.readString()
        )

        override fun describeContents() = 0

        override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
            writeInt(errorCode)
            writeString(errorMessage)
            writeString(uuid)
            writeString(mac)
            writeString(ssid)
            writeString(password)
            writeInt(security)
            writeString(ipAddress)
            writeInt(languageType)
            writeString(serverAddress)
            writeInt(devicePort)
            writeString(bindCode)
            writeInt(net)
            writeInt(pid)
            writeInt(os)
            writeString(version)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<BindContext> = object : Parcelable.Creator<BindContext> {
                override fun createFromParcel(source: Parcel): BindContext = BindContext(source)
                override fun newArray(size: Int): Array<BindContext?> = arrayOfNulls(size)
            }
        }

        fun onUpdateWiFiConfig(ssid: String, password: String, security: Int) {
            this.ssid = ssid
            this.password = password
            this.security = security
        }
    }

    const val EVENT_TYPE_WIFI_CONFIG = 1
    const val EVENT_TYPE_SERVER_CONFIG = 2
    const val EVENT_TYPE_LANGUAGE_CONFIG = 3
    const val EVENT_TYPE_BIND_CODE_CONFIG = 4
    const val EVENT_TYPE_PING_ACTION = 5
    const val EVENT_TYPE_RECEIVE_LOCAL_MESSAGE = 6

    const val PARAMS_ERROR_NO_ERROR = 0

    const val PARAMS_ERROR_UUID_ERROR = -1
    const val PARAMS_ERROR_MAC_ERROR = -2
    const val PARAMS_ERROR_SSID_ERROR = -3
    const val PARAMS_ERROR_PASSWORD_ERROR = -4
    const val PARAMS_ERROR_IP_ADDRESS_ERROR = -5

    @JvmStatic
    fun checkParamsForEventType(bindContext: BindContext, eventType: Int) {
        when (eventType) {
            EVENT_TYPE_WIFI_CONFIG -> {
                when {
                    bindContext.uuid.isNullOrEmpty() -> {
                        bindContext.errorCode = PARAMS_ERROR_UUID_ERROR
                        bindContext.errorMessage = "无效的uuid:${bindContext.uuid}"
                    }
                    bindContext.mac.isNullOrEmpty() -> {
                        bindContext.errorCode = PARAMS_ERROR_MAC_ERROR
                        bindContext.errorMessage = "无效的mac 地址:${bindContext.mac}"
                    }
                    bindContext.ssid.isNullOrEmpty() -> {
                        bindContext.errorCode = PARAMS_ERROR_SSID_ERROR
                        bindContext.errorMessage = "无效的 ssid:${bindContext.ssid}"
                    }
                    bindContext.password.isNullOrEmpty() -> {
                        bindContext.errorCode = PARAMS_ERROR_PASSWORD_ERROR
                        bindContext.errorMessage = "无效的 password:${bindContext.password}"
                    }
                    bindContext.ipAddress.isNullOrEmpty() -> {
                        bindContext.errorCode = PARAMS_ERROR_IP_ADDRESS_ERROR
                        bindContext.errorMessage = "无效的 ip 地址:${bindContext.ipAddress}"
                    }

                }
            }
        }
    }

    @JvmStatic
    fun checkParamsForFullBind(bindContext: BindContext): Int {
        return PARAMS_ERROR_NO_ERROR
    }

    @JvmStatic
    fun considerUsefulLocalMessage(bindContext: BindContext, localUdpMsg: RxEvent.LocalUdpMsg) {
        checkParamsForEventType(bindContext, EVENT_TYPE_RECEIVE_LOCAL_MESSAGE)
        if (bindContext.errorCode == PARAMS_ERROR_NO_ERROR) {

            val udpHeader = DpUtils.unpackDataWithoutThrow(localUdpMsg.data, JfgUdpMsg.UdpHeader::class.java, null)
            when {
                TextUtils.equals(udpHeader?.cmd, UdpConstant.PING_ACK) -> {
                    val pingAck = DpUtils.unpackDataWithoutThrow(localUdpMsg.data, JfgUdpMsg.PingAck::class.java, null)
                    bindContext.net = pingAck?.net ?: bindContext.net
                    bindContext.pid = pingAck?.pid ?: bindContext.pid
                }
                TextUtils.equals(udpHeader?.cmd, UdpConstant.F_PING_ACK) -> {
                    val fPingAck = DpUtils.unpackDataWithoutThrow(localUdpMsg.data, JfgUdpMsg.FPingAck::class.java, null)
                    bindContext.os = fPingAck?.os ?: bindContext.os
                    bindContext.version = fPingAck?.version ?: bindContext.version
                    bindContext.mac = fPingAck?.mac ?: bindContext.mac
                }
            }

        }
    }

    @JvmStatic
    fun performPingAction(bindContext: BindContext) {
        checkParamsForEventType(bindContext, EVENT_TYPE_PING_ACTION)
        if (bindContext.errorCode == PARAMS_ERROR_NO_ERROR) {
            val pingBytes = JfgUdpMsg.Ping().toBytes()
            val fpingBytes = JfgUdpMsg.FPing().toBytes()
            for (i in 0..2) {
                Command.getInstance().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, pingBytes)
                Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, pingBytes)
                Command.getInstance().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, fpingBytes)
                Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, fpingBytes)
            }
        }
    }

    @JvmStatic
    fun sendWiFiConfig(bindContext: BindContext) {
        checkParamsForEventType(bindContext, EVENT_TYPE_WIFI_CONFIG)
        if (bindContext.errorCode == PARAMS_ERROR_NO_ERROR) {
            val setWifi = JfgUdpMsg.DoSetWifi(bindContext.uuid, bindContext.mac, bindContext.ssid, bindContext.password)
            for (i in 1..3) {
                Command.getInstance().sendLocalMessage(bindContext.ipAddress!!, UdpConstant.PORT, setWifi.toBytes())
                Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, setWifi.toBytes())
            }
        }
    }

    @JvmStatic
    fun performRepairAction(bindContext: BindContext, errorCode: Int): Boolean {
        return true
    }

    @JvmStatic
    fun isNoError(bindContext: BindContext): Boolean {
        return bindContext.errorCode == PARAMS_ERROR_NO_ERROR
    }

    @JvmStatic
    fun sendServerConfig(bindContext: BindContext) {
        checkParamsForEventType(bindContext, EVENT_TYPE_SERVER_CONFIG)
        if (bindContext.errorCode == PARAMS_ERROR_NO_ERROR) {
            //设置服务器
            val setServer = JfgUdpMsg.SetServer(bindContext.uuid, bindContext.mac, bindContext.serverAddress, bindContext.devicePort, 80)
            for (i in 0..2) {
                Command.getInstance().sendLocalMessage(bindContext.ipAddress!!, UdpConstant.PORT, setServer.toBytes())
                Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, setServer.toBytes())
            }
        }
    }

    @JvmStatic
    fun sendLanguageConfig(bindContext: BindContext) {
        checkParamsForEventType(bindContext, EVENT_TYPE_LANGUAGE_CONFIG)
        if (bindContext.errorCode == PARAMS_ERROR_NO_ERROR) {
            //设置语言
            val setLanguage = JfgUdpMsg.SetLanguage(bindContext.uuid, bindContext.mac, bindContext.languageType)
            for (i in 0..2) {
                Command.getInstance().sendLocalMessage(bindContext.ipAddress!!, UdpConstant.PORT, setLanguage.toBytes())
                Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, setLanguage.toBytes())
            }
        }
    }

    @JvmStatic
    fun sendBindCodeConfig(bindContext: BindContext) {
        checkParamsForEventType(bindContext, EVENT_TYPE_BIND_CODE_CONFIG)
        if (bindContext.errorCode == PARAMS_ERROR_NO_ERROR) {
            val code = JfgUdpMsg.FBindDeviceCode(bindContext.uuid, bindContext.mac, bindContext.bindCode)
            for (i in 0..1) {
                Command.getInstance().sendLocalMessage(bindContext.ipAddress!!, UdpConstant.PORT, code.toBytes())
                Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, code.toBytes())
            }
        }
    }


    @JvmStatic
    fun sendWiFiConfig(uuid: String, mac: String, ssid: String, password: String, security: Int = 0): Observable<JfgUdpMsg.DoSetWifiAck> {
        return Observable.create<JfgUdpMsg.DoSetWifiAck> { subscriber ->
            val setWifi = JfgUdpMsg.DoSetWifi(uuid, mac, ssid, password)
            val gson = Gson()
            val json = PreferencesUtils.getString(JConstant.BINDING_DEVICE)
            val devicePortrait = gson.fromJson<UdpConstant.UdpDevicePortrait>(json, UdpConstant.UdpDevicePortrait::class.java)
            val deviceIP = devicePortrait?.ipAddress ?: UdpConstant.IP
            setWifi.security = security
            for (i in 1..3) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "sendWiFiConfig:uuid is:$uuid,mac is:$mac,ssid is:$ssid,password is:$password,security is:$security")
                }
                Command.getInstance().sendLocalMessage(deviceIP, UdpConstant.PORT, setWifi.toBytes())
                Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, setWifi.toBytes())
            }
            subscriber.onNext(JfgUdpMsg.DoSetWifiAck())
            subscriber.onCompleted()
        }
                .subscribeOn(Schedulers.io())
                .delay(1, TimeUnit.SECONDS)
    }

    @JvmStatic
    fun sendServerConfig(uuid: String, mac: String, languageType: Int): Observable<Any> {
        return Observable.create<Any> { subscriber ->
            var serverAddress = OptionsImpl.getServer()
            val gson = Gson()
            val json = PreferencesUtils.getString(JConstant.BINDING_DEVICE)
            val devicePortrait = gson.fromJson<UdpConstant.UdpDevicePortrait>(json, UdpConstant.UdpDevicePortrait::class.java)
            val deviceIP = devicePortrait?.ipAddress ?: UdpConstant.IP
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
            val bindCode = DataSourceManager.getInstance().jfgAccount.account + System.currentTimeMillis()
            val code = JfgUdpMsg.FBindDeviceCode(uuid, mac, bindCode)
            for (i in 0..1) {
                Command.getInstance().sendLocalMessage(deviceIP, UdpConstant.PORT, code.toBytes())
                Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, code.toBytes())
            }
            for (i in 0..2) {
                Command.getInstance().sendLocalMessage(deviceIP, UdpConstant.PORT, setServer.toBytes())
                Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, setServer.toBytes())

                Command.getInstance().sendLocalMessage(deviceIP, UdpConstant.PORT, setLanguage.toBytes())
                Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, setLanguage.toBytes())
            }

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
            val bindSubscriber = Schedulers.io().createWorker().schedulePeriodically({
                var ret: Int = -1
                try {
                    //需要检查参数合法性
                    if (uuid.isEmpty() || bindCode.isEmpty() || mac.isEmpty()) {
                        AppLogger.e("无效的绑定参数: uuid:$uuid,bindCode:$bindCode,mac:$mac,bindFlag:$bindFlag")
                        subscriber.onError(IllegalArgumentException("无效的绑定参数: uuid:$uuid,bindCode:$bindCode,mac:$mac,bindFlag:$bindFlag"))
                        return@schedulePeriodically
                    }
                    ret = Command.getInstance().bindDevice(uuid, bindCode, mac, bindFlag)
                    AppLogger.w("正在发送绑定请求:uuid:$uuid,bindCode:$bindCode,mac:$mac,bindFlag:$bindFlag,ret:$ret")
                } catch (e: JfgException) {
                    AppLogger.w("发送绑定请求失败了:uuid:$uuid,bindCode:$bindCode,mac:$mac,bindFlag:$bindFlag,ret:$ret")
                    e.printStackTrace()
                    AppLogger.e(e)
                }
            }, 1, INTERVAL.toLong(), TimeUnit.SECONDS)
            subscriber.add(bindSubscriber)

            val subscribe = RxBus.getCacheInstance().toObservable(RxEvent.BindDeviceEvent::class.java)
                    .observeOn(Schedulers.io())
                    .filter { bindDeviceEvent ->
                        bindSubscriber.unsubscribe()
                        if (bindDeviceEvent.bindResult != JError.ErrorOK) {
                            subscriber.onError(RxEvent.HelperBreaker(bindDeviceEvent))
                        }
                        bindDeviceEvent.bindResult == JError.ErrorOK
                    }
                    .first()
                    .flatMap { _ -> Observable.interval(INTERVAL.toLong(), TimeUnit.SECONDS) }
                    .map { _ ->
                        var seq: Long = -1
                        try {
                            val params = ArrayList<JFGDPMsg>(1)
                            val msg = JFGDPMsg(201, 0)
                            params.add(msg)
                            seq = Command.getInstance().robotGetData(uuid, params, 1, false, 0)
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
                        subscriber.onError(e)
                        e.printStackTrace()
                        AppLogger.e(e)
                    }
            subscriber.add(subscribe)
        })
                .timeout(TIME_OUT, TimeUnit.MILLISECONDS, Observable.just(false))
                .observeOn(AndroidSchedulers.mainThread())
    }

}