package com.cylan.jiafeigou.n.view.panorama

import android.content.Intent
import com.androidkun.xtablayout.XTabLayout
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.ActivityComponent
import com.cylan.jiafeigou.base.wrapper.BaseActivity
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.utils.ActivityUtils
import com.cylan.jiafeigou.utils.PreferencesUtils
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
        val position = PreferencesUtils.getInt(JConstant.LIVE_PLATFORM_KEY, 0)
//        switchRtmpPage(position)
        rtmp_type_tabs.getTabAt(position)!!.select()
        /*这里可能以后又会有一大堆判断,返回键*/
        custom_toolbar.setBackAction { onBackPressed() }
        custom_toolbar.setRightAction { saveLiveConfigure() }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun saveLiveConfigure() {
        PreferencesUtils.putInt(JConstant.LIVE_PLATFORM_KEY, rtmp_type_tabs.selectedTabPosition)

        when (rtmp_type_tabs.selectedTabPosition) {

            0 -> {
                //facebook
                PreferencesUtils.getString(JConstant.FACEBOOK_PREF_CONFIGURE, null)
                //todo
                finish()
            }
            1 -> {
                //youtube
                PreferencesUtils.getString(JConstant.YOUTUBE_PREF_CONFIGURE, null)
            }
            2 -> {
                //weibo

            }
            3 -> {
                //rtmp
//               rtmpFragment.
            }
        }
        finish()
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
            else -> ActivityUtils.replaceFragmentNoAnimation(R.id.page_container, supportFragmentManager, facebookFragment)
        }
    }
}
