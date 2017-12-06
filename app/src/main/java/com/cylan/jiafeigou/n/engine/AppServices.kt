package com.cylan.jiafeigou.n.engine

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.cylan.jiafeigou.module.BellerSupervisor
import com.cylan.jiafeigou.module.DeviceSupervisor
import com.cylan.jiafeigou.module.HookerSupervisor
import com.cylan.jiafeigou.module.Supervisor

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
        DeviceSupervisor.monitorSyncMessages()
        DeviceSupervisor.monitorReportDevices()
        BellerSupervisor.monitorBeller()
    }

    override fun onDestroy() {
        super.onDestroy()
        HookerSupervisor.removeHooker(appHooker)
    }
    private class AppHooker : HookerSupervisor.ActionHooker() {

        override fun doHookerActionHooker(action: Supervisor.Action, parameter: HookerSupervisor.HookerActionParameter): Any? {
            Log.d(TAG, "App is alive and hook a action:${parameter.action}")
            return super.doHookerActionHooker(action, parameter)
        }
    }
}