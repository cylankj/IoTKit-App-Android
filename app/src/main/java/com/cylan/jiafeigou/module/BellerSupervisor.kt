package com.cylan.jiafeigou.module

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.cylan.entity.jniCall.JFGDoorBellCaller
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity
import com.cylan.jiafeigou.support.Security
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ContextUtils

/**
 * Created by yanzhendong on 2017/12/4.
 */
object BellerSupervisor : Supervisor {
    private val TAG = BellerSupervisor::class.java.simpleName
    private val urlHooker = URLHooker()

    init {
        HookerSupervisor.addHooker(OwnerHooker())
        HookerSupervisor.addHooker(urlHooker)
        HookerSupervisor.addHooker(RepeatHooker())

    }

    fun monitorBeller() {
        listenForLocal()
        listenForRemote()
    }

    abstract class BellerHooker : Supervisor.Hooker {
        override fun parameterType(): Array<Class<*>> = arrayOf(BellerParameter::class.java)

        override fun hooker(action: Supervisor.Action) {
            val parameter = action.parameter()
            when (parameter) {
                is BellerParameter -> doHooker(action, parameter)
                else -> action.process()
            }
        }

        open protected fun doHooker(action: Supervisor.Action, parameter: BellerParameter) {
            action.process()
        }
    }

    private class RepeatHooker : BellerHooker() {
        private val TAG = RepeatHooker::class.java.simpleName
        private val record = mutableMapOf<String, Long>()
        private val CALL_DURATION: Long = 30
        override fun doHooker(action: Supervisor.Action, parameter: BellerParameter) {
            Log.d("BellerSupervisor", "RepeatHooker:" + parameter)
            val lastTime = record[parameter.cid] ?: 0
            record[parameter.cid] = parameter.time
            if (parameter.time - lastTime < CALL_DURATION) {
                Log.d(TAG, "the bell call(caller:${parameter.cid},time:${parameter.time},url:${parameter.url}) is in call duration,skip it")
            } else {
                action.process()
            }
        }
    }

    private class URLHooker : BellerHooker() {
        val recordUrls = mutableMapOf<String, String>()
        override fun doHooker(action: Supervisor.Action, parameter: BellerParameter) {
            Log.d("BellerSupervisor", "URLHooker:" + parameter)
            if (!parameter.url.isEmpty()) {
                recordUrls[parameter.cid] = parameter.url
            }
            super.doHooker(action, parameter)
        }
    }

    private class OwnerHooker : BellerHooker() {
        override fun doHooker(action: Supervisor.Action, parameter: BellerParameter) {
            Log.d("BellerSupervisor", "OwnerHooker:" + parameter)
            if (TextUtils.equals(DataSourceManager.getInstance().getDevice(parameter.cid).uuid, parameter.cid)) {
                action.process()
            }
        }
    }

    data class BellerParameter(var cid: String, var time: Long, var url: String)

    private data class BellerAction(private val parameter: BellerParameter) : Supervisor.Action {

        override fun parameter() = parameter

        override fun process(): Any? {
            Log.d(TAG, "performLauncher,cid:${parameter.cid},time:${parameter.time},url:${parameter.url}")
            val intent = Intent(ContextUtils.getContext(), BellLiveActivity::class.java)
            intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, parameter.cid)
            intent.putExtra(JConstant.VIEW_CALL_WAY, JConstant.VIEW_CALL_WAY_LISTEN)
            intent.putExtra(JConstant.VIEW_CALL_WAY_EXTRA, parameter.url)
            intent.putExtra(JConstant.VIEW_CALL_WAY_TIME, parameter.time)
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)//华为服务使用.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra(JConstant.IS_IN_BACKGROUND, BaseApplication.isBackground())
            ContextUtils.getContext().startActivity(intent)
            return parameter
        }

        override fun toString(): String {
            return "BellerAction(parameter=$parameter)"
        }

    }

    private fun listenForLocal() {
        UDPMessageSupervisor.observeDoorBellRing().subscribe(BellerSupervisor::receiveLocalBeller) {
            it.printStackTrace()
            AppLogger.e(it)
            listenForLocal()
        }
    }

    private fun receiveLocalBeller(event: UDPMessageSupervisor.DoorBellRingEvent) {
        Log.d(TAG, "receive local beller:$event")
        performLauncher(event.caller)
    }

    private fun listenForRemote() {
        AppCallbackSupervisor.observe(JFGDoorBellCaller::class.java).subscribe(BellerSupervisor::receiveRemoteBeller) {
            it.printStackTrace()
            AppLogger.e(it)
            listenForRemote()
        }
    }

    private fun receiveRemoteBeller(event: JFGDoorBellCaller) {
        Log.d(TAG, "receive remote beller:$event")
        val caller = event.cid
        val time = event.time
        val regionType = event.regionType
//        val device = DeviceSupervisor.getDevice(caller)
        val device = DataSourceManager.getInstance().getDevice(caller)
        val V2 = device?.vid?.isEmpty() == true
        val url = if (V2) {
            "cylan:///$caller/$time.jpg?regionType=$regionType"
        } else {
            "cylan:///cid/${Security.getVId()}/$caller/$time.jpg?regionType=$regionType"
        }
        performLauncher(caller, time, url)
    }

    @JvmStatic
    fun receivePushBeller(context: Context, message: String, bundle: Bundle) {
        Log.d(TAG, "receive push beller,message:$message,bundle:$bundle")
        //[16,'500000000385','',1488012270,1]
        val items = message.split(",")
        val cid = items[1].replace("\'", "")
        val time = items[3].toLong()
        val url = items[4].replace("\'", "")
        performLauncher(cid, time, url)
    }

    @JvmStatic
    fun receiveRemoteDoorBeller() {

    }

    @JvmStatic
    fun receiveLocalDoorBeller() {

    }

    @JvmStatic
    fun getBellerPicture(uuid: String): String? {
        return urlHooker.recordUrls[uuid]
    }

    private fun performLauncher(cid: String, time: Long = System.currentTimeMillis() / 1000L, url: String = "") {
        HookerSupervisor.performHooker(BellerAction(BellerParameter(cid, time, url)))
    }
}