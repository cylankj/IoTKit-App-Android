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
object BellerSupervisor {
    private val TAG = BellerSupervisor::class.java.simpleName
    private val hookers: MutableList<BellerHooker> = mutableListOf(OwnerHooker(), RepeatHooker())

    private class RepeatHooker : BellerHooker {
        private val record = mutableMapOf<String, Long>()
        private val CALL_DURATION: Long = 30
        private val TAG = RepeatHooker::class.java.simpleName
        override fun hook(cid: String, time: Long, url: String): Boolean {
            val lastTime = record[cid] ?: 0
            record[cid] = time
            if (time - lastTime < CALL_DURATION) {
                Log.d(TAG, "the bell call(caller:$cid,time:$time,url:$url) is in call duration,skip it")
                return true
            }
            return false
        }
    }

    private class OwnerHooker : BellerHooker {
        override fun hook(cid: String, time: Long, url: String): Boolean {
            return DeviceSupervisor.getDevice(cid) != null
        }
    }

    interface BellerHooker {
        fun hook(cid: String, time: Long, url: String): Boolean
    }

    init {
        listenForLocal()
        listenForRemote()
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

    private fun performHooker(cid: String, time: Long, url: String): Boolean {
        val hooked = hookers.any { it.hook(cid, time, url) }
        Log.d(TAG, "hook finished: the hook result for cid:$cid,time:$time,url:$url is:$hooked ")
        return hooked
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
        if (!performHooker(cid, time, url)) {
            Log.d(TAG, "performLauncher,cid:$cid,time:$time,url:$url")
            val intent = Intent(ContextUtils.getContext(), BellLiveActivity::class.java)
            intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, cid)
            intent.putExtra(JConstant.VIEW_CALL_WAY, JConstant.VIEW_CALL_WAY_LISTEN)
            intent.putExtra(JConstant.VIEW_CALL_WAY_EXTRA, url)
            intent.putExtra(JConstant.VIEW_CALL_WAY_TIME, time)
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)//华为服务使用.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra(JConstant.IS_IN_BACKGROUND, BaseApplication.isBackground())
            ContextUtils.getContext().startActivity(intent)
        }
    }

    @JvmStatic
    fun addHooker(hooker: BellerHooker) {
        Log.d(TAG, "hooker:$hooker is added")
        hookers.add(hooker)
    }

    @JvmStatic
    fun removeHooker(hooker: BellerHooker) {
        Log.d(TAG, "hooker:$hooker is removed")
        hookers.remove(hooker)
    }
}