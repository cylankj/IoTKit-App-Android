package com.cylan.jiafeigou.n.view.activity

// Using R.layout.activity_main from the main source set
import android.net.wifi.WifiConfiguration
import android.os.Bundle
import android.text.TextUtils
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity
import com.cylan.jiafeigou.n.mvp.BasePresenter
import com.cylan.jiafeigou.utils.ToastUtil
import com.cylan.jiafeigou.utils.ViewUtils
import com.cylan.jiafeigou.utils.WifiApUtils
import kotlinx.android.synthetic.main.activity_ap_setting.*


class ApSettingActivity : BaseFullScreenFragmentActivity<BasePresenter>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ap_setting)
        //获取AP_Name

        var apConfig: WifiConfiguration = WifiApUtils.getApControl().wifiApConfiguration
        if (apConfig.SSID == null)
            et_ap_name.setText(getString(R.string.ENTER_WIFI))
        else et_ap_name.setText(apConfig.SSID)
        //禁止中文输入，长度最大32
        et_ap_name.filters = arrayOf(ViewUtils.excludeChineseAndBlankFilter(),
                ViewUtils.getMaxLenInputFilter(32))
        tv_submit_ap.setOnClickListener {
            val ssid: String = et_ap_name.text.toString()
            val pwd: String = et_ap_pwd.text.toString()
            if (TextUtils.isEmpty(ssid)) {
                ToastUtil.showToast("请输入热点名称")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(pwd)) {
                ToastUtil.showToast("请输入热点密码")
                return@setOnClickListener
            }
            if (pwd.length < 8) {
                ToastUtil.showToast("密码不能少于8位")
                return@setOnClickListener
            }
            //先发送完wifi配置，再自身启热点.
            // 此处非常反人类
            // 1.客户端连接设备，然后发送配置信息.
            // 2.设备连接客户端，开始直播.
            // 各位领导，体验一下茄子快传的交互吧.
        }
        //默认显示
        ViewUtils.showPwd(et_ap_pwd, true)
        cb_show_pwd.setOnCheckedChangeListener { _, isChecked ->
            ViewUtils.showPwd(et_ap_pwd, isChecked)
        }
    }
}
