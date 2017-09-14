package com.cylan.jiafeigou.n.view.panorama

import android.content.Intent
import android.text.TextUtils
import com.androidkun.xtablayout.XTabLayout
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.ActivityComponent
import com.cylan.jiafeigou.base.wrapper.BaseActivity
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ActivityUtils
import com.cylan.jiafeigou.utils.IMEUtils
import com.cylan.jiafeigou.utils.PreferencesUtils
import com.cylan.jiafeigou.utils.ToastUtil
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
//                PreferencesUtils.getString(JConstant.YOUTUBE_PREF_CONFIGURE, null)
                finish()
            }
            2 -> {
                //weibo
                PreferencesUtils.putString(JConstant.WEIBO_PREF_DESCRIPTION, weiboFragment.getWeiboLiveDescription())
                finish()
            }
            3 -> {
                //rtmp
                val rtmpServer = rtmpFragment.getRtmpServer()
                val secretKey = rtmpFragment.getRtmpSecretKey()
                if (TextUtils.isEmpty(rtmpServer) || "rtmp://" == rtmpServer) {
                    AppLogger.w("非法的 rtmp 地址")
                    ToastUtil.showToast(getString(R.string.RTMP_EMPTY_TIPS))
                } else if (!rtmpServer.startsWith("rtmp://")) {
                    ToastUtil.showToast(getString(R.string.RTMP_ERROR_TIPS))
                } else {
                    if (rtmpServer.endsWith("/")) {
                        PreferencesUtils.putString(JConstant.RTMP_PREF_CONFIGURE, "$rtmpServer$secretKey")
                    } else {
                        PreferencesUtils.putString(JConstant.RTMP_PREF_CONFIGURE, "$rtmpServer/$secretKey")
                    }
                    finish()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        IMEUtils.hide(this)
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
