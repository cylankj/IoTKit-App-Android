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
        tv_submit_ap.setOnClickListener {
            if (TextUtils.isEmpty(et_ap_name.text)) {
                ToastUtil.showToast("请输入热点名称")
                return@setOnClickListener
            }

        }

        cb_show_pwd.setOnCheckedChangeListener { _, isChecked ->
            ViewUtils.showPwd(et_ap_pwd, isChecked)
        }
    }
}
