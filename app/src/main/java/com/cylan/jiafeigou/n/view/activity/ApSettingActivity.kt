package com.cylan.jiafeigou.n.view.activity

// Using R.layout.activity_main from the main source set
import android.content.*
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.misc.AlertDialogManager
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.misc.UdpDevice
import com.cylan.jiafeigou.misc.bind.UdpConstant
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity
import com.cylan.jiafeigou.n.mvp.contract.setting.ApSettingContract
import com.cylan.jiafeigou.n.mvp.impl.setting.ApSettingsPresenter
import com.cylan.jiafeigou.rx.RxEvent
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.*
import com.cylan.jiafeigou.widget.LoadingDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_ap_setting.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class ApSettingActivity : BaseFullScreenFragmentActivity<ApSettingContract.Presenter>(),
        ApSettingContract.View {

    override fun getHotSpotName(): String {
        return et_ap_name.text.toString()
    }


    override fun success() {
        ToastUtil.showToast(getString(R.string.PWD_OK_2))
        finishExt()
    }

    override fun timeout() {
        //连接失败，请重试。
        //失败后，必须重新 恢复状态。
        ToastUtil.showToast(getString(R.string.HOTSPOTS_CONNECT_TIMEOUT_TIPS))
    }

    val BACK_FROM_TETHER_SETTINGS: Int = 5000
    val BACK_FROM_WRITE_SETTINGS: Int = 5001

    var tetherChangeReceiver: TetherChangeReceiver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ap_setting)
        //初始化presenter
        basePresenter = ApSettingsPresenter(this)
        //获取AP_Name
        ct_tool_bar.setBackAction {
            AlertDialogManager.getInstance().showDialog(this, "back", getString(R.string.Tap1_AddDevice_tips),
                    getString(R.string.OK), { _, _ ->
                finishExt()
            }, getString(R.string.CANCEL), { dialog, _ -> dialog.dismiss() })
        }
        val apConfig: WifiConfiguration = WifiApUtils.getApControl().wifiApConfiguration
        if (apConfig.SSID == null || TextUtils.isEmpty(apConfig.SSID)) {
            et_ap_name.setText("Cam-Hotspot")
            et_ap_pwd.setText("11111111")
        } else et_ap_name.setText(apConfig.SSID)
        //禁止中文输入，长度最大32
        et_ap_name.filters = arrayOf(ViewUtils.excludeChineseAndBlankFilter(),
                ViewUtils.getMaxLenInputFilter(32))
        tv_submit_ap.setOnClickListener {
            val ssid: String = et_ap_name.text.toString()
            val pwd: String = et_ap_pwd.text.toString()
            if (TextUtils.isEmpty(ssid)) {
                ToastUtil.showToast("缺语言包：请输入热点名称")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(pwd)) {
                ToastUtil.showToast(getString(R.string.HOTSPOT_PASSWORD))
                return@setOnClickListener
            }
            if (pwd.length < 8) {
                ToastUtil.showToast(getString(R.string.HOTSPOT_PASSWORD_ERROR))
                return@setOnClickListener
            }
            if (needTurnOnWriteSetting()) {
                //需要允许 app读写 Settings
                //如果不能自动启动热点，可以跳转到热点页面.

                return@setOnClickListener
            }
            ToastUtil.showToast("设计缺陷:确保sim正常使用")
            val fullCid: String = intent.getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID)
            LoadingDialog.showLoading(this, getString(R.string.Start_Hotspot))
//            basePresenter.addSubscription("getDevicePortrait",
            UdpDevice.getDevicePortrait(fullCid, UdpConstant.PORT, UdpConstant.IP)
                    .flatMap { r -> UdpDevice.sendWifiInfo(r, ssid, pwd, 3, UdpConstant.IP, UdpConstant.PORT) }
                    .subscribeOn(Schedulers.newThread())
                    .delay(500, TimeUnit.MILLISECONDS)
                    .map {
                        val result: Boolean = NetUtils.createHotSpot(ssid, pwd)
                        //开启应该等候1-2s，等待hotSpot完全启动
                        AppLogger.d("即将开启热点:" + result)
//                        basePresenter.monitorHotSpot()
                        registerReceiver()
                        if (!result) throw RxEvent.HelperBreaker("setHotSpotFailed")
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        r ->
                        Log.d("...", ",发送wifi配置成功，即将开启热点,,...:" + Gson().toJson(r))
                        LoadingDialog.dismissLoading()
                    }, { r ->
                        val result: String = r.localizedMessage
                        if (result.contains("ingFailed")) {
                            //设备找不到
                            ToastUtil.showToast("缺语言包:不在同一个局域网")
                        } else if (result.contains("sendWifiInfoFailed")) {
                            //设置wifi失败
                        }
                        LoadingDialog.dismissLoading()
                        ToastUtil.showToast(getString(R.string.Start_Failed))
                        Log.d("...", ",,,...:" + r)
                    })
        }
        //默认显示
        ViewUtils.showPwd(et_ap_pwd, true)
        cb_show_pwd.setOnCheckedChangeListener { _, isChecked ->
            ViewUtils.showPwd(et_ap_pwd, isChecked)
        }
    }

    override fun onStart() {
        super.onStart()
    }

    private fun registerReceiver() {
        unregisterReceiver()
        val filter: IntentFilter = IntentFilter()
        filter.addAction("android.net.conn.TETHER_STATE_CHANGED")
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED")
        filter.addAction("com.android.settings.TetherSettings")
        tetherChangeReceiver = TetherChangeReceiver()
        registerReceiver(tetherChangeReceiver, filter)
    }

    private fun unregisterReceiver() {
        if (tetherChangeReceiver != null)
            unregisterReceiver(tetherChangeReceiver)
    }

    override fun onStop() {
        super.onStop()
        if (tetherChangeReceiver != null)
            unregisterReceiver(tetherChangeReceiver)
    }

    override fun onBackPressed() {
        finishExt()
    }

    fun needTurnOnWriteSetting(): Boolean {
        // 6.0-7.1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            //7.1 以上需要 手工设置
            jump2HotSpotSettings()
            return true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1
                && !android.provider.Settings.System.canWrite(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + packageName)
            startActivityForResult(intent, BACK_FROM_WRITE_SETTINGS)
            return true
        }
        return false
    }

    fun jump2HotSpotSettings() {
        //7.1手机需要权限 WRITE_SETTINGS权限
        val intent = Intent("com.android.settings.TetherSettings")
        intent.setClassName("com.android.settings",
                "com.android.settings.TetherSettings")
        val cName: ComponentName = intent.resolveActivity(packageManager)
        if (cName != null && !TextUtils.isEmpty(cName.packageName)) {
            //got it
            startActivityForResult(intent, BACK_FROM_TETHER_SETTINGS)
        } else {
            AppLogger.e("what the hell:" + MiscUtils.dumpSystemInfo())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BACK_FROM_TETHER_SETTINGS) {
            //
            AppLogger.d("检查热点信息")
        } else if (requestCode == BACK_FROM_WRITE_SETTINGS) {

        }
    }

    //get client list
    //https://github.com/nickrussler/Android-Wifi-Hotspot-Manager-Class/blob/master/src/com/whitebyte/wifihotspotutils/WifiApManager.java
    inner class TetherChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String = intent?.action!!
            if (action == ("android.net.conn.TETHER_STATE_CHANGED")) {
                val available: ArrayList<String> = intent.getStringArrayListExtra("availableArray")
                val active: ArrayList<String> = intent.getStringArrayListExtra("activeArray")
                val errored: ArrayList<String> = intent.getStringArrayListExtra("erroredArray")
                Log.d("...", "TETHER_STATE_CHANGED:" + Gson().toJson(available) + "," + Gson().toJson(active))
                basePresenter!!.monitorHotSpot()
            } else if (action == ("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                val state: Int = intent.getIntExtra("wifi_state", 0)
                Log.d("...", "WIFI_AP_STATE_CHANGED:" + state)
            }
        }
    }
}
