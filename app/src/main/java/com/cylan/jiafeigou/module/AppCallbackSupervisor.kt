package com.cylan.jiafeigou.module

import android.text.TextUtils
import android.util.Log
import com.cylan.entity.jniCall.*
import com.cylan.jfgapp.interfases.AppCallBack
import com.cylan.jiafeigou.base.module.*
import com.cylan.jiafeigou.dp.DpUtils
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.misc.ver.AbstractVersion
import com.cylan.jiafeigou.module.message.MIDHeader
import com.cylan.jiafeigou.rx.RxBus
import com.cylan.jiafeigou.rx.RxEvent
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ListUtils
import com.cylan.jiafeigou.utils.PreferencesUtils
import com.google.gson.Gson
import rx.Observable
import rx.subjects.PublishSubject
import java.util.*
import kotlin.collections.ArrayList

@Suppress("unused", "ArrayInDataClass", "UNCHECKED_CAST")
/**
 * Created by yzd on 17-12-3.
 */

object AppCallbackSupervisor : AppCallBack, Supervisor {
    private val TAG = AppCallbackSupervisor::class.java.simpleName
    private val publisher = PublishSubject.create<Any>().toSerialized()
    private fun publish(event: Any) {
        Log.d(TAG, "publish a event:$event")
        HookerSupervisor.performHooker(PublishAction(event))
    }

    fun <T> observe(eventType: Class<T>): Observable<T> {
        Log.d(TAG, "observe a event:$eventType")
        return (HookerSupervisor.performHooker(ObserverAction(eventType)) as? ObserverParameter<T>)?.observable ?: Observable.empty()
    }

    abstract class PublishHooker : Supervisor.Hooker {
        override fun parameterType(): Array<Class<*>> = arrayOf(PublishParameter::class.java)

        override fun hooker(action: Supervisor.Action, parameter: Any) {
            when (parameter) {
                is PublishParameter -> doPublishHooker(action, parameter)
                else -> action.process()
            }
        }

        open protected fun doPublishHooker(action: Supervisor.Action, parameter: PublishParameter) = action.process()

    }

    class PublishParameter(var event: Any)

    class PublishAction<out T : Any>(val event: T) : Supervisor.Action {
        private val parameter = PublishParameter(event)
        override fun parameter() = parameter

        override fun process(): Any? {
            publisher.onNext(event)
            return parameter
        }
    }

    abstract class ObserverHooker : Supervisor.Hooker {
        override fun parameterType(): Array<Class<*>> = arrayOf(ObserverParameter::class.java)

        override fun hooker(action: Supervisor.Action, parameter: Any) {
            when (parameter) {
                is ObserverParameter<*> -> doObserverHooker(action, parameter)
                else -> action.process()
            }
        }

        open protected fun doObserverHooker(action: Supervisor.Action, parameter: ObserverParameter<*>) = action.process()

    }

    class ObserverParameter<T>(val clazz: Class<T>, var observable: Observable<T> = Observable.empty())


    class ObserverAction<T>(var clazz: Class<T>) : Supervisor.Action {
        private val parameter = ObserverParameter(clazz, publisher.ofType(clazz))
        override fun parameter() = parameter

        override fun process(): Any? {
            return parameter
        }
    }

    /**
     * 不要在这个类里做复杂的逻辑处理,所有的消息都应该以 RxBus 发送出去,在对应的地方再做处理
     */
    private val gson = Gson()

    override fun OnLocalMessage(s: String, i: Int, bytes: ByteArray) {
        //        AppLogger.d("OnLocalMessage :" + account + ",i:" + i);
        val localUdpMsg = RxEvent.LocalUdpMsg(System.currentTimeMillis(), s, i.toShort(), bytes)
        BaseUdpMsgParser.getInstance().parserUdpMessage(localUdpMsg)
        RxBus.getCacheInstance().post(localUdpMsg)
    }


    override fun OnReportJfgDevices(jfgDevices: Array<JFGDevice>) {
        AppLogger.w("OnReportJfgDevices" + gson.toJson(jfgDevices))
//        RxBus.getCacheInstance().post(RxEvent.SerializeCacheDeviceEvent(jfgDevices))
        publish(jfgDevices)
    }

    override fun OnUpdateAccount(jfgAccount: JFGAccount) {
        AppLogger.w("OnUpdateAccount :" + gson.toJson(jfgAccount))
        RxBus.getCacheInstance().post(RxEvent.SerializeCacheAccountEvent(jfgAccount))
    }

    override fun OnUpdateHistoryVideoList(jfgHistoryVideo: JFGHistoryVideo) {
        AppLogger.w("OnUpdateHistoryVideoList :" + jfgHistoryVideo.list.size)
        DataSourceManager.getInstance().cacheHistoryDataList(jfgHistoryVideo)
    }

    override fun OnUpdateHistoryVideoV2(bytes: ByteArray) {
        DataSourceManager.getInstance().cacheHistoryDataList(bytes)
    }

    override fun OnUpdateHistoryErrorCode(jfgHistoryVideoErrorInfo: JFGHistoryVideoErrorInfo) {
        AppLogger.w("OnUpdateHistoryErrorCode :" + gson.toJson(jfgHistoryVideoErrorInfo))
        RxBus.getCacheInstance().post(jfgHistoryVideoErrorInfo)
    }

    override fun OnServerConfig(jfgServerCfg: JFGServerCfg) {
        AppLogger.w("OnServerConfig :" + gson.toJson(jfgServerCfg))
        RxBus.getCacheInstance().post(jfgServerCfg)
    }

    override fun OnLogoutByServer(i: Int) {
        AppLogger.w("OnLogoutByServer:" + i)
        LoginHelper.performLogout()
        RxBus.getCacheInstance().post(RxEvent.PwdHasResetEvent(i))
    }

    override fun OnVideoDisconnect(jfgMsgVideoDisconn: JFGMsgVideoDisconn) {
        AppLogger.w("OnVideoDisconnect :" + gson.toJson(jfgMsgVideoDisconn))
        RxBus.getCacheInstance().post(jfgMsgVideoDisconn)
    }

    override fun OnVideoNotifyResolution(jfgMsgVideoResolution: JFGMsgVideoResolution) {
        AppLogger.w("OnVideoNotifyResolution" + jfgMsgVideoResolution.peer)
        Command.videoWidth = jfgMsgVideoResolution.width
        Command.videoHeight = jfgMsgVideoResolution.height
        RxBus.getCacheInstance().post(jfgMsgVideoResolution)
    }

    override fun OnVideoNotifyRTCP(jfgMsgVideoRtcp: JFGMsgVideoRtcp) {
        Log.w("", "OnVideoNotifyRTCP" + gson.toJson(jfgMsgVideoRtcp))
        RxBus.getCacheInstance().post(jfgMsgVideoRtcp)
    }

    override fun OnHttpDone(jfgMsgHttpResult: JFGMsgHttpResult) {
        AppLogger.w("OnHttpDone :" + gson.toJson(jfgMsgHttpResult))
        RxBus.getCacheInstance().post(jfgMsgHttpResult)
    }

    override fun OnRobotTransmitMsg(robotMsg: RobotMsg) {
        AppLogger.w("OnRobotTransmitMsg :" + gson.toJson(robotMsg))
        RxBus.getCacheInstance().post(robotMsg)
    }

    override fun OnRobotMsgAck(i: Int) {
        AppLogger.w("OnRobotMsgAck :" + i)
    }

    override fun OnRobotGetDataRsp(robotoGetDataRsp: RobotoGetDataRsp) {
        AppLogger.w("OnRobotGetDataRsp :" + gson.toJson(robotoGetDataRsp))
        RxBus.getCacheInstance().post(robotoGetDataRsp)
        publish(robotoGetDataRsp)
    }

    override fun OnRobotGetDataExRsp(l: Long, s: String, arrayList: ArrayList<JFGDPMsg>) {
        val robotoGetDataRsp = RobotoGetDataRsp()
        robotoGetDataRsp.identity = s
        robotoGetDataRsp.seq = l
        robotoGetDataRsp.put(-1, arrayList)//key在这种情况下无用
//        RxBus.getCacheInstance().post(RxEvent.SerializeCacheGetDataEvent(robotoGetDataRsp))
        publish(robotoGetDataRsp)
        AppLogger.w("OnRobotGetDataExRsp :" + s + "," + gson.toJson(arrayList))
    }

    override fun OnRobotSetDataRsp(l: Long, uuid: String, arrayList: ArrayList<JFGDPMsgRet>) {
        AppLogger.w("OnRobotSetDataRsp :" + l + gson.toJson(arrayList))
        RxBus.getCacheInstance().post(RxEvent.SetDataRsp(l, uuid, arrayList))
    }

    override fun OnRobotGetDataTimeout(l: Long, s: String) {
        AppLogger.w("OnRobotGetDataTimeout :$l:$s")
    }

    override fun OnQuerySavedDatapoint(s: String, arrayList: ArrayList<JFGDPMsg>): ArrayList<JFGDPMsg>? {
        AppLogger.w("这是一个bug")
        return null
    }

    override fun OnlineStatus(b: Boolean) {
        if (b != DataSourceManager.getInstance().isOnline) {
            AppLogger.w("OnlineStatus :" + b)
            RxBus.getCacheInstance().post(RxEvent.OnlineStatusRsp(b))
            DataSourceManager.getInstance().isOnline = b//设置用户在线信息
        }
    }

    override fun OnResult(jfgResult: JFGResult) {
        RxBus.getCacheInstance().post(jfgResult)
        BaseJFGResultParser.getInstance().parserResult(jfgResult)
        AppLogger.w("jfgResult [" + jfgResult.event + ":" + jfgResult.code + "]")
    }

    override fun OnDoorBellCall(jfgDoorBellCaller: JFGDoorBellCaller) {
        AppLogger.w("OnDoorBellCall :" + gson.toJson(jfgDoorBellCaller))
        publish(jfgDoorBellCaller)
    }

    override fun OnOtherClientAnswerCall(s: String) {
        AppLogger.w("OnOtherClientAnswerCall:" + s)
        RxBus.getCacheInstance().post(RxEvent.CallResponse(false))
    }

    override fun OnRobotCountDataRsp(l: Long, s: String, arrayList: ArrayList<JFGDPMsgCount>) {
        AppLogger.w("OnRobotCountDataRsp :$l:$s")
    }

    override fun OnRobotDelDataRsp(l: Long, s: String, i: Int) {
        AppLogger.w("OnRobotDelDataRsp :$l uuid:$s i:$i")
        RxBus.getCacheInstance().post(RxEvent.DeleteDataRsp(l, s, i))
    }

    data class RobotSyncDataEvent(var fromDevice: Boolean, var uuid: String, var dpList: ArrayList<JFGDPMsg>)


    override fun OnRobotSyncData(b: Boolean, s: String, arrayList: ArrayList<JFGDPMsg>) {
        AppLogger.w("OnRobotSyncData :" + b + " " + s + " " + Gson().toJson(arrayList))
//        RxBus.getCacheInstance().post(RxEvent.SerializeCacheSyncDataEvent(b, s, arrayList))
        val dpIDs = java.util.ArrayList<Long>(arrayList.size)
        arrayList.forEach { dpIDs.add(it.id) }
        publish(RxEvent.DeviceSyncRsp(arrayList, dpIDs, s))
    }

    override fun OnSendSMSResult(i: Int, s: String) {
        AppLogger.w("OnSendSMSResult :$i,$s")
        //store the token .
        PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN, s)
        RxBus.getCacheInstance().post(RxEvent.SmsCodeResult(i, s))
    }

    override fun OnGetFriendListRsp(ret: Int, arrayList: ArrayList<JFGFriendAccount>) {
        AppLogger.w("OnLocalMessage :" + arrayList.size)
        //        arrayList = new ArrayList<>();
        //        for (int i = 0; i < 5; i++) {
        //            JFGFriendAccount req = new JFGFriendAccount();
        //            req.account = "333" + i + "_hunt";
        //            req.alias = "wth? " + i;
        //            req.markName = "zi lai?";
        //            arrayList.add(req);
        //        }
        DataSourceManager.getInstance().friendsList = arrayList
    }

    override fun OnGetFriendRequestListRsp(ret: Int, arrayList: ArrayList<JFGFriendRequest>) {
        AppLogger.w("OnGetFriendRequestListRsp:" + arrayList.size)
        //        AppLogger.d("测试专用");
        //        arrayList = new ArrayList<>();
        //        for (int i = 0; i < 5; i++) {
        //            JFGFriendRequest req = new JFGFriendRequest();
        //            req.account = "333" + i + "_hunt";
        //            req.alias = "wth? " + i;
        //            req.sayHi = " you son of...";
        //            req.time = System.currentTimeMillis() - RandomUtils.getRandom(30) * 3600 * 1000;
        //            arrayList.add(req);
        //        }
        DataSourceManager.getInstance().friendsReqList = arrayList
    }

    override fun OnGetFriendInfoRsp(i: Int, jfgFriendAccount: JFGFriendAccount) {
        AppLogger.w("OnLocalMessage :" + Gson().toJson(jfgFriendAccount))
        RxBus.getCacheInstance().post(RxEvent.GetFriendInfoCall(i, jfgFriendAccount))
    }

    override fun OnCheckFriendAccountRsp(i: Int, s: String, s1: String, b: Boolean) {
        AppLogger.w("OnCheckFriendAccountRsp :")
        RxBus.getCacheInstance().post(RxEvent.CheckAccountCallback(i, s, s1, b))
    }

    override fun OnShareDeviceRsp(i: Int, s: String, s1: String) {
        AppLogger.w("OnShareDeviceRsp :$i:$s:$s1")
        RxBus.getCacheInstance().post(RxEvent.ShareDeviceCallBack(i, s, s1))
    }

    override fun OnUnShareDeviceRsp(i: Int, s: String, s1: String) {
        AppLogger.w("OnUnShareDeviceRsp :$i,$s,$s1")
        RxBus.getCacheInstance().post(RxEvent.UnShareDeviceCallBack(i, s, s1))
    }

    override fun OnGetShareListRsp(i: Int, arrayList: ArrayList<JFGShareListInfo>) {
        AppLogger.w("OnGetShareListRsp :" + i)
        DataSourceManager.getInstance().cacheShareList(arrayList)
    }

    override fun OnGetUnShareListByCidRsp(i: Int, arrayList: ArrayList<JFGFriendAccount>) {
        AppLogger.w("UnShareListByCidEvent :")
        RxBus.getCacheInstance().post(RxEvent.UnShareListByCidEvent(i, arrayList))
    }

    override fun OnUpdateNTP(l: Int) {
        AppLogger.w("OnUpdateNTP :" + l)
        PreferencesUtils.putInt(JConstant.KEY_NTP_INTERVAL, (System.currentTimeMillis() / 1000 - l).toInt())
    }

    override fun OnForgetPassByEmailRsp(i: Int, s: String) {
        AppLogger.w("OnForgetPassByEmailRsp :" + s)
        RxBus.getCacheInstance().post(RxEvent.ForgetPwdByMail(s).setRet(i))
    }

    override fun OnGetAliasByCidRsp(i: Int, s: String) {
        AppLogger.w("OnGetAliasByCidRsp :$i:$s")
    }

    override fun OnGetFeedbackRsp(ret: Int, arrayList: ArrayList<JFGFeedbackInfo>) {
        AppLogger.w("OnGetFeedbackRsp :" + ListUtils.getSize(arrayList))
        //        arrayList = new ArrayList<>();
        //        for (int i = 0; i < 5; i++) {
        //            JFGFeedbackInfo info = new JFGFeedbackInfo();
        //            info.msg = "dfafa" + i;
        //            info.time = System.currentTimeMillis() - RandomUtils.getRandom(20) * 3600;
        //            arrayList.add(info);
        //        }
        if (ListUtils.isEmpty(arrayList)) {
            return
        }
        FeedbackManager.getInstance().cachePush(arrayList)
        DataSourceManager.getInstance().handleSystemNotification(arrayList)
    }


    override fun OnNotifyStorageType(i: Int) {
        AppLogger.w("OnNotifyStorageType:" + i)
        //此event是全局使用,不需要删除.因为在DataSourceManager需要用到.
        DataSourceManager.getInstance().storageType = i
    }

    override fun OnBindDevRsp(i: Int, s: String, s1: String) {
        AppLogger.w("onBindDev: $i uuid:$s,reason:$s1")
        RxBus.getCacheInstance().post(RxEvent.BindDeviceEvent(i, s, s1))
        PreferencesUtils.putString(JConstant.BINDING_DEVICE, "")
    }

    override fun OnUnBindDevRsp(i: Int, s: String) {
        AppLogger.w(String.format(Locale.getDefault(), "OnUnBindDevRsp:%d,%s", i, s))
    }

    override fun OnGetVideoShareUrl(s: String) {
        AppLogger.w(String.format(Locale.getDefault(), "OnGetVideoShareUrl:%s", s))
        RxBus.getCacheInstance().post(RxEvent.GetVideoShareUrlEvent(s))
    }

    override fun OnForwardData(bytes: ByteArray) {
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

    override fun OnMultiShareDevices(i: Int, s: String, s1: String) {
        AppLogger.w(String.format(Locale.getDefault(), "check OnMultiShareDevices:%d,%s,%s", i, s, s1))
        RxBus.getCacheInstance().post(RxEvent.MultiShareDeviceEvent(i, s, s1))
    }

    override fun OnCheckClientVersion(i: Int, s: String, i1: Int) {
        RxBus.getCacheInstance().post(RxEvent.ClientCheckVersion(i, s, i1))
    }

    override fun OnRobotCountMultiDataRsp(l: Long, o: Any) {


        AppLogger.w("OnRobotCountMultiDataRsp:" + o.toString())
    }

    override fun OnRobotGetMultiDataRsp(l: Long, o: Any?) {
        AppLogger.w("OnRobotGetMultiDataRsp:$l:$o")
        if (o != null && o is HashMap<*, *>) {
            val rawMap = o as HashMap<String, HashMap<Long, Array<JFGDPValue>>>?
            val set = rawMap!!.keys
            val count = set.size
            for (uuid in set) {
                val rsp = RobotoGetDataRsp()
                rsp.identity = uuid
                rsp.seq = l
                rsp.map = HashMap()
                val map = rawMap[uuid]
                for (lll in map!!.keys) {
                    val msgList = ArrayList<JFGDPMsg>()
                    val msgId = lll.toInt()
                    for (j in map!![lll]!!) {
                        val msg = JFGDPMsg()
                        msg.id = msgId.toLong()
                        msg.version = j.version
                        msg.packValue = j.value
                        msgList.add(msg)
                    }
                    rsp.map.put(msgId, msgList)
                }
//                RxBus.getCacheInstance().post(RxEvent.SerializeCacheGetDataEvent(rsp))
                publish(rsp)
            }

            //            CacheHolderKt.saveProperty((Map<String, Map<Long, JFGDPValue[]>>) (Object) rawMap, HashStrategyFactory.RECORD_END_EVENT::select);
            Log.d("OnRobotGetMultiDataRsp", "size: " + count)
        }
    }


    override fun OnGetAdPolicyRsp(i: Int, l: Long, picUrl: String, tagUrl: String) {
        AppLogger.w("OnGetAdPolicyRsp:$l:$picUrl")
        //        l = System.currentTimeMillis() + 2 * 60 * 1000;
        //        tagUrl = "http://www.baidu.com";
        //        picUrl = "http://cdn.duitang.com/uploads/item/201208/19/20120819131358_2KR2S.thumb.600_0.png";
        RxBus.getCacheInstance().postSticky(RxEvent.AdsRsp().setPicUrl(picUrl).setTagUrl(tagUrl)
                .setRet(i).setTime(l))
    }

    //final boolean hasNew, final String url, final String version,
    // final String tip, final String md5, final String cid
    override fun OnCheckDevVersionRsp(b: Boolean, url: String, tagVersion: String,
                                      tip: String, md5: String, cid: String) {
        AppLogger.w("OnCheckDevVersionRsp :" + b + ":" + url + ":" + tagVersion
                + ":" + tip + ":" + md5 + "," + cid)
        //        isFriend = true;
        //        account = "http://yf.cylan.com.cn:82/Garfield/JFG2W/3.0.0/3.0.0.1000/201704261515/hi.bin";
        //        alias = "3.0.0";
        //        s2 = "你好";
        //        s3 = "xx";
        if (!b) {
            PreferencesUtils.remove(JConstant.KEY_FIRMWARE_CONTENT + cid)
        }
        val arrayList = ArrayList<DevUpgradeInfo>()
        val info = DevUpgradeInfo()
        info.md5 = md5
        info.tag = 0
        info.url = url
        info.version = tagVersion
        arrayList.add(info)
        val version = AbstractVersion.BinVersion()
        version.cid = cid
        version.content = tip
        version.list = arrayList
        version.tagVersion = tagVersion
        RxBus.getCacheInstance().post(RxEvent.VersionRsp().setUuid(cid).setVersion(version))
    }


    override fun OnCheckTagDeviceVersionRsp(ret: Int, cid: String,
                                            tagVersion: String,
                                            content: String,
                                            arrayList: ArrayList<DevUpgradeInfo>) {
        AppLogger.w("OnCheckTagDeviceVersionRsp:" + ret + ":" + cid + ",:" + tagVersion + "," + Gson().toJson(arrayList))
        //        arrayList = testList();
        //        cid = "290000000065";
        //        tagVersion = "1.0.0.009";
        //        content = "test";
        if (!TextUtils.isEmpty(cid)) {
            if (ret != 0 || ListUtils.isEmpty(arrayList)) {
                PreferencesUtils.remove(JConstant.KEY_FIRMWARE_CONTENT + cid)
            }
        } else {
            return
        }
        val version = AbstractVersion.BinVersion()
        version.setCid(cid)
        version.setContent(content)
        version.setList(arrayList)
        version.setTagVersion(tagVersion)
        RxBus.getCacheInstance().post(RxEvent.VersionRsp().setUuid(cid).setVersion(version))
    }

    override fun OnUniversalDataRsp(l: Long, i: Int, bytes: ByteArray) {
        //        try {
        //            Object value = CacheHolderKt.getObjectMapper().get().readValue(bytes, Object.class);
        //            Log.w(JConstant.CYLAN_TAG, "OnUniversalDataRsp:" + value);
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //        }

        RxBus.getCacheInstance().post(RxEvent.UniversalDataRsp(l, i, bytes))
    }
}