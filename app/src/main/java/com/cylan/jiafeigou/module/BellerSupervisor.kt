package com.cylan.jiafeigou.module

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    init {
        addHooker(OwnerHooker())
        addHooker(RepeatHooker())
        listenForLocal()
        listenForRemote()
    }

    interface BellerHooker : Supervisor.Hooker<BellerParameter>

    private class RepeatHooker : BellerHooker {
        private val record = mutableMapOf<String, Long>()
        private val CALL_DURATION: Long = 30
        private val TAG = RepeatHooker::class.java.simpleName

        override fun hook(action: Supervisor.Action<BellerParameter>, parameter: BellerParameter): BellerParameter? {
            val lastTime = record[parameter.cid] ?: 0
            record[parameter.cid] = parameter.time
            return if (parameter.time - lastTime < CALL_DURATION) {
                Log.d(TAG, "the bell call(caller:${parameter.cid},time:${parameter.time},url:${parameter.url}) is in call duration,skip it")
                parameter
            } else {
                action.process(parameter)
            }
        }
    }

    private class OwnerHooker : BellerHooker {
        override fun hook(action: Supervisor.Action<BellerParameter>, parameter: BellerParameter): BellerParameter? {
            return if (DeviceSupervisor.getDevice(parameter.cid) == null) {
                parameter
            } else {
                action.process(parameter)
            }
        }
    }

    class BellerParameter(var cid: String, var time: Long, var url: String) : Supervisor.Parameter

    private class BellerAction : Supervisor.Action<BellerParameter> {
        override fun process(parameter: BellerParameter): BellerParameter? {
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
        AppCallbackSupervisor.observeDoorBellCall().subscribe(BellerSupervisor::receiveRemoteBeller) {
            it.printStackTrace()
            AppLogger.e(it)
            listenForRemote()
        }
    }

    private fun receiveRemoteBeller(event: AppCallbackSupervisor.DoorBellCallEvent) {
        Log.d(TAG, "receive remote beller:$event")
        val caller = event.bellCaller.cid
        val time = event.bellCaller.time
        val regionType = event.bellCaller.regionType
        val device = DeviceSupervisor.getDevice(caller)
        val V2 = device?.box?.vid?.isEmpty() == true
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

    private fun performLauncher(cid: String, time: Long = System.currentTimeMillis() / 1000L, url: String = "") {
        HookerSupervisor.performHooker(this, BellerAction(), BellerParameter(cid, time, url))
    }


    @JvmStatic
    fun addHooker(hooker: BellerHooker) {
        HookerSupervisor.addHooker(this, BellerParameter::class.java, hooker)
    }

    @JvmStatic
    fun removeHooker(hooker: BellerHooker) {
        Log.d(TAG, "hooker:$hooker is removed")
        HookerSupervisor.removeHooker(this, BellerParameter::class.java, hooker)
    }
}