package com.cylan.jiafeigou.n.engine

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION
import android.os.IBinder
import android.support.v7.app.AlertDialog
import android.util.Log
import com.cylan.entity.JfgEnum
import com.cylan.entity.jniCall.JFGDoorBellCaller
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.SmartcallActivity
import com.cylan.jiafeigou.module.*
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.rx.RxBus
import com.cylan.jiafeigou.rx.RxEvent
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.support.network.NetMonitor
import com.cylan.jiafeigou.support.network.NetworkCallback
import com.cylan.jiafeigou.utils.ContextUtils
import com.cylan.jiafeigou.utils.NetUtils


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
        BellerSupervisor.monitorBeller()
        NetMonitor.getNetMonitor().registerNet(this, arrayOf(ConnectivityManager.CONNECTIVITY_ACTION, NETWORK_STATE_CHANGED_ACTION))
    }


    @Volatile
    private var isConnected: Boolean = true

    override fun onNetworkChanged(context: Context?, intent: Intent) {
        var connected = NetUtils.getJfgNetType() != 0
        if (connected != isConnected) {
            //网络连接状态发生了变化
            if (connected && !BaseApplication.isBackground()) {
                Command.getInstance().reportEnvChange(JfgEnum.ENVENT_TYPE.ENV_NETWORK_CONNECTED)
            }
        }
        isConnected = connected;
    }


    override fun onDestroy() {
        super.onDestroy()
        HookerSupervisor.removeHooker(appHooker)
        NetMonitor.getNetMonitor().unregister(this)
        SubscriptionSupervisor.unsubscribe(this, SubscriptionSupervisor.CATEGORY_DEFAULT, "LoginHelper.performAutoLogin()")
    }

    private inner class AppHooker : HookerSupervisor.ActionHooker() {

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
                is RxEvent.PwdHasResetEvent -> doHookerPasswordChanged(eventAction)
            }

        }

        private fun doHookerPasswordChanged(eventAction: RxEvent.PwdHasResetEvent) {
            AppLogger.d("收到密码已被修改通知" + BaseApplication.isBackground())
            LoginHelper.performLogout()
            RxBus.getCacheInstance().removeAllStickyEvents()
            if (!BaseApplication.isBackground()) {
                when (eventAction.code) {
                    16008, 1007, 16006 -> {
                        val dialog = AlertDialog.Builder(BaseApplication.getCurrentActivity(), R.style.AlertDialog)
                                .setTitle(R.string.RET_ELOGIN_ERROR)
                                .setMessage(R.string.PWD_CHANGED)
                                .setCancelable(false)
                                .setPositiveButton(R.string.OK, { dialog, _ ->
                                    dialog.dismiss()
                                    val intent = Intent(ContextUtils.getContext(), SmartcallActivity::class.java)
                                    intent.putExtra("from_log_out", true);
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                                    ContextUtils.getContext().applicationContext.startActivity(intent);
                                }).create()
                        dialog.show()
                    }
                }
            }
        }

        private fun doHookerDoorBeller(eventAction: JFGDoorBellCaller) {
        }

        private fun doHookerReportDevices(eventAction: AppCallbackSupervisor.ReportDeviceEvent) {
            DeviceSupervisor
        }

        private fun doHookerSyncMessages(syncEvent: RxEvent.DeviceSyncRsp) {
//            syncEvent.dpList.forEach { PropertySupervisor.setValue(syncEvent.uuid, it.id.toInt(), it.version, it.packValue) }
        }
    }
}