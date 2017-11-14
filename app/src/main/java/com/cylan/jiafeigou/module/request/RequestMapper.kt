package com.cylan.jiafeigou.module.request

import com.cylan.entity.jniCall.JFGDPMsg
import com.cylan.jiafeigou.module.message.DPList
import com.cylan.jiafeigou.module.message.DPListConverter
import com.cylan.jiafeigou.module.message.MIDHeader
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.rx.RxBus
import com.cylan.jiafeigou.rx.RxEvent
import org.msgpack.MessagePack
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
        return msgPack.createBufferUnpacker(header.rawBytes).read(responseType)
    }

    companion object {
        @JvmStatic
        val msgPack: MessagePack = MessagePack()
    }

    init {
        msgPack.register(DPList::class.java, DPListConverter())
    }
}

abstract class AbstractResponse : IResponse, MIDHeader() {
}

@Message
class RobotGetDataRequest(
        caller: String = "",
        callee: String = "",
        seq: Long = 0L,
        @Index(4) var limit: Int = 20,
        @Index(5) var asc: Boolean = false,
        @Index(6) var reqList: ByteArray = byteArrayOf(),
        @Index(7) var equal: Boolean = false
) : AbstractRequest<RobotGetDataResponse>(20200, caller, callee, seq) {
    override fun execute(): Observable<RobotGetDataResponse> {
        BaseApplication.getAppComponent().getCmd().robotGetData(caller, null, limit, asc, 0)
        return Observable.empty()
    }
}

@Message
class RobotGetDataResponse(@Index(4) var dataMap: Map<Int, DPList> = mutableMapOf()) : AbstractResponse() {
}

@Message
class RobotSetDataRequest(
        caller: String = "",
        callee: String = "",
        seq: Long = 0,
        @Index(4) var reqList: DPList
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