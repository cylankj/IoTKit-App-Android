package com.cylan.jiafeigou.n.engine

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.cylan.jiafeigou.module.*
import com.cylan.jiafeigou.support.log.AppLogger

/**
 * Created by yanzhendong on 2017/12/1.
 */
class AppServices() : Service() {
    private val TAG = "AppServices"
    private val bellerHooker = AppBellerHooker()
    private val deviceHooker = AppDeviceHooker()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        BellerSupervisor.addHooker(bellerHooker)
        DeviceSupervisor.addHooker(deviceHooker)
        monitorSyncMessage()
    }

    override fun onDestroy() {
        super.onDestroy()
        BellerSupervisor.removeHooker(bellerHooker)
        DeviceSupervisor.removeHooker(deviceHooker)
    }

    private fun monitorSyncMessage() {
        AppCallbackSupervisor.observeRobotSyncData().subscribe(this::receiveSyncMessage) {
            it.printStackTrace()
            AppLogger.e(it)
            monitorSyncMessage()
        }
    }

    private fun receiveSyncMessage(event: AppCallbackSupervisor.RobotSyncDateEvent) {
        Log.d(TAG, "receive sync message,uuid:${event.s},from device:${event.b},messages:${event.arrayList}")
        event.arrayList?.forEach { PropertySupervisor.setValue(event.s, it.id.toInt(), it.version, it.packValue) }
    }

    private class AppBellerHooker : BellerSupervisor.BellerHooker {
        private val TAG = AppBellerHooker::class.java.simpleName
        override fun hook(cid: String, time: Long, url: String): Boolean {
            Log.d(TAG, "App still alive and received a beller:$cid")
            return false
        }
    }

    private class AppDeviceHooker : DeviceSupervisor.DeviceHooker {
        private val TAG = AppDeviceHooker::class.java.simpleName
        override fun hook(device: Device, uuid: String, msgId: Int): Boolean {
            Log.d(TAG, "App still alive and hooke a getValue request,device:$device,uuid:$uuid,msgId:$msgId")
            return false
        }
    }
}