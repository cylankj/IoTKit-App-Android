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
    private val propertyHooker = AppPropertyHooker()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        BellerSupervisor.addHooker(bellerHooker)
        DeviceSupervisor.addHooker(deviceHooker)
        PropertySupervisor.addHooker(propertyHooker)
        monitorSyncMessage()
    }

    override fun onDestroy() {
        super.onDestroy()
        BellerSupervisor.removeHooker(bellerHooker)
        DeviceSupervisor.removeHooker(deviceHooker)
        PropertySupervisor.removeHooker(propertyHooker)
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
        override fun hook(action: Supervisor.Action<BellerSupervisor.BellerParameter>, parameter: BellerSupervisor.BellerParameter): BellerSupervisor.BellerParameter? {
            Log.d(TAG, "App still alive and received a beller:${parameter.cid}")
            return action.process(parameter)
        }
    }

    private class AppDeviceHooker : DeviceSupervisor.DeviceHooker {
        private val TAG = AppDeviceHooker::class.java.simpleName
        override fun hook(action: Supervisor.Action<DeviceSupervisor.DeviceParameter>, parameter: DeviceSupervisor.DeviceParameter): DeviceSupervisor.DeviceParameter? {
            Log.d(TAG, "App still alive and hooke a getDevice request," +
                    "device:${parameter.device},uuid:${parameter.uuid}")
            return action.process(parameter)
        }
    }

    private class AppPropertyHooker : PropertySupervisor.PropertyHooker {
        private val TAG = AppPropertyHooker::class.java.simpleName
        override fun hook(action: Supervisor.Action<PropertySupervisor.PropertyParameter>, parameter: PropertySupervisor.PropertyParameter): PropertySupervisor.PropertyParameter? {
            Log.d(TAG, "App still alive and hooker a property request: uuid:${parameter.uuid},msgId:${parameter.msgId},version:${parameter.version},value:${parameter.value},bytes:${parameter.bytes}")
            return action.process(parameter)
        }
    }
}