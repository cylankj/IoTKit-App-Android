package com.cylan.jiafeigou.n.view.panorama

import android.view.View
import com.androidkun.xtablayout.XTabLayout
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.ActivityComponent
import com.cylan.jiafeigou.base.wrapper.BaseActivity
import com.cylan.jiafeigou.support.log.AppLogger
import kotlinx.android.synthetic.main.activity_live_setting.*

class LiveSettingActivity : BaseActivity<LiveSettingContact.Presenter>(), LiveSettingContact.View, XTabLayout.OnTabSelectedListener {


    override fun setActivityComponent(activityComponent: ActivityComponent?) {
        activityComponent!!.inject(this)
    }

    override fun getContentViewID(): Int {
        return R.layout.activity_live_setting
    }


    override fun initViewAndListener() {
        super.initViewAndListener()
        rtmp_type_tabs.setOnTabSelectedListener(this)

    }


    fun setup(v: View) {

    }

    override fun onTabReselected(tab: XTabLayout.Tab?) {
    }

    override fun onTabUnselected(tab: XTabLayout.Tab?) {
    }

    override fun onTabSelected(tab: XTabLayout.Tab?) {
        when (tab!!.position) {
            0 -> {
                AppLogger.w("Facebook")
            }
            1 -> {
                AppLogger.w("YouTube")
            }
            2 -> {
                AppLogger.w("Weibo")
            }
            3 -> {
                AppLogger.w("Rtmp")
            }
        }
    }

}
