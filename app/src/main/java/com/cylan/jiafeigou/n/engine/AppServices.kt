package com.cylan.jiafeigou.n.engine

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION
import android.os.IBinder
import android.util.Log
import com.cylan.entity.jniCall.JFGDoorBellCaller
import com.cylan.jiafeigou.module.*
import com.cylan.jiafeigou.rx.RxEvent
import com.cylan.jiafeigou.support.network.NetMonitor
import com.cylan.jiafeigou.support.network.NetworkCallback
import com.cylan.jiafeigou.utils.NetUtils
import rx.Observable
import java.util.concurrent.TimeUnit

/**
 * Created by yanzhendong on 2017/12/1.
 */
class AppServices() : Service(), NetworkCallback {


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
        NetMonitor.getNetMonitor().registerNet(this, arrayOf(ConnectivityManager.CONNECTIVITY_ACTION, NETWORK_STATE_CHANGED_ACTION))
    }

    private var hasLoginAction = false

    override fun onNetworkChanged(context: Context?, intent: Intent) {
        if (NetUtils.isNetworkAvailable(context)) {
            Log.d(TAG, "网络状态发生了变化,正在执行自动登录")
            if (hasLoginAction) {
                hasLoginAction = false
                val subscribe = Observable.just("").delay(2, TimeUnit.SECONDS)
                        .flatMap { LoginHelper.performAutoLogin() }
                        .subscribe({
                            Log.d(TAG, "登录成功了")
                        }) {
                            it.printStackTrace()
                            Log.d(TAG, "登录失败了:${it.message}")
                        }
                SubscriptionSupervisor.subscribe(this, SubscriptionSupervisor.CATEGORY_DEFAULT, "LoginHelper.performAutoLogin()", subscribe)
            }
        } else {
            hasLoginAction = true
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        HookerSupervisor.removeHooker(appHooker)
        NetMonitor.getNetMonitor().unregister(this)
        SubscriptionSupervisor.unsubscribe(this, SubscriptionSupervisor.CATEGORY_DEFAULT, "LoginHelper.performAutoLogin()")
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