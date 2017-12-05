package com.cylan.jiafeigou.module

import android.util.Log
import com.cylan.entity.jniCall.*
import com.cylan.jfgapp.interfases.AppCallBack
import com.cylan.jiafeigou.base.module.BaseForwardHelper
import com.cylan.jiafeigou.base.module.BaseUdpMsgParser
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.base.module.PanoramaEvent
import com.cylan.jiafeigou.dp.DpUtils
import com.cylan.jiafeigou.module.message.MIDHeader
import com.cylan.jiafeigou.rx.RxBus
import com.cylan.jiafeigou.rx.RxEvent
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ListUtils
import com.google.gson.Gson
import rx.Observable
import rx.subjects.PublishSubject
import java.util.*

@Suppress("unused", "ArrayInDataClass")
/**
 * Created by yzd on 17-12-3.
 */


object AppCallbackSupervisor : AppCallBack, Supervisor {
    private val TAG = AppCallbackSupervisor::class.java.simpleName
    private val publisher = PublishSubject.create<Any>().toSerialized()
    private val gson = Gson()

    private fun publish(event: Any) {
        HookerSupervisor.performHooker(this, PublishAction(), PublishParameter(event))
    }

    private fun <T> observe(eventType: Class<T>): Observable<T> {
        return HookerSupervisor.performHooker(this, ObserverAction(), ObserverParameter<T>(publisher.ofType(eventType)))?.observable!!
    }

    interface PublishHooker : Supervisor.Hooker<PublishParameter>

    class PublishParameter(var event: Any) : Supervisor.Parameter

    class PublishAction : Supervisor.Action<PublishParameter> {
        override fun process(parameter: PublishParameter): PublishParameter? {
            publisher.onNext(parameter.event)
            return parameter
        }
    }

    interface ObserverHooker : Supervisor.Hooker<ObserverParameter<*>>

    class ObserverParameter<T>(var observable: Observable<T>) : Supervisor.Parameter

    class ObserverAction<T> : Supervisor.Action<ObserverParameter<T>> {
        override fun process(parameter: ObserverParameter<T>): ObserverParameter<T>? {
            return parameter
        }

    }

    data class LocalMessageEvent(var s: String, var i: Int, var bytes: ByteArray)

    override fun OnLocalMessage(s: String, i: Int, bytes: ByteArray) {
        Log.d(TAG, "OnLocalMessage:$s,$i,${Arrays.toString(bytes)}")
        val localUdpMsg = RxEvent.LocalUdpMsg(System.currentTimeMillis(), s, i.toShort(), bytes)
        publisher.onNext(LocalMessageEvent(s, i, bytes))
        BaseUdpMsgParser.getInstance().parserUdpMessage(localUdpMsg)
    }

    @JvmStatic
    fun observeLocalMessage(): Observable<LocalMessageEvent> {
        return publisher.ofType(LocalMessageEvent::class.java)
    }

    data class ReportDeviceEvent(var devices: Array<JFGDevice>)

    override fun OnReportJfgDevices(jfgDevices: Array<JFGDevice>) {
        Log.d(TAG, "OnReportJfgDevices:${gson.toJson(jfgDevices)}")
        publisher.onNext(ReportDeviceEvent(jfgDevices))
        RxBus.getCacheInstance().post(RxEvent.SerializeCacheDeviceEvent(jfgDevices))
    }

    @JvmStatic
    fun observeReportDevice(): Observable<ReportDeviceEvent> {
        return publisher.ofType(ReportDeviceEvent::class.java)
    }

    data class UpdateAccountEvent(var jfgAccount: JFGAccount)

    override fun OnUpdateAccount(jfgAccount: JFGAccount) {
        Log.d(TAG, "OnUpdateAccount:${gson.toJson(jfgAccount)}")
        publisher.onNext(UpdateAccountEvent(jfgAccount))
        RxBus.getCacheInstance().post(RxEvent.SerializeCacheAccountEvent(jfgAccount))

    }

    @JvmStatic
    fun observeUpdateAccount(): Observable<UpdateAccountEvent> {
        return publisher.ofType(UpdateAccountEvent::class.java)
    }

    data class UpdateHistoryVideoListEvent(var jfgHistoryVideo: JFGHistoryVideo)

    override fun OnUpdateHistoryVideoList(jfgHistoryVideo: JFGHistoryVideo) {
        AppLogger.w("OnUpdateHistoryVideoList :" + jfgHistoryVideo.list.size)
        publisher.onNext(UpdateHistoryVideoListEvent(jfgHistoryVideo))
        DataSourceManager.getInstance().cacheHistoryDataList(jfgHistoryVideo)
    }

    @JvmStatic
    fun observeUpdateHistoryVideoList(): Observable<UpdateHistoryVideoListEvent> {
        return publisher.ofType(UpdateHistoryVideoListEvent::class.java)
    }

    data class UpdateHistoryVideoV2Event(var bytes: ByteArray)

    override fun OnUpdateHistoryVideoV2(bytes: ByteArray) {
        Log.d(TAG, "OnUpdateHistoryVideoV2:${Arrays.toString(bytes)}")
        publisher.onNext(UpdateHistoryVideoV2Event(bytes))
        DataSourceManager.getInstance().cacheHistoryDataList(bytes)
    }

    @JvmStatic
    fun observeUpdateHistoryVideoV2(): Observable<UpdateHistoryVideoV2Event> {
        return publisher.ofType(UpdateHistoryVideoV2Event::class.java)
    }

    data class UpdateHistoryErrorCodeEvent(var error: JFGHistoryVideoErrorInfo)

    override fun OnUpdateHistoryErrorCode(jfgHistoryVideoErrorInfo: JFGHistoryVideoErrorInfo) {
        AppLogger.w("OnUpdateHistoryErrorCode :" + gson.toJson(jfgHistoryVideoErrorInfo))
        publisher.onNext(UpdateHistoryErrorCodeEvent(jfgHistoryVideoErrorInfo))
        RxBus.getCacheInstance().post(jfgHistoryVideoErrorInfo)
    }

    @JvmStatic
    fun observeUpdateHistoryErrorCode(): Observable<UpdateHistoryErrorCodeEvent> {
        return publisher.ofType(UpdateHistoryErrorCodeEvent::class.java)
    }

    data class ServerConfigEvent(var config: JFGServerCfg)

    override fun OnServerConfig(jfgServerCfg: JFGServerCfg) {
        AppLogger.w("OnServerConfig :" + gson.toJson(jfgServerCfg))
        publisher.onNext(ServerConfigEvent(jfgServerCfg))
        RxBus.getCacheInstance().post(jfgServerCfg)
    }

    @JvmStatic
    fun observeServerConfig(): Observable<ServerConfigEvent> {
        return publisher.ofType(ServerConfigEvent::class.java)
    }

    data class LogoutByServerEvent(var i: Int)

    override fun OnLogoutByServer(i: Int) {
        AppLogger.w("OnLogoutByServer:" + i)
        publisher.onNext(LogoutByServerEvent(i))
        LoginHelper.performLogout()
        RxBus.getCacheInstance().post(RxEvent.PwdHasResetEvent(i))
    }

    @JvmStatic
    fun observeLogoutByServer(): Observable<LogoutByServerEvent> {
        return publisher.ofType(LogoutByServerEvent::class.java)
    }

    data class VideoDisconnectEvent(var disconnect: JFGMsgVideoDisconn)

    override fun OnVideoDisconnect(jfgMsgVideoDisconn: JFGMsgVideoDisconn) {
        AppLogger.w("OnVideoDisconnect :" + gson.toJson(jfgMsgVideoDisconn))
        publisher.onNext(VideoDisconnectEvent(jfgMsgVideoDisconn))
        RxBus.getCacheInstance().post(jfgMsgVideoDisconn)
    }

    @JvmStatic
    fun observeVideoDisconnect(): Observable<VideoDisconnectEvent> {
        return publisher.ofType(VideoDisconnectEvent::class.java)
    }

    data class VideoNotifyResolutionEvent(var resolution: JFGMsgVideoResolution)

    override fun OnVideoNotifyResolution(jfgMsgVideoResolution: JFGMsgVideoResolution) {
        AppLogger.w("OnVideoNotifyResolution" + jfgMsgVideoResolution.peer)
        publisher.onNext(VideoNotifyResolutionEvent(jfgMsgVideoResolution))
        RxBus.getCacheInstance().post(jfgMsgVideoResolution)
    }

    @JvmStatic
    fun observeVideoNotifyResolution(): Observable<VideoNotifyResolutionEvent> {
        return publisher.ofType(VideoNotifyResolutionEvent::class.java)
    }

    data class VideoNotifyRTCPEvent(var rtcp: JFGMsgVideoRtcp)

    override fun OnVideoNotifyRTCP(jfgMsgVideoRtcp: JFGMsgVideoRtcp) {
        Log.w("", "OnVideoNotifyRTCP" + gson.toJson(jfgMsgVideoRtcp))
        publisher.onNext(VideoNotifyRTCPEvent(jfgMsgVideoRtcp))
        RxBus.getCacheInstance().post(jfgMsgVideoRtcp)
    }

    @JvmStatic
    fun observeVideoNotifyRTCP(): Observable<VideoNotifyRTCPEvent> {
        return publisher.ofType(VideoNotifyRTCPEvent::class.java)
    }

    data class HttpDoneEvent(var result: JFGMsgHttpResult)

    override fun OnHttpDone(jfgMsgHttpResult: JFGMsgHttpResult) {
        AppLogger.w("OnHttpDone :" + gson.toJson(jfgMsgHttpResult))
        publisher.onNext(HttpDoneEvent(jfgMsgHttpResult))
        RxBus.getCacheInstance().post(jfgMsgHttpResult)
    }

    @JvmStatic
    fun observeHttpDone(): Observable<HttpDoneEvent> {
        return publisher.ofType(HttpDoneEvent::class.java)
    }

    data class RobotTransmitMsgEvent(var msg: RobotMsg)

    override fun OnRobotTransmitMsg(robotMsg: RobotMsg) {
        AppLogger.w("OnRobotTransmitMsg :" + gson.toJson(robotMsg))
        publisher.onNext(RobotTransmitMsgEvent(robotMsg))
        RxBus.getCacheInstance().post(robotMsg)
    }

    @JvmStatic
    fun observeRobotTransmitMsg(): Observable<RobotTransmitMsgEvent> {
        return publisher.ofType(RobotTransmitMsgEvent::class.java)
    }

    data class RobotMsgAckEvent(val i: Int)

    override fun OnRobotMsgAck(i: Int) {
        AppLogger.w("OnRobotMsgAck :" + i)
        publisher.onNext(RobotMsgAckEvent(i))
    }

    @JvmStatic
    fun observeRobotMsgAck(): Observable<RobotMsgAckEvent> {
        return publisher.ofType(RobotMsgAckEvent::class.java)
    }

    data class RobotGetDataRspEvent(val robotoGetDataRsp: RobotoGetDataRsp)

    override fun OnRobotGetDataRsp(robotoGetDataRsp: RobotoGetDataRsp) {
        AppLogger.w("OnRobotGetDataRsp :" + gson.toJson(robotoGetDataRsp))
        publisher.onNext(RobotGetDataRspEvent(robotoGetDataRsp))
    }

    @JvmStatic
    fun observeRobotGetDataRsp(): Observable<RobotGetDataRspEvent> {
        return publisher.ofType(RobotGetDataRspEvent::class.java)
    }

    data class RobotGetDataExRsp(val l: Long, val s: String, val arrayList: ArrayList<JFGDPMsg>)

    override fun OnRobotGetDataExRsp(l: Long, s: String, arrayList: ArrayList<JFGDPMsg>) {
        AppLogger.w("OnRobotGetDataExRsp :" + s + "," + gson.toJson(arrayList))
        publisher.onNext(RobotGetDataExRsp(l, s, arrayList))
    }

    @JvmStatic
    fun observeRobotGetDataExRsp(): Observable<RobotGetDataExRsp> {
        return publisher.ofType(RobotGetDataExRsp::class.java)
    }

    data class RobotSetDataRspEvent(val l: Long, val uuid: String, val arrayList: ArrayList<JFGDPMsgRet>)

    override fun OnRobotSetDataRsp(l: Long, uuid: String, arrayList: ArrayList<JFGDPMsgRet>) {
        AppLogger.w("OnRobotSetDataRsp :" + l + gson.toJson(arrayList))
        publisher.onNext(RobotSetDataRspEvent(l, uuid, arrayList))
    }

    @JvmStatic
    fun observeRobotSetDataRsp(): Observable<RobotSetDataRspEvent> {
        return publisher.ofType(RobotSetDataRspEvent::class.java)
    }

    data class RobotGetDataTimeoutEvent(val l: Long, val s: String)

    override fun OnRobotGetDataTimeout(l: Long, s: String) {
        AppLogger.w("OnRobotGetDataTimeout :$l:$s")
        publisher.onNext(OnRobotGetDataTimeout(l, s))
    }

    @JvmStatic
    fun observeRobotGetDataTimeout(): Observable<RobotGetDataTimeoutEvent> {
        return publisher.ofType(RobotGetDataTimeoutEvent::class.java)
    }

    override fun OnQuerySavedDatapoint(s: String, arrayList: ArrayList<JFGDPMsg>): ArrayList<JFGDPMsg>? {
        AppLogger.w("这是一个bug")
        return null
    }

    data class OnlineStatusEvent(val b: Boolean)

    override fun OnlineStatus(b: Boolean) {
        publisher.onNext(OnlineStatusEvent(b))
    }

    @JvmStatic
    fun observeOnlineStatus(): Observable<OnlineStatusEvent> {
        return publisher.ofType(OnlineStatusEvent::class.java)
    }

    data class ResultEvent(val result: JFGResult)

    override fun OnResult(jfgResult: JFGResult) {
        AppLogger.w("jfgResult [" + jfgResult.event + ":" + jfgResult.code + "]")
        publisher.onNext(ResultEvent(jfgResult))
    }

    @JvmStatic
    fun observeJfgResult(): Observable<ResultEvent> {
        return publisher.ofType(ResultEvent::class.java)
    }

    data class DoorBellCallEvent(val bellCaller: JFGDoorBellCaller)

    override fun OnDoorBellCall(jfgDoorBellCaller: JFGDoorBellCaller) {
        AppLogger.w("OnDoorBellCall :" + gson.toJson(jfgDoorBellCaller))
        publisher.onNext(DoorBellCallEvent(jfgDoorBellCaller))
    }

    @JvmStatic
    fun observeDoorBellCall(): Observable<DoorBellCallEvent> {
        return publisher.ofType(DoorBellCallEvent::class.java)
    }

    data class OtherClientAnswerCallEvent(val s: String)

    override fun OnOtherClientAnswerCall(s: String) {
        AppLogger.w("OnOtherClientAnswerCall:" + s)
        publisher.onNext(OtherClientAnswerCallEvent(s))
    }


    @JvmStatic
    fun observeOtherClientAnswerCall(): Observable<OtherClientAnswerCallEvent> {
        return publisher.ofType(OtherClientAnswerCallEvent::class.java)
    }

    data class RobotCountDataRspEvent(val l: Long, val s: String, val arrayList: ArrayList<JFGDPMsgCount>)

    override fun OnRobotCountDataRsp(l: Long, s: String, arrayList: ArrayList<JFGDPMsgCount>) {
        AppLogger.w("OnRobotCountDataRsp :$l:$s")
        publisher.onNext(RobotCountDataRspEvent(l, s, arrayList))
    }

    @JvmStatic
    fun observeRobotCountDataRsp(): Observable<RobotCountDataRspEvent> {
        return publisher.ofType(RobotCountDataRspEvent::class.java)
    }

    data class RobotDelDataRspEvent(val l: Long, val s: String, val i: Int)

    override fun OnRobotDelDataRsp(l: Long, s: String, i: Int) {
        AppLogger.w("OnRobotDelDataRsp :$l uuid:$s i:$i")
        publisher.onNext(RobotDelDataRspEvent(l, s, i))
    }

    @JvmStatic
    fun observeRobotDelDataRsp(): Observable<RobotDelDataRspEvent> {
        return publisher.ofType(RobotDelDataRspEvent::class.java)
    }

    data class RobotSyncDateEvent(val b: Boolean, val s: String, val arrayList: ArrayList<JFGDPMsg>?)

    override fun OnRobotSyncData(b: Boolean, s: String, arrayList: ArrayList<JFGDPMsg>) {
        AppLogger.w("OnRobotSyncData :" + b + " " + s + " " + Gson().toJson(arrayList))
        publisher.onNext(RobotSyncDateEvent(b, s, arrayList))
    }

    @JvmStatic
    fun observeRobotSyncData(): Observable<RobotSyncDateEvent> {
        return publisher.ofType(RobotSyncDateEvent::class.java)
    }

    data class SendSMSResultEvent(var i: Int, var s: String)

    override fun OnSendSMSResult(i: Int, s: String) {
        AppLogger.w("OnSendSMSResult :$i,$s")
        publisher.onNext(SendSMSResultEvent(i, s))
    }

    @JvmStatic
    fun observeSendSMSResult(): Observable<SendSMSResultEvent> {
        return publisher.ofType(SendSMSResultEvent::class.java)
    }

    data class GetFriendListRspEvent(var ret: Int, val arrayList: ArrayList<JFGFriendAccount>)

    override fun OnGetFriendListRsp(ret: Int, arrayList: ArrayList<JFGFriendAccount>) {
        AppLogger.w("OnLocalMessage :" + arrayList.size)
        publisher.onNext(GetFriendListRspEvent(ret, arrayList))
    }

    @JvmStatic
    fun observeGetFriendListRsp(): Observable<GetFriendListRspEvent> {
        return publisher.ofType(GetFriendListRspEvent::class.java)
    }

    data class GetFriendRequestListRspEvent(var ret: Int, var arrayList: ArrayList<JFGFriendRequest>)

    override fun OnGetFriendRequestListRsp(ret: Int, arrayList: ArrayList<JFGFriendRequest>) {
        AppLogger.w("OnGetFriendRequestListRsp:" + arrayList.size)
        publisher.onNext(GetFriendRequestListRspEvent(ret, arrayList))
    }

    @JvmStatic
    fun observeGetFriendRequestListRsp(): Observable<GetFriendRequestListRspEvent> {
        return publisher.ofType(GetFriendRequestListRspEvent::class.java)
    }

    data class GetFriendInfoRspEvent(var i: Int, var friendAccount: JFGFriendAccount)

    override fun OnGetFriendInfoRsp(i: Int, jfgFriendAccount: JFGFriendAccount) {
        AppLogger.w("OnLocalMessage :" + Gson().toJson(jfgFriendAccount))
        publisher.onNext(GetFriendInfoRspEvent(i, jfgFriendAccount))
    }

    @JvmStatic
    fun observeGetFriendInfoRsp(): Observable<GetFriendInfoRspEvent> {
        return publisher.ofType(GetFriendInfoRspEvent::class.java)
    }

    data class CheckFriendAccountRspEvent(var i: Int, var s: String, var s1: String, var b: Boolean)

    override fun OnCheckFriendAccountRsp(i: Int, s: String, s1: String, b: Boolean) {
        AppLogger.w("OnCheckFriendAccountRsp :")
        publisher.onNext(CheckFriendAccountRspEvent(i, s, s1, b))
    }

    @JvmStatic
    fun observeCheckFriendAccountRsp(): Observable<CheckFriendAccountRspEvent> {
        return publisher.ofType(CheckFriendAccountRspEvent::class.java)
    }

    data class ShareDeviceRspEvent(var i: Int, var s: String, var s1: String)

    override fun OnShareDeviceRsp(i: Int, s: String, s1: String) {
        AppLogger.w("OnShareDeviceRsp :$i:$s:$s1")
        publisher.onNext(ShareDeviceRspEvent(i, s, s1))
    }

    @JvmStatic
    fun observeShareDeviceRsp(): Observable<ShareDeviceRspEvent> {
        return publisher.ofType(ShareDeviceRspEvent::class.java)
    }

    data class UnShareDeviceRspEvent(var i: Int, var s: String, var s1: String)

    override fun OnUnShareDeviceRsp(i: Int, s: String, s1: String) {
        AppLogger.w("OnUnShareDeviceRsp :$i,$s,$s1")
        publisher.onNext(UnShareDeviceRspEvent(i, s, s1))
    }

    @JvmStatic
    fun observeUnShareDeviceRsp(): Observable<UnShareDeviceRspEvent> {
        return publisher.ofType(UnShareDeviceRspEvent::class.java)
    }

    data class GetShareListRspEvent(var i: Int, var arrayList: ArrayList<JFGShareListInfo>)

    override fun OnGetShareListRsp(i: Int, arrayList: ArrayList<JFGShareListInfo>) {
        AppLogger.w("OnGetShareListRsp :" + i)
        publisher.onNext(GetShareListRspEvent(i, arrayList))
    }

    @JvmStatic
    fun observeGetShareListRsp(): Observable<GetShareListRspEvent> {
        return publisher.ofType(GetShareListRspEvent::class.java)
    }

    data class GetUnShareListByCidRspEvent(var i: Int, var arrayList: ArrayList<JFGFriendAccount>)

    override fun OnGetUnShareListByCidRsp(i: Int, arrayList: ArrayList<JFGFriendAccount>) {
        AppLogger.w("UnShareListByCidEvent :")
        publisher.onNext(GetUnShareListByCidRspEvent(i, arrayList))
    }

    @JvmStatic
    fun observeGetUnShareListByCidRsp(): Observable<GetUnShareListByCidRspEvent> {
        return publisher.ofType(GetUnShareListByCidRspEvent::class.java)
    }

    data class UpdateNTPEvent(var l: Int)

    override fun OnUpdateNTP(l: Int) {
        AppLogger.w("OnUpdateNTP :" + l)
        publisher.onNext(UpdateNTPEvent(l))
    }

    @JvmStatic
    fun observeUpdateNTP(): Observable<UpdateNTPEvent> {
        return publisher.ofType(UpdateNTPEvent::class.java)
    }

    data class ForgetPassByEmailRspEvent(var i: Int, var s: String)

    override fun OnForgetPassByEmailRsp(i: Int, s: String) {
        AppLogger.w("OnForgetPassByEmailRsp :" + s)
        publisher.onNext(ForgetPassByEmailRspEvent(i, s))
    }

    @JvmStatic
    fun observeForgetPassByEmailRsp(): Observable<ForgetPassByEmailRspEvent> {
        return publisher.ofType(ForgetPassByEmailRspEvent::class.java)
    }

    data class GetAliasByCidRspEvent(var i: Int, var s: String)

    override fun OnGetAliasByCidRsp(i: Int, s: String) {
        AppLogger.w("OnGetAliasByCidRsp :$i:$s")
        publisher.onNext(GetAliasByCidRspEvent(i, s))
    }

    @JvmStatic
    fun observeGetAliasByCidRsp(): Observable<GetAliasByCidRspEvent> {
        return publisher.ofType(GetAliasByCidRspEvent::class.java)
    }

    data class GetFeedbackRspEvent(var ret: Int, var arrayList: ArrayList<JFGFeedbackInfo>)

    override fun OnGetFeedbackRsp(ret: Int, arrayList: ArrayList<JFGFeedbackInfo>) {
        AppLogger.w("OnGetFeedbackRsp :" + ListUtils.getSize(arrayList))
    }

    @JvmStatic
    fun observeGetFeedbackRsp(): Observable<GetFeedbackRspEvent> {
        return publisher.ofType(GetFeedbackRspEvent::class.java)
    }

    data class NotifyStorageTypeEvent(var i: Int)

    override fun OnNotifyStorageType(i: Int) {
        AppLogger.w("OnNotifyStorageType:" + i)
        publisher.onNext(NotifyStorageTypeEvent(i))
    }

    @JvmStatic
    fun observeNotifyStorageType(): Observable<NotifyStorageTypeEvent> {
        return publisher.ofType(NotifyStorageTypeEvent::class.java)
    }

    data class BindDevRspEvent(var i: Int, var s: String, var s1: String)

    override fun OnBindDevRsp(i: Int, s: String, s1: String) {
        AppLogger.w("onBindDev: $i uuid:$s,reason:$s1")
        publisher.onNext(BindDevRspEvent(i, s, s1))
    }

    @JvmStatic
    fun observeBindDevRsp(): Observable<BindDevRspEvent> {
        return publisher.ofType(BindDevRspEvent::class.java)
    }

    data class UnbindDeviceRspEvent(var i: Int, var s: String)

    override fun OnUnBindDevRsp(i: Int, s: String) {
        AppLogger.w(String.format(Locale.getDefault(), "OnUnBindDevRsp:%d,%s", i, s))
        publisher.onNext(UnbindDeviceRspEvent(i, s))
    }

    @JvmStatic
    fun observeUnbindDeviceRsp(): Observable<UnbindDeviceRspEvent> {
        return publisher.ofType(UnbindDeviceRspEvent::class.java)
    }

    data class GetVideoShareUrlEvent(var s: String)

    override fun OnGetVideoShareUrl(s: String) {
        AppLogger.w(String.format(Locale.getDefault(), "OnGetVideoShareUrl:%s", s))
        publisher.onNext(GetVideoShareUrlEvent(s))
    }

    @JvmStatic
    fun observeGetVideoShareUrl(): Observable<GetVideoShareUrlEvent> {
        return publisher.ofType(GetVideoShareUrlEvent::class.java)
    }

    data class ForwardDataEvent(var bytes: ByteArray)

    override fun OnForwardData(bytes: ByteArray) {
        publisher.onNext(ForwardDataEvent(bytes))
        val midHeader = DpUtils.unpackDataWithoutThrow(bytes, MIDHeader::class.java, null)
        if (midHeader == null) {
            AppLogger.w("解析透传消息失败:" + DpUtils.unpack(bytes)!!)
            return
        }
        midHeader.rawBytes = bytes
        when (midHeader.msgId) {
            20006 -> {
                val rawRspMsg = DpUtils.unpackDataWithoutThrow(bytes, PanoramaEvent.MsgForward::class.java, null) ?: return

                RxBus.getCacheInstance().post(rawRspMsg)
                BaseForwardHelper.getInstance().dispatcherForward<Any>(rawRspMsg)
            }
            else -> {
                RxBus.getCacheInstance().post(midHeader)
            }
        }
    }

    @JvmStatic
    fun observeForwardData(): Observable<ForwardDataEvent> {
        return publisher.ofType(ForwardDataEvent::class.java)
    }

    data class MultiShareDevicesEvent(var i: Int, var s: String, var s1: String)

    override fun OnMultiShareDevices(i: Int, s: String, s1: String) {
        AppLogger.w(String.format(Locale.getDefault(), "check OnMultiShareDevices:%d,%s,%s", i, s, s1))
        publisher.onNext(MultiShareDevicesEvent(i, s, s1))
    }

    @JvmStatic
    fun observeMultiShareDevices(): Observable<MultiShareDevicesEvent> {
        return publisher.ofType(MultiShareDevicesEvent::class.java)
    }

    data class CheckClientVersionEvent(var i: Int, var s: String, var i1: Int)

    override fun OnCheckClientVersion(i: Int, s: String, i1: Int) {
        publisher.onNext(CheckClientVersionEvent(i, s, i1))
    }

    @JvmStatic
    fun observeCheckClientVersion(): Observable<CheckClientVersionEvent> {
        return publisher.ofType(CheckClientVersionEvent::class.java)
    }

    data class RobotCountMultiDataRspEvent(var l: Long, var o: Any)

    override fun OnRobotCountMultiDataRsp(l: Long, o: Any) {
        AppLogger.w("OnRobotCountMultiDataRsp:" + o.toString())
        publisher.onNext(RobotCountMultiDataRspEvent(l, o))
    }

    @JvmStatic
    fun observeRobotCountMultiDataRsp(): Observable<RobotCountMultiDataRspEvent> {
        return publisher.ofType(RobotCountMultiDataRspEvent::class.java)
    }

    data class RobotGetMultiDataRspEvent(var l: Long, var o: Any)

    override fun OnRobotGetMultiDataRsp(l: Long, o: Any) {
        AppLogger.w("OnRobotGetMultiDataRsp:$l:$o")
        publisher.onNext(RobotGetMultiDataRspEvent(l, o))
    }

    @JvmStatic
    fun observeRobotGetMultiDataRsp(): Observable<RobotGetMultiDataRspEvent> {
        return publisher.ofType(RobotGetMultiDataRspEvent::class.java)
    }

    data class GetAdPolicyRspEvent(var i: Int, var l: Long, var picUrl: String, var tarUrl: String)

    override fun OnGetAdPolicyRsp(i: Int, l: Long, picUrl: String, tagUrl: String) {
        AppLogger.w("OnGetAdPolicyRsp:$l:$picUrl")
        publisher.onNext(GetAdPolicyRspEvent(i, l, picUrl, tagUrl))
    }

    @JvmStatic
    fun observeGetAdPolicyRsp(): Observable<GetAdPolicyRspEvent> {
        return publisher.ofType(GetAdPolicyRspEvent::class.java)
    }

    data class CheckDevVersionRspEvent(var b: Boolean, var url: String, var targetVersion: String, var tip: String, var md5: String, var cid: String)

    override fun OnCheckDevVersionRsp(b: Boolean, url: String, tagVersion: String,
                                      tip: String, md5: String, cid: String) {
        AppLogger.w("OnCheckDevVersionRsp :" + b + ":" + url + ":" + tagVersion
                + ":" + tip + ":" + md5 + "," + cid)
        publisher.onNext(CheckDevVersionRspEvent(b, url, tagVersion, tip, md5, cid))
    }

    @JvmStatic
    fun observeCheckDevVersionRsp(): Observable<CheckDevVersionRspEvent> {
        return publisher.ofType(CheckDevVersionRspEvent::class.java)
    }

    data class CheckTagDeviceVersionRspEvent(var ret: Int, var cid: String, var targetVersion: String, var content: String, var arrayList: ArrayList<DevUpgradeInfo>)

    override fun OnCheckTagDeviceVersionRsp(ret: Int, cid: String, tagVersion: String, content: String, arrayList: ArrayList<DevUpgradeInfo>) {
        AppLogger.w("OnCheckTagDeviceVersionRsp:" + ret + ":" + cid + ",:" + tagVersion + "," + Gson().toJson(arrayList))
        publisher.onNext(CheckTagDeviceVersionRspEvent(ret, cid, tagVersion, content, arrayList))
    }

    @JvmStatic
    fun observeCheckTagDeviceVersionRsp(): Observable<CheckTagDeviceVersionRspEvent> {
        return publisher.ofType(CheckTagDeviceVersionRspEvent::class.java)
    }

    data class UniversalDataRspEvent(var l: Long, var i: Int, var bytes: ByteArray)

    override fun OnUniversalDataRsp(l: Long, i: Int, bytes: ByteArray) {
        publisher.onNext(UniversalDataRspEvent(l, i, bytes))
    }

    @JvmStatic
    fun observeUniversalDataRsp(): Observable<UniversalDataRspEvent> {
        return publisher.ofType(UniversalDataRspEvent::class.java)
    }


    fun addHooker() {

    }

    fun removeHooker() {

    }
}