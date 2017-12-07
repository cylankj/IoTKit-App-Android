package com.cylan.jiafeigou.n.engine

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.cylan.entity.jniCall.JFGDoorBellCaller
import com.cylan.jiafeigou.module.*
import com.cylan.jiafeigou.rx.RxEvent

/**
 * Created by yanzhendong on 2017/12/1.
 */
class AppServices() : Service() {
    companion object {
        const val TAG = "CYLAN_TAG:AppServices:"
    }

    private val appHooker = AppHooker()
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        HookerSupervisor.addHooker(appHooker)
        DeviceSupervisor.monitorReportDevices()
        BellerSupervisor.monitorBeller()
    }

    override fun onDestroy() {
        super.onDestroy()
        HookerSupervisor.removeHooker(appHooker)
    }

    private class AppHooker : HookerSupervisor.ActionHooker() {

        override fun doHookerActionHooker(action: Supervisor.Action, parameter: HookerSupervisor.HookerActionParameter): Any? {
            val actionParameter = parameter.action.parameter()
            Log.d(TAG, "App is alive and hook a action:${parameter.action}")
            when (actionParameter) {
                is AppCallbackSupervisor.PublishParameter -> doHookerPublishAction(actionParameter)
            }
            return super.doHookerActionHooker(action, parameter)
        }

        private fun doHookerPublishAction(parameter: AppCallbackSupervisor.PublishParameter) {
            val eventAction = parameter.event
            when (eventAction) {
                is RxEvent.DeviceSyncRsp -> doHookerSyncMessages(eventAction)
                is AppCallbackSupervisor.ReportDeviceEvent -> doHookerReportDevices(eventAction)
                is JFGDoorBellCaller -> doHookerDoorBeller(eventAction)
            }

        }

        private fun doHookerDoorBeller(eventAction: JFGDoorBellCaller) {
        }

        private fun doHookerReportDevices(eventAction: AppCallbackSupervisor.ReportDeviceEvent) {
            DeviceSupervisor
        }

        private fun doHookerSyncMessages(syncEvent: RxEvent.DeviceSyncRsp) {
            syncEvent.dpList.forEach { PropertySupervisor.setValue(syncEvent.uuid, it.id.toInt(), it.version, it.packValue) }
        }
    }
}