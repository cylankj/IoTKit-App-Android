package com.cylan.jiafeigou.module

import android.Manifest
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.cylan.constants.JfgConstants
import com.cylan.entity.jniCall.JFGAccount
import com.cylan.entity.jniCall.JFGDPMsg
import com.cylan.entity.jniCall.JFGVideoRect
import com.cylan.entity.jniCall.RobotMsg
import com.cylan.ex.JfgException
import com.cylan.jfgapp.interfases.AppCallBack
import com.cylan.jfgapp.interfases.AppCmd
import com.cylan.jfgapp.interfases.CallBack
import com.cylan.jfgapp.jni.JfgAppCallBack
import com.cylan.jfgapp.jni.JfgAppJni
import com.cylan.jiafeigou.base.module.BaseAppCallBackHolder
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.OptionsImpl
import com.cylan.jiafeigou.support.Security
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.utils.ContextUtils
import com.cylan.utils.JfgNetUtils
import com.cylan.utils.JfgUtils
import permissions.dispatcher.PermissionUtils.hasSelfPermissions
import java.util.*

/**
 * Created by yanzhendong on 2017/11/30.
 * 这个类里面的所有方法都不能在主线程调用
 */
class Command : AppCmd {


    companion object {
        val TAG = Command::class.java.simpleName
        @JvmField
        val appcallback: BaseAppCallBackHolder = BaseAppCallBackHolder()
        var isOnTop = true
        @JvmField
        var videoWidth: Int = 0
        @JvmField
        var videoHeight: Int = 0
        private var command: Command? = null
        @JvmStatic
        private var isLogEnabled = false
        private var initSuccess: Boolean = false
        private val logPath = JConstant.WORKER_PATH
        private val publicKey = "-----BEGIN PUBLIC KEY-----\n" +
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC+buVKQrwIZsBdY67tg4AVCxlH\n" +
                "yux50lVbLy61sprqsA4GA8/+B/oZnTNrnMJc/3d/4uTNy9Tpc9+VK7Bb7JI4iuE+\n" +
                "tx3ufFfFsz722eqmySvRsy1zo5I5s9CJYOJCOr4Qclr0Sc68RS8nChXlCsGSne3r\n" +
                "gaiD63d8yeq8YDOYSwIDAQAB\n" +
                "-----END PUBLIC KEY-----"


        init {
            try {
                Log.d(TAG, "正在初始化 SDK ")
                System.loadLibrary("jfgsdk")
                JfgConstants.ADDR = OptionsImpl.getServer()
                ensureNativeParams()
            } catch (e: Exception) {
                e.printStackTrace()
                AppLogger.e(e)
            }
        }

        @JvmStatic
        private fun ensureNativeParams() {
            try {
                if (!initSuccess) {
                    synchronized(Command::class.java) {
                        if (!initSuccess) {
                            AppLogger.w("正在初始化 NativeParams")
                            JfgAppCallBack.getInstance().setCallBack(appcallback)
                            initSuccess = JfgAppJni.NativeInit(ContextUtils.getContext(), JfgAppCallBack.getInstance(), JConstant.ROOT_DIR, publicKey, Security.getVId(), Security.getVKey())
                            AppLogger.w("正在初始化 NativeParams 成功了!")
                        }
                    }
                }
                if (!isLogEnabled) {
                    val permissions = hasSelfPermissions(ContextUtils.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (permissions) {
                        synchronized(Command::class.java) {
                            if (!isLogEnabled) {
                                isLogEnabled = true
                                JfgAppJni.EnableLog(true, logPath)
                            }
                        }
                        AppLogger.permissionGranted = true
                    }
                }
            } catch (e: Exception) {
                initSuccess = false
                e.printStackTrace()
                AppLogger.e("初始化出现错误!!!" + e.message + "vid:" + Security.getVId() + ",vkey:" + Security.getVKey() + ",serverAddress:" + OptionsImpl.getServer() + ",logPath:" + logPath)
            }
        }

        @JvmStatic
        fun getInstance(): Command {
            if (command == null) {
                synchronized(Command::class.java) {
                    if (command == null) {
                        command = Command()
                    }
                }
            }
            return command!!
        }
    }


    override fun setCallBack(callBack: AppCallBack) {
        JfgAppCallBack.getInstance().setCallBack(callBack)
    }

    @Throws(JfgException::class)
    override fun initNativeParam(vid: String, vkey: String, serverAddress: String): Boolean {
        return initSuccess
    }

    @Throws(JfgException::class)
    override fun initNativeParam(vid: String, vkey: String, serverAddress: String, workerPath: String): Boolean {
        return initSuccess
    }

    override fun sendCheckCode(account: String, language: Int, type: Int): Int {
        ensureNativeParams()
        return when {
            initSuccess && account.isNotEmpty() -> {
                JfgAppJni.SendCheckCode(account, language, type)
            }
            else -> throw IllegalStateException("sendCheckCode error:init:$initSuccess,account:$account,language:$language,type:$type")
        }
    }

    private fun getSignatures(): String {
        ensureNativeParams()
        val signatures = JfgUtils.getSignatures(ContextUtils.getContext())
        return when {
            initSuccess && signatures.isNotEmpty() -> {
                val sb = StringBuilder(signatures)
                var i = 2
                while (i < sb.length) {
                    sb.insert(i, ":")
                    i += 3
                }
                sb.toString()
            }
            else -> throw RuntimeException("Not signed in this apk !")
        }
    }

    @Throws(JfgException::class)
    override fun login(language: Int, account: String, pwd: String): Int {
        ensureNativeParams()
        return when {
            initSuccess && pwd.isNotEmpty() -> {
                val signatures = this.getSignatures()
                val net = JfgNetUtils.getInstance(ContextUtils.getContext())
                JfgAppJni.UserLogin(account, pwd, net.jfgNetType, net.netName, language, "", signatures, false)
            }
            else -> throw JfgException("login error,init:$initSuccess,language:$language,account:$account,pwd:$pwd")
        }
    }

    @Throws(JfgException::class)
    override fun login(language: Int, account: String, pwd: String, pwdEncrypted: Boolean): Int {
        ensureNativeParams()
        return when {
            initSuccess && pwd.isNotEmpty() -> {
                val net = JfgNetUtils.getInstance(ContextUtils.getContext())
                val signatures = this.getSignatures()
                return JfgAppJni.UserLogin(account, pwd, net.jfgNetType, net.netName, language, "", signatures, pwdEncrypted)
            }
            else -> throw JfgException(" login error,init$initSuccess,language$language,account$account,pwd:$pwd,pwdEncrypted:$pwdEncrypted ")
        }
    }

    @Throws(JfgException::class)
    override fun openLogin(language: Int, openId: String, token: String, type: Int): Int {
        ensureNativeParams()
        return when {
            initSuccess && openId.isNotEmpty() && token.isNotEmpty() -> {
                val signatures = this.getSignatures()
                val net = JfgNetUtils.getInstance(ContextUtils.getContext())
                return JfgAppJni.OpenLogin(openId, token, net.jfgNetType, net.netName, language, "", signatures, type)
            }
            else -> throw JfgException("openLogin error:init:$initSuccess,language:$language,openId:$openId,token:$token,type:$type")
        }
    }

    override fun sendFeedback(time: Long, content: String, hasLog: Boolean): Int {
        ensureNativeParams()
        return when {
            initSuccess -> {
                JfgAppJni.SendFeedback(time, content, hasLog)
            }
            else -> throw IllegalStateException("sendFeedback error:init:$initSuccess,time:$time,content:$content,hasLog:$hasLog")
        }
    }

    override fun getSdkVersion(): String {
        ensureNativeParams()
        return when {
            initSuccess -> {
                val version = JfgAppJni.GetSdkVersion()
                JfgAppCallBack.getInstance().setSdkVersion(version)
                version
            }
            else -> throw IllegalStateException("getSdkVersion error:init:$initSuccess")
        }
    }

    @Throws(JfgException::class)
    override fun enableLog(enable: Boolean, logPath: String): Int {
        val permissions = hasSelfPermissions(ContextUtils.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return if (initSuccess && logPath.isNotEmpty() && permissions && enable) {
            JfgAppJni.EnableLog(true, logPath)
        } else {
            JfgAppJni.EnableLog(false, logPath)
        }
    }

    override fun logout(): Int {
        return when {
            initSuccess -> {
                JfgAppJni.UserLogout()
            }
            else -> throw IllegalStateException("logout error,init:$initSuccess")
        }
    }

    override fun releaseApi() {
        when {
            initSuccess -> JfgAppJni.Release()
        }
    }

    override fun refreshDevList(): Int {
        ensureNativeParams()
        return when {
            initSuccess -> JfgAppJni.RefreshDevList()
            else -> throw IllegalStateException("refreshDevList error,init:$initSuccess")
        }
    }

    @Throws(JfgException::class)
    override fun resetPassword(account: String, password: String, token: String): Int {
        ensureNativeParams()
        return when {
            initSuccess && account.isNotEmpty() && password.isNotEmpty() && token.isNotEmpty() -> {
                JfgAppJni.ResetPass(account, password, token)
            }
            else -> throw JfgException("resetPassword error:init:$initSuccess,account:$account,password:$password,token:$token")
        }
    }

    @Throws(JfgException::class)
    override fun register(language: Int, account: String, pwd: String, type: Int, token: String): Int {
        ensureNativeParams()
        return when {
            initSuccess && account.isNotEmpty() && pwd.isNotEmpty() -> {
                JfgAppJni.UserRegister(language, account, pwd, type, token)
            }
            else -> throw JfgException("register error:init$initSuccess,language:$language,account:$account,pwd:$pwd,type:$type,token:$token")
        }
    }

    override fun getAccount(): Int {
        ensureNativeParams()
        return when {
            initSuccess -> JfgAppJni.GetAccount()
            else -> throw IllegalStateException("getAccount error,init:$initSuccess")
        }
    }

    @Throws(JfgException::class)
    override fun setAccount(account: JFGAccount?): Int {
        ensureNativeParams()
        return when {
            initSuccess && account != null -> {
                JfgAppJni.SetAccount(account)
            }
            else -> throw JfgException("setAccount error:init:$initSuccess,account:$account")
        }
    }

    override fun screenshot(local: Boolean, callBack: CallBack<Bitmap>) {
        ensureNativeParams()
        when {
            initSuccess -> {
                val data = JfgAppJni.TakeSnapShot(local)
                if (data?.isNotEmpty() == true) {
                    callBack.onSucceed(JfgUtils.byte2bitmap(videoWidth, videoHeight, data))
                } else {
                    callBack.onFailure("screenshot failure!")
                }
            }
        }
    }

    override fun screenshot(local: Boolean): ByteArray? {
        ensureNativeParams()
        return when {
            initSuccess -> JfgAppJni.TakeSnapShot(local)
            else -> null
        }
    }

    override fun setAudio(local: Boolean, mic: Boolean, speaker: Boolean): Int {
        ensureNativeParams()
        if (initSuccess) {
            return JfgAppJni.SetAudio(local, mic, speaker)
        } else {
            throw IllegalStateException("setAudio error,init:$initSuccess,local:$local,mic:$mic,speaker:$speaker")
        }
    }

    @Throws(JfgException::class)
    override fun playVideo(cid: String): Int {
        ensureNativeParams()
        if (initSuccess) {
            return this.playVideo(cid, "")
        } else {
            throw JfgException("playVideo error,init:$initSuccess,cid:$cid")
        }
    }

    @Throws(JfgException::class)
    override fun playVideo(cid: String, constraint: String): Int {
        ensureNativeParams()
        return when {
            initSuccess && !TextUtils.isEmpty(cid) -> {
                JfgAppJni.PlayVideo(cid, constraint)
            }
            else -> throw JfgException("playVideo error,init:$initSuccess, cid:$cid,constraint:$constraint")
        }
    }

    @Throws(JfgException::class)
    override fun changePassword(account: String, oldPwd: String, newPwd: String): Int {
        ensureNativeParams()
        return if (initSuccess && !TextUtils.isEmpty(account) && !TextUtils.isEmpty(oldPwd) && !TextUtils.isEmpty(newPwd)) {
            JfgAppJni.ChangePass(account, oldPwd, newPwd)
        } else {
            throw JfgException("changePassword error:init:$initSuccess,account:$account,oldPwd:$oldPwd,newPwd:$newPwd")
        }
    }

    override fun switchVideoMode(realTime: Boolean, time: Long): Int {
        ensureNativeParams()
        return if (initSuccess) {
            JfgAppJni.SwitchVideoMode(realTime, time)
        } else {
            throw  IllegalStateException("switchVideoMode error,init:$initSuccess,realTime:$realTime,time:$time")
        }
    }

    @Throws(JfgException::class)
    override fun stopPlay(uuid: String): Int {
        ensureNativeParams()
        return if (initSuccess && !TextUtils.isEmpty(uuid)) {
            JfgAppJni.DisconnectPeer(uuid)
        } else {
            throw JfgException("stopPlay error:init:$initSuccess,uuid:$uuid")
        }
    }

    @Throws(JfgException::class)
    override fun getVideoList(cid: String): Int {
        ensureNativeParams()
        return if (initSuccess && !TextUtils.isEmpty(cid)) {
            JfgAppJni.GetVideoList(cid)
        } else {
            throw JfgException("getVideoList eror:init:$initSuccess,cid:$cid")
        }
    }

    @Throws(JfgException::class)
    override fun bindDevice(cid: String, code: String, mac: String, rebind: Int): Int {
        ensureNativeParams()
        return if (initSuccess && !TextUtils.isEmpty(cid) && !TextUtils.isEmpty(code) && !TextUtils.isEmpty(mac)) {
            JfgAppJni.BindDev(cid, code, mac, rebind)
        } else {
            throw JfgException("bindDevice error,init:$initSuccess,cid:$cid,code:$code,mac:$mac,rebind:$rebind")
        }
    }

    @Throws(JfgException::class)
    override fun enableRenderLocalView(enable: Boolean, view: View?): Int {
        ensureNativeParams()
        return if (initSuccess && view != null) {
            JfgAppJni.EnableRenderLocalView(enable, view)
        } else {
            throw JfgException("enableRenderLocalView error:init:$initSuccess,enable:$enable,view:$view")
        }
    }

    @Throws(JfgException::class)
    override fun enableRenderMultiRemoteView(enable: Boolean, ssrc: Int, view: View?, rect: JFGVideoRect): Int {
        ensureNativeParams()
        return if (initSuccess && view != null) {
            JfgAppJni.EnableRenderMultiRemoteView(enable, ssrc, view, rect)
        } else {
            throw JfgException("enableRenderMultiRemoteView error:init:$initSuccess,ssrc:$ssrc,view:$view,rect:$rect")
        }
    }

    @Throws(JfgException::class)
    override fun enableRenderSingleRemoteView(enable: Boolean, view: View?): Int {
        ensureNativeParams()
        return if (initSuccess && view != null) {
            JfgAppJni.EnableRenderSingleRemoteView(enable, view)
        } else {
            throw JfgException("enableRenderSingleRemoteView error:init:$initSuccess,enable:$enable,view:$view")
        }
    }

    @Throws(JfgException::class)
    override fun playHistoryVideo(cid: String, time: Long): Int {
        ensureNativeParams()
        return if (initSuccess && !TextUtils.isEmpty(cid)) {
            JfgAppJni.PlayHistoryVideo(cid, time)
        } else {
            throw JfgException("playHistoryVideo error:init:$initSuccess,cid:$cid,time:$time")
        }
    }

    @Throws(JfgException::class)
    override fun sendLocalMessage(ip: String, port: Short, data: ByteArray): Int {
        ensureNativeParams()
        return if (initSuccess && !TextUtils.isEmpty(ip)) {
            JfgAppJni.SendLocalMessage(ip, port, data)
        } else {
            throw JfgException("sendLocalMessage error:init:$initSuccess,ip:$ip,port:$port,data:$data")
        }
    }

    @Throws(JfgException::class)
    override fun robotTransmitMsg(msg: RobotMsg?): Int {
        ensureNativeParams()
        return if (initSuccess && msg != null) {
            JfgAppJni.RobotTransmitMsg(msg)
        } else {
            throw JfgException("robotTransmitMsg error:init$initSuccess,msg:$msg")
        }
    }

    @Throws(JfgException::class)
    override fun robotSetData(peer: String?, dps: ArrayList<JFGDPMsg>): Long {
        ensureNativeParams()
        return if (initSuccess && peer != null) {
            JfgAppJni.RobotSetData(peer, dps)
        } else {
            throw JfgException("robotSetData error:init:$initSuccess,peer:$peer,dps:$dps")
        }
    }

    @Throws(JfgException::class)
    override fun robotGetData(peer: String?, queryDps: ArrayList<JFGDPMsg>, limit: Int, asc: Boolean, timeoutMs: Int): Long {
        ensureNativeParams()
        return if (initSuccess && peer != null) {
            JfgAppJni.RobotGetData(peer, queryDps, limit, asc, timeoutMs)
        } else {
            throw JfgException("robotGetData error:init:$initSuccess,peer:$peer,queryDPs:$queryDps,limit:$limit,asc:$asc,timeoutMs:$timeoutMs")
        }
    }

    @Throws(JfgException::class)
    override fun robotDelData(peer: String?, dps: ArrayList<JFGDPMsg>, timeoutMs: Int): Long {
        ensureNativeParams()
        return if (initSuccess && peer != null) {
            JfgAppJni.RobotDelData(peer, dps, timeoutMs)
        } else {
            throw JfgException("robotDelData error:init:$initSuccess,peer:$peer,dps:$dps,timeoutMs:$timeoutMs")
        }
    }

    @Throws(JfgException::class)
    override fun putFileToCloud(remoteUrl: String, localFilePath: String): Int {
        ensureNativeParams()
        return if (initSuccess && !TextUtils.isEmpty(remoteUrl) && !TextUtils.isEmpty(localFilePath)) {
            JfgAppJni.PutFileToCloud(remoteUrl, localFilePath)
        } else {
            throw JfgException("putFileToCloud error:init:$initSuccess,remoteUrl:$remoteUrl,localFilePath:$localFilePath")
        }
    }

    @Throws(JfgException::class)
    override fun unBindDevice(cid: String): Int {
        ensureNativeParams()
        return if (initSuccess && !TextUtils.isEmpty(cid)) {
            JfgAppJni.UnbindDev(cid)
        } else {
            throw JfgException("unBindDevice error:init:$initSuccess,cid:$cid")
        }
    }

    @Throws(JfgException::class)
    override fun robotCountData(peer: String?, dpIDs: LongArray?, timeoutMs: Int): Long {
        ensureNativeParams()
        return if (initSuccess && peer != null && dpIDs != null) {
            JfgAppJni.RobotCountData(peer, dpIDs, timeoutMs)
        } else {
            throw JfgException("robotCountData error:init:$initSuccess,peer:$peer,dpIDs:$dpIDs,timeoutMs:$timeoutMs")
        }
    }

    @Throws(JfgException::class)
    override fun robotCountDataClear(peer: String?, dpIDs: LongArray?, timeoutMs: Int): Long {
        ensureNativeParams()
        return if (initSuccess && peer != null && dpIDs != null) {
            JfgAppJni.RobotCountDataClear(peer, dpIDs, timeoutMs)
        } else {
            throw JfgException("robotCountDataClear error:init:$initSuccess,peer:$peer,dpIDs:$dpIDs,timeoutMs:$timeoutMs")
        }

    }

    @Throws(JfgException::class)
    override fun robotGetDataEx(peer: String?, asc: Boolean, version: Long, dpIDs: LongArray?, timeoutMs: Int): Long {
        ensureNativeParams()
        return if (initSuccess && peer != null && dpIDs?.isNotEmpty() == true) {
            JfgAppJni.RobotGetDataEx(peer, asc, version, dpIDs, timeoutMs)
        } else {
            throw JfgException("robotGetDataEx error:init:$initSuccess,peer:$peer,asc:$asc,version:$version,dpIDs:$dpIDs,timeoutMS:$timeoutMs")
        }
    }

    override fun reportEnvChange(type: Int) {
        ensureNativeParams()
        if (initSuccess) {
            when (type) {
                0 -> if (!isOnTop) {
                    isOnTop = true
                    JfgAppJni.ReportEnvChange(type)
                }
                1 -> {
                    isOnTop = false
                    JfgAppJni.ReportEnvChange(type)
                }
                else -> JfgAppJni.ReportEnvChange(type)
            }
        }
    }

    @Throws(JfgException::class)
    override fun getSignedCloudUrl(regionType: Int, url: String?): String {
        ensureNativeParams()
        return if (initSuccess && !TextUtils.isEmpty(url)) {
            JfgAppJni.GetSignedCloudUrl(regionType, url)
        } else {
            throw JfgException("getSignedCloudUrl error:init:$initSuccess,url:$url")
        }
    }

    @Throws(JfgException::class)
    override fun verifySMS(account: String, sms: String, token: String): Int {
        ensureNativeParams()
        return when {
            initSuccess && !TextUtils.isEmpty(account) && !TextUtils.isEmpty(sms) && !TextUtils.isEmpty(token) -> {
                JfgAppJni.VerifySMS(account, sms, token)
            }
            else -> {
                throw JfgException("verifySMS,init:$initSuccess,account:$account,sms:$sms,token:$token")
            }
        }
    }

    override fun getFriendList(): Int {
        ensureNativeParams()
        return if (initSuccess) {
            JfgAppJni.GetFriendList()
        } else {
            throw IllegalStateException("getFriendList error,init:$initSuccess")
        }
    }

    override fun getFriendRequestList(): Int {
        ensureNativeParams()
        return if (initSuccess) {
            JfgAppJni.GetFriendRequestList()
        } else {
            throw IllegalStateException("getFriendRequestList error:init:$initSuccess")
        }
    }

    @Throws(JfgException::class)
    override fun addFriend(targetAccount: String, sayHi: String): Int {
        ensureNativeParams()
        return if (initSuccess && !TextUtils.isEmpty(targetAccount)) {
            JfgAppJni.AddFriend(targetAccount, sayHi)
        } else {
            throw JfgException("addFriend error,init:$initSuccess,targetAccount:$targetAccount")
        }
    }

    @Throws(JfgException::class)
    override fun delFriend(targetAccount: String): Int {
        ensureNativeParams()
        return if (initSuccess && !TextUtils.isEmpty(targetAccount)) {
            JfgAppJni.DelFriend(targetAccount)
        } else {
            throw JfgException("delFriend error:init:$initSuccess,targetAccount:$targetAccount")
        }
    }

    @Throws(JfgException::class)
    override fun consentAddFriend(targetAccount: String): Int {
        ensureNativeParams()
        return if (initSuccess && !TextUtils.isEmpty(targetAccount)) {
            JfgAppJni.ConsentAddFriend(targetAccount)
        } else {
            throw JfgException("consentAddFriend error:init:$initSuccess,targetAccount:$targetAccount")
        }
    }

    @Throws(JfgException::class)
    override fun setFriendMarkName(targetAccount: String, markName: String): Int {
        ensureNativeParams()
        return if (initSuccess && !TextUtils.isEmpty(targetAccount) && !TextUtils.isEmpty(markName)) {
            JfgAppJni.SetFriendMarkName(targetAccount, markName)
        } else {
            throw JfgException("setFriendMarkName error:init:$initSuccess,targetAccount:$targetAccount,markName:$markName")
        }
    }

    @Throws(JfgException::class)
    override fun getFriendInfo(targetAccount: String): Int {
        ensureNativeParams()
        return when {
            initSuccess && !TextUtils.isEmpty(targetAccount) -> {
                JfgAppJni.GetFriendInfo(targetAccount)
            }
            else -> throw JfgException("getFriendInfo error,init:$initSuccess,targetAccount:$targetAccount")
        }
    }

    override fun getFeedbackList(): Int {
        ensureNativeParams()
        return when {
            initSuccess -> JfgAppJni.GetFeedbackList()
            else -> throw IllegalStateException("getFeedbackList error:init:$initSuccess")
        }
    }

    @Throws(JfgException::class)
    override fun checkFriendAccount(targetAccount: String): Int {
        ensureNativeParams()
        return when {
            initSuccess && !TextUtils.isEmpty(targetAccount) -> {
                JfgAppJni.CheckFriendAccount(targetAccount)
            }
            else -> throw JfgException("checkFriendAccount error:init:$initSuccess,targetAccount:$targetAccount")
        }
    }

    @Throws(JfgException::class)
    override fun shareDevice(cid: String, account: String): Int {
        ensureNativeParams()
        return when {
            initSuccess && !TextUtils.isEmpty(account) && !TextUtils.isEmpty(cid) -> {
                JfgAppJni.ShareDevice(cid, account)
            }
            else -> throw JfgException("shareDevice error:init:$initSuccess,cid:$cid,account:$account")
        }
    }

    @Throws(JfgException::class)
    override fun unShareDevice(cid: String?, account: String?): Int {
        ensureNativeParams()
        return when {
            initSuccess && !TextUtils.isEmpty(account) && !TextUtils.isEmpty(cid) -> {
                JfgAppJni.UnShareDevice(cid, account)
            }
            else -> throw JfgException("unShareDevice error:init:$initSuccess,cid:$cid,account:$account")
        }
    }

    override fun getShareList(cids: ArrayList<String>): Int {
        ensureNativeParams()
        return when {
            initSuccess -> JfgAppJni.GetShareList(cids)
            else -> throw IllegalStateException("getShareList error,init:$initSuccess,cids:$cids")
        }
    }

    @Throws(JfgException::class)
    override fun getUnShareListByCid(cid: String?): Int {
        ensureNativeParams()
        return when {
            initSuccess && cid?.isNotEmpty() == true -> {
                JfgAppJni.GetUnShareListByCid(cid)
            }
            else -> throw JfgException("getUnShareListByCid error:init:$initSuccess,cid:$cid")
        }
    }

    @Throws(JfgException::class)
    override fun getHash(account: String?, src: String?): String {
        ensureNativeParams()
        return when {
            initSuccess && account?.isNotEmpty() == true && src?.isNotEmpty() == true -> {
                JfgAppJni.GetHash(account, src)
            }
            else -> throw JfgException("getHash error:init:$initSuccess,account:$account,src:$src")
        }
    }

    @Throws(JfgException::class)
    override fun forgetPassByEmail(language: Int, account: String?): Int {
        ensureNativeParams()
        return when {
            initSuccess && account?.isNotEmpty() == true -> {
                JfgAppJni.ForgetPassByEmail(language, account)
            }
            else -> throw JfgException("forgetPassByEmail error:init:$initSuccess,language:$language,account:$account")
        }
    }

    @Throws(JfgException::class)
    override fun httpPostFile(reqPath: String?, filePath: String?): Int {
        ensureNativeParams()
        return when {
            initSuccess && reqPath?.isNotEmpty() == true && filePath?.isNotEmpty() == true -> {
                JfgAppJni.HttpPostFile(reqPath, filePath)
            }
            else -> throw JfgException("httpPostFile error:init:$initSuccess,reqPath:$reqPath,filePath:$filePath")
        }
    }

    @Throws(JfgException::class)
    override fun updateAccountPortrait(filePath: String?): Long {
        ensureNativeParams()
        return when {
            initSuccess && filePath?.isNotEmpty() == true -> {
                JfgAppJni.UpdateAccountPortrait(filePath)
            }
            else -> throw JfgException("updateAccountPortrait error:init:$initSuccess,filePath:$filePath")
        }
    }

    @Throws(JfgException::class)
    override fun setAliasByCid(cid: String, alias: String): Int {
        ensureNativeParams()
        return when {
            initSuccess && cid.isNotEmpty() && alias.isNotEmpty() -> {
                JfgAppJni.SetAliasByCid(cid, alias)
            }
            else -> throw JfgException("setAliasByCid error:init:$initSuccess,cid:$cid,alias:$alias")
        }
    }

    @Throws(JfgException::class)
    override fun setPwdWithBindAccount(pwd: String, type: Int, token: String): Int {
        ensureNativeParams()
        return when {
            initSuccess && pwd.isNotEmpty() -> {
                return JfgAppJni.SetPwdWithBindAccount(pwd, type, token)
            }
            else -> throw JfgException("setPwdWithBindAccount error:init:$initSuccess,pwd:$pwd,type:$type,token:$token")
        }
    }

    @Throws(JfgException::class)
    override fun checkAccountRegState(account: String): Int {
        ensureNativeParams()
        return when {
            initSuccess && account.isNotEmpty() -> {
                JfgAppJni.CheckAccountRegState(account)
            }
            else -> throw JfgException("checkAccountRegState error:init:$initSuccess,account:$account ")
        }
    }

    @Throws(JfgException::class)
    override fun delAddFriendMsg(account: String): Int {
        ensureNativeParams()
        return when {
            initSuccess && account.isNotEmpty() -> JfgAppJni.DelAddFriendMsg(account)
            else -> throw JfgException("delAddFriendMsg error:init$initSuccess,account:$account ")
        }
    }

    @Throws(JfgException::class)
    override fun robotGetDataByTime(peer: String, queryDps: ArrayList<JFGDPMsg>, timeoutMs: Int): Long {
        ensureNativeParams()
        return when {
            initSuccess -> JfgAppJni.RobotGetDataByTime(peer, queryDps, timeoutMs)
            else -> throw JfgException("checkDevVersion error:init$initSuccess,peer:$peer,queryDps:$queryDps,timeoutMs:$timeoutMs")
        }
    }

    @Throws(JfgException::class)
    override fun robotSetDataByTime(peer: String, dps: ArrayList<JFGDPMsg>): Long {
        ensureNativeParams()
        return when {
            initSuccess -> JfgAppJni.RobotSetDataByTime(peer, dps)
            else -> throw JfgException("robotSetDataByTime error:init$initSuccess,peer:$peer,dps:$dps")
        }
    }

    @Throws(JfgException::class)
    override fun checkDevVersion(pid: Int, cid: String, version: String): Long {
        ensureNativeParams()
        return when {
            initSuccess && cid.isNotEmpty() && version.isNotEmpty() -> {
                JfgAppJni.CheckDevVersion(pid, cid, version)
            }
            else -> throw JfgException("checkDevVersion error:init$initSuccess,pid:$pid,cid:$cid,version:$version")
        }
    }

    @Throws(JfgException::class)
    override fun getVideoShareUrl(fileName: String, content: String, ossType: Int, shareType: Int): Long {
        ensureNativeParams()
        return when {
            initSuccess -> JfgAppJni.GetVideoShareUrl(fileName, content, ossType, shareType)
            else -> throw JfgException("SendForwardData error:init:$initSuccess,fileName:$fileName,content:$content,ossType:$ossType,shareType:$shareType")
        }
    }

    override fun SendForwardData(data: ByteArray): Int {
        ensureNativeParams()
        return when {
            initSuccess -> JfgAppJni.SendForwardData(data)
            else -> throw JfgException("SendForwardData error:init:$initSuccess,data:$data")
        }
    }

    @Throws(JfgException::class)
    override fun multiShareDevices(devicds: Array<String>?, account: Array<String>?): Int {
        ensureNativeParams()
        return when {
            initSuccess && devicds?.isNotEmpty() == true && account?.isNotEmpty() == true -> {
                JfgAppJni.MultiShareDevices(devicds, account)
            }
            else -> throw JfgException("multiShareDevices error:init:$initSuccess,devicds:$devicds,account:$account")
        }
    }

    @Throws(JfgException::class)
    override fun checkClientVersion(vid: String): Int {
        ensureNativeParams()
        return when {
            initSuccess && vid.isNotEmpty() -> {
                JfgAppJni.CheckClientVersion(vid)
            }
            else -> throw JfgException("checkClientVersion error:init:$initSuccess,vid:$vid")
        }
    }

    @Throws(JfgException::class)
    override fun robotGetMultiData(map: HashMap<String, Array<JFGDPMsg>>?, limit: Int, ase: Boolean, timeoutMS: Int): Long {
        ensureNativeParams()
        return when {
            initSuccess && map?.isNotEmpty() == true -> {
                JfgAppJni.RobotGetMultiData(map, limit, ase, timeoutMS)
            }
            else -> throw JfgException("robotGetMultiData error:init:$initSuccess,map:$map,limit:$limit,ase:$ase,timeoutMS$timeoutMS")
        }
    }

    @Throws(JfgException::class)
    override fun robotCountMultiData(map: HashMap<String, LongArray>?, clear: Boolean, timeoutMS: Int): Long {
        ensureNativeParams()
        return when {
            initSuccess && map?.isNotEmpty() == true -> {
                JfgAppJni.RobotCountMultiData(map, clear, timeoutMS)
            }
            else -> throw JfgException("robotCountMultiData error,init$initSuccess,map:$map,clear:$clear,timeoutMS:$timeoutMS")
        }
    }

    override fun resetLog() {
        ensureNativeParams()
        when {
            initSuccess -> JfgAppJni.ResetLog()
        }
    }

    @Throws(JfgException::class)
    override fun countADClick(language: Int, version: String?, tagUrl: String?) {
        ensureNativeParams()
        return when {
            initSuccess && version?.isNotEmpty() == true && tagUrl?.isNotEmpty() == true -> {
                JfgAppJni.CountADClick(language, version, tagUrl)
            }
            else -> throw JfgException("countADClick error,init:$initSuccess,language:$language,version:$version,tagUrl:$tagUrl")
        }
    }

    @Throws(JfgException::class)
    override fun GetAdPolicy(language: Int, versionName: String?, resolution: String?): Int {
        ensureNativeParams()
        return when {
            initSuccess && versionName?.isNotEmpty() == true && resolution?.isNotEmpty() == true -> {
                JfgAppJni.GetAdPolicy(language, versionName, resolution)
            }
            else -> throw JfgException("GetAdPolicy error:init:$initSuccess,language:$language,versionName:$versionName,resolution:$resolution")
        }
    }

    @Throws(JfgException::class)
    override fun setPushToken(token: String?, bundleId: String, type: Int): Int {
        ensureNativeParams()
        return when {
            initSuccess && token?.isNotEmpty() == true -> JfgAppJni.SetPushToken(token, bundleId, type)
            else -> throw JfgException("setPushToken error:init:$initSuccess,token:$token,bundleId:$bundleId,type:$type")
        }
    }

    @Throws(JfgException::class)
    override fun CheckTagDeviceVersion(cid: String?): Int {
        ensureNativeParams()
        return when {
            initSuccess && cid?.isNotEmpty() == true -> JfgAppJni.CheckTagDeviceVersion(cid)
            else -> throw JfgException("CheckTagDeviceVersion error,init$initSuccess,cid:$cid")
        }
    }

    @Throws(JfgException::class)
    override fun sendUniservalDataSeq(mid: Int, data: ByteArray): Long {
        ensureNativeParams()
        return when {
            initSuccess -> this.sendUniservalDataSeq(mid, "", data)
            else -> throw JfgException("sendUniservalDataSeq error, init:$initSuccess,mid:$mid,data:$data")
        }
    }

    @Throws(JfgException::class)
    override fun sendUniservalDataSeq(mid: Int, callee: String, data: ByteArray?): Long {
        ensureNativeParams()
        return when {
            initSuccess && data?.isNotEmpty() == true -> JfgAppJni.SendUniversalDataSeq(mid, callee, data)
            else -> throw JfgException("sendUniservalDataSeq error, init:$initSuccess,mid:$mid,callee:$callee,data:$data")
        }
    }

    @Throws(JfgException::class)
    override fun setTargetLeveledBFS(mic: Int, speaker: Int): Int {
        ensureNativeParams()
        return when {
            initSuccess -> JfgAppJni.SetTargetLeveledBFS(mic, speaker)
            else -> throw JfgException("setTargetLeveledBFS error,init$initSuccess,mic:$mic,speaker:$speaker")
        }
    }

    override fun getSessionId(): String {
        ensureNativeParams()
        return when {
            initSuccess -> JfgAppJni.GetSessionId()
            else -> ""
        }
    }

    @Throws(JfgException::class)
    override fun getVideoListV2(cid: String, beginTime: Int, way: Int, num: Int): Int {
        ensureNativeParams()
        return when {
            initSuccess && cid.isNotEmpty() -> JfgAppJni.GetVideoListV2(cid, beginTime, way, num)
            else -> throw JfgException("getVideoListV2 error,init:$initSuccess,cid:$cid,beginTime:$beginTime,way:$way,num:$num")
        }
    }
}