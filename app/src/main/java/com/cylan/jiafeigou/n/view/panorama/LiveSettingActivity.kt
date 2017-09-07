package com.cylan.jiafeigou.n.view.panorama

import android.content.Intent
import com.androidkun.xtablayout.XTabLayout
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.ActivityComponent
import com.cylan.jiafeigou.base.wrapper.BaseActivity
import com.cylan.jiafeigou.utils.ActivityUtils
import kotlinx.android.synthetic.main.activity_live_setting.*

class LiveSettingActivity : BaseActivity<LiveSettingContact.Presenter>(), LiveSettingContact.View, XTabLayout.OnTabSelectedListener {

    private val facebookFragment by lazy { FacebookLiveSettingFragment.newInstance(uuid) }
    private val youtubeFragment by lazy { YouTubeLiveSettingFragment.newInstance(uuid) }
    private val weiboFragment by lazy { WeiboLiveSettingFragment.newInstance(uuid) }
    private val rtmpFragment by lazy { RtmpLiveSettingFragment.newInstance(uuid) }

    override fun setActivityComponent(activityComponent: ActivityComponent?) {
        activityComponent!!.inject(this)
    }

    override fun getContentViewID(): Int {
        return R.layout.activity_live_setting

    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        rtmp_type_tabs.setOnTabSelectedListener(this)
        switchRtmpPage(rtmp_type_tabs.selectedTabPosition)
        /*这里可能以后又会有一大堆判断,返回键*/
        custom_toolbar.setBackAction { onBackPressed() }
        
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onTabReselected(tab: XTabLayout.Tab?) {

    }

    override fun onTabUnselected(tab: XTabLayout.Tab?) {
    }

    override fun onTabSelected(tab: XTabLayout.Tab?) {
        switchRtmpPage(tab!!.position)
    }

    private fun switchRtmpPage(position: Int) {
        when (position) {
            0 -> ActivityUtils.replaceFragmentNoAnimation(R.id.page_container, supportFragmentManager, facebookFragment)
            1 -> ActivityUtils.replaceFragmentNoAnimation(R.id.page_container, supportFragmentManager, youtubeFragment)
            2 -> ActivityUtils.replaceFragmentNoAnimation(R.id.page_container, supportFragmentManager, weiboFragment)
            3 -> ActivityUtils.replaceFragmentNoAnimation(R.id.page_container, supportFragmentManager, rtmpFragment)
        }
    }
}
