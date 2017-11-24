package com.cylan.jiafeigou.module.request

import com.cylan.entity.jniCall.JFGDPMsg
import com.cylan.jiafeigou.dp.DpUtils
import com.cylan.jiafeigou.module.message.DPList
import com.cylan.jiafeigou.module.message.MIDHeader
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.rx.RxBus
import com.cylan.jiafeigou.rx.RxEvent
import com.cylan.jiafeigou.support.log.AppLogger
import org.msgpack.annotation.Index
import org.msgpack.annotation.Message
import rx.Observable
import rx.schedulers.Schedulers
import java.lang.reflect.ParameterizedType
import java.util.*

/**
 * Created by yanzhendong on 2017/11/13.
 */

interface IRequest<T : IResponse> {

    fun execute(): Observable<T>
}

interface IResponse {

}

abstract class AbstractRequest<T : IResponse>(
        msgId: Int,
        caller: String,
        callee: String,
        seq: Long
) : IRequest<T>, MIDHeader(msgId, caller, callee, seq) {

    fun convert(header: MIDHeader): T {
        val parameterizedType = javaClass.genericSuperclass as ParameterizedType
        val responseType: Class<T> = parameterizedType.actualTypeArguments[0] as Class<T>
        return DpUtils.unpackData(header.rawBytes, responseType)
    }


}

abstract class AbstractResponse : IResponse, MIDHeader() {
}

@Message
class RobotGetDataRequest(
        caller: String = "",
        callee: String = "",
        seq: Long = 0L,
        @JvmField @field:Index(4) var limit: Int = 20,
        @JvmField @field:Index(5) var asc: Boolean = false,
        @JvmField @field:Index(6) var reqList: ByteArray = byteArrayOf(),
        @JvmField @field:Index(7) var equal: Boolean = false
) : AbstractRequest<RobotGetDataResponse>(20200, caller, callee, seq) {
    override fun execute(): Observable<RobotGetDataResponse> {
        BaseApplication.getAppComponent().getCmd().robotGetData(caller, null, limit, asc, 0)
        return Observable.empty()
    }
}

@Message
class RobotGetDataResponse(@JvmField @field:Index(4) var dataMap: Map<Int, DPList> = mutableMapOf()) : AbstractResponse() {
}

@Message
class RobotSetDataRequest(
        caller: String = "",
        callee: String = "",
        seq: Long = 0,
        @JvmField @field:Index(4) var reqList: DPList
) : AbstractRequest<RobotSetDataResponse>(20202, caller, callee, seq) {
    override fun execute(): Observable<RobotSetDataResponse> {
        Observable.create<RobotSetDataResponse> { subscriber ->
            reqList.map {
                JFGDPMsg(it.msgId, it.version, it.value)
            }.apply {
                val seq = BaseApplication.getAppComponent().getCmd().robotSetData(caller, this as ArrayList<JFGDPMsg>?)
                RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp::class.java)
                        .first { it.seq == seq }
                        .subscribe {

                        }
            }
        }
                .subscribeOn(Schedulers.io())


        return Observable.empty();
    }

}

class RobotSetDataResponse : AbstractResponse()

@Message
class RobotForwardDataV3Request(
        caller: String = "",
        callee: String = "",
        @JvmField @field:Index(4) var action: Int = 0,
        @JvmField @field:Index(5) var values: DPList = DPList()
) : AbstractRequest<RobotForwardDataV3Response>(20224, caller, callee, Math.abs(Random().nextLong())) {

    override fun execute(): Observable<RobotForwardDataV3Response> {
        return Observable.create<RobotForwardDataV3Response> { subscriber ->
            val subscribe = RxBus.getCacheInstance().toObservable(MIDHeader::class.java).first { it.seq == seq }.subscribe({
                val dataV3Response = convert(it)
                subscriber.onNext(dataV3Response)
                subscriber.onCompleted()
            }) {
                it.printStackTrace()
                subscriber.onError(it)
            }
            subscriber.add(subscribe)
            val appCmd = BaseApplication.getAppComponent().getCmd()
            val bytes = DpUtils.pack(this@RobotForwardDataV3Request)
            AppLogger.w("正在发送 RobotForwardDataV3Request,原始 bytes 为:${Arrays.toString(bytes)}")
            appCmd.SendForwardData(bytes)
        }.subscribeOn(Schedulers.io())
    }
}

@Message
class RobotForwardDataV3Response(
        @JvmField @field:Index(4) var action: Int = 0,
        @JvmField @field:Index(5) var values: DPList = DPList()
) : AbstractResponse()