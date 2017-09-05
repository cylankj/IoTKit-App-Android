package com.cylan.jiafeigou.n.view.activity

// Using R.layout.activity_main from the main source set
import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity
import com.cylan.jiafeigou.n.mvp.BasePresenter
import com.cylan.jiafeigou.utils.WifiApUtils
import kotlinx.android.synthetic.main.activity_ap_setting.*


class ApSettingActivity : BaseFullScreenFragmentActivity<BasePresenter>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ap_setting)
        //获取AP_Name
//        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
//        intent.data = Uri.parse("package:" + packageName)
//        startActivity(intent)

        var wifiManager: WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        tv_submit_ap.setOnClickListener {
            var isApEnable: Boolean = WifiApUtils.isApSupported()
            val apConfig = WifiConfiguration()
            apConfig.SSID = "hunt---hnt"
            // 热点的配置类
            // 配置热点的名称(可以在名字后面加点随机数什么的)
            // 配置热点的密码
            apConfig.preSharedKey = "12345678"
            // 安全：WPA2_PSK
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            WifiApUtils.getApControl(wifiManager)
                    .setWifiApEnabled(apConfig, true)
            println("isSupport:" + isApEnable)
        }

        val wifiApState: Boolean = WifiApUtils.getApControl(wifiManager).isWifiApEnabled
        val wifiApStat: Int = WifiApUtils.getApControl(wifiManager).wifiApState

        println("wifiApState:" + wifiApState)
        println("wifiApStat:" + wifiApStat)


    }
}
