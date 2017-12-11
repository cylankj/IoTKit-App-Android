package com.example.yzd.helloworld

import com.cylan.entity.jniCall.*
import com.cylan.jfgapp.interfases.AppCallBack
import rx.Observable
import rx.subjects.PublishSubject
import java.util.*

/**
 * Created by yzd on 17-12-3.
 */
object AppCallbackSupervisor : AppCallBack {
    override fun OnRobotSetDataRsp(p0: Long, p1: String?, p2: ArrayList<JFGDPMsgRet>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnCheckTagDeviceVersionRsp(p0: Int, p1: String?, p2: String?, p3: String?, p4: ArrayList<DevUpgradeInfo>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnGetFriendListRsp(p0: Int, p1: ArrayList<JFGFriendAccount>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnCheckClientVersion(p0: Int, p1: String?, p2: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnResult(p0: JFGResult?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnServerConfig(p0: JFGServerCfg?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnRobotTransmitMsg(p0: RobotMsg?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnVideoNotifyRTCP(p0: JFGMsgVideoRtcp?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnGetFeedbackRsp(p0: Int, p1: ArrayList<JFGFeedbackInfo>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnBindDevRsp(p0: Int, p1: String?, p2: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnForwardData(p0: ByteArray?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnRobotGetMultiDataRsp(p0: Long, p1: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnlineStatus(p0: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnUpdateHistoryVideoV2(p0: ByteArray?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnHttpDone(p0: JFGMsgHttpResult?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnGetShareListRsp(p0: Int, p1: ArrayList<JFGShareListInfo>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnGetFriendRequestListRsp(p0: Int, p1: ArrayList<JFGFriendRequest>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnDoorBellCall(p0: JFGDoorBellCaller?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnUpdateHistoryErrorCode(p0: JFGHistoryVideoErrorInfo?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnRobotSyncData(p0: Boolean, p1: String?, p2: ArrayList<JFGDPMsg>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnUpdateHistoryVideoList(p0: JFGHistoryVideo?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnUpdateAccount(p0: JFGAccount?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnVideoNotifyResolution(p0: JFGMsgVideoResolution?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnUnBindDevRsp(p0: Int, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnGetAliasByCidRsp(p0: Int, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnNotifyStorageType(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnLocalMessage(p0: String?, p1: Int, p2: ByteArray?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnGetAdPolicyRsp(p0: Int, p1: Long, p2: String?, p3: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnUniversalDataRsp(p0: Long, p1: Int, p2: ByteArray?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnForgetPassByEmailRsp(p0: Int, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnCheckFriendAccountRsp(p0: Int, p1: String?, p2: String?, p3: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnGetVideoShareUrl(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnVideoDisconnect(p0: JFGMsgVideoDisconn?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnLogoutByServer(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnRobotGetDataRsp(p0: RobotoGetDataRsp?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnUpdateNTP(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnUnShareDeviceRsp(p0: Int, p1: String?, p2: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnRobotCountMultiDataRsp(p0: Long, p1: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnReportJfgDevices(p0: Array<out JFGDevice>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnCheckDevVersionRsp(p0: Boolean, p1: String?, p2: String?, p3: String?, p4: String?, p5: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnRobotMsgAck(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnSendSMSResult(p0: Int, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnQuerySavedDatapoint(p0: String?, p1: ArrayList<JFGDPMsg>?): ArrayList<JFGDPMsg> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnRobotDelDataRsp(p0: Long, p1: String?, p2: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnOtherClientAnswerCall(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnShareDeviceRsp(p0: Int, p1: String?, p2: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnGetFriendInfoRsp(p0: Int, p1: JFGFriendAccount?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnRobotCountDataRsp(p0: Long, p1: String?, p2: ArrayList<JFGDPMsgCount>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnMultiShareDevices(p0: Int, p1: String?, p2: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnGetUnShareListByCidRsp(p0: Int, p1: ArrayList<JFGFriendAccount>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnRobotGetDataExRsp(p0: Long, p1: String?, p2: ArrayList<JFGDPMsg>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnRobotGetDataTimeout(p0: Long, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @JvmStatic
    private val publisher = PublishSubject.create<Any>().toSerialized()

    @JvmStatic
    fun observeOnAccountUpdate(): Observable<String> {
        return publisher.ofType(String::class.java)
    }
}