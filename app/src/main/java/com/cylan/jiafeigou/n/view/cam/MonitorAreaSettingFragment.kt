package com.cylan.jiafeigou.n.view.cam

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import butterknife.OnClick
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.module.GlideApp
import com.cylan.jiafeigou.n.mvp.contract.cam.MonitorAreaSettingContact
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.PreferencesUtils
import com.cylan.jiafeigou.utils.ToastUtil
import com.cylan.jiafeigou.utils.ViewUtils
import com.cylan.jiafeigou.widget.crop.Shaper
import kotlinx.android.synthetic.main.fragment_monitor_area_setting.*

/**
 * Created by yanzhendong on 2017/11/15.
 */
class MonitorAreaSettingFragment : BaseFragment<MonitorAreaSettingContact.Presenter>(), MonitorAreaSettingContact.View {


    private var monitorWidth: Int = 0
    private var monitorHeight: Int = 0
    private var monitorAreaArray = mutableListOf<DpMsgDefine.Rect4F>()
    private var restoreMonitorLayout: Boolean = true
    private var localPictureLoadActionCompleted: Boolean = false
    private var remotePictureLoadActionCompleted: Boolean = false
    private var isMotionAreaSettingCompleted: Boolean = false
    private var pictureRadio: Float = 0F
    private var screenRadio: Float = 0F
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var monitorPictureWidth: Int = 0
    private var monitorPictureHeight: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_monitor_area_setting, container, false)
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        effect_container.keepScreenOn = true
        effect_container.setSizeUpdateListener(this::onMonitorAreaChanged)
        effect_container.setOnSystemUiVisibilityChangeListener { ViewUtils.setSystemUiVisibility(monitor_picture, false) }
        monitorWidth = context!!.resources.getDimensionPixelSize(R.dimen.y206)
        monitorHeight = context!!.resources.getDimensionPixelSize(R.dimen.y136)
    }

    override fun onStart() {
        super.onStart()
        ViewUtils.setRequestedOrientation(activity as Activity, ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE)
        ViewUtils.setSystemUiVisibility(monitor_picture, false)
        if (!isMotionAreaSettingCompleted) {
            presenter.loadMonitorAreaSetting()
        }
    }

    private fun onMonitorAreaChanged(shaper: Shaper?, width: Int, height: Int) {
        AppLogger.w("onMonitorAreaChanged:width:$width,height:$height")
    }

    override fun onGetMonitorPictureSuccess(url: String) {
        AppLogger.w("onGetMonitorPictureSuccess:$url")
        isMotionAreaSettingCompleted = true
        tryGetMonitorPicture(url)
    }


    override fun onGetMonitorPictureError() {
        AppLogger.w("onGetMonitorPictureError")
        isMotionAreaSettingCompleted = true
        remotePictureLoadActionCompleted = true
        hideLoadingBar()
        alertErrorGetMonitorPicture()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val metrics = Resources.getSystem().displayMetrics
            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
            screenRadio = screenHeight.toFloat() / screenWidth.toFloat()
            updateMonitorAreaRadio()
        }
    }

    override fun tryGetLocalMonitorPicture() {
        val localUrl: String? = PreferencesUtils.getString(JConstant.MONITOR_AREA_PICTURE + ":$uuid")
        localUrl?.apply {
            GlideApp.with(this@MonitorAreaSettingFragment)
                    .asBitmap()
                    .load(this)
                    .onlyRetrieveFromCache(true)
                    .skipMemoryCache(true)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
                            resource?.apply {
                                localPictureLoadActionCompleted = true
                                updateMonitorAreaPicture(this)
                                if (monitorAreaArray.size > 0) {
                                    //说明区域数据已经先回来了
                                    toggleMonitorAreaMode(false)
                                }
                            }
                        }
                    })
        }
    }


    private fun tryGetMonitorPicture(url: String) {
        GlideApp.with(this)
                .asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
                        if (resource != null) {
                            remotePictureLoadActionCompleted = true
                            AppLogger.w("设置区域设置图片")
                            PreferencesUtils.putString(JConstant.MONITOR_AREA_PICTURE + ":$uuid", url)
                            updateMonitorAreaPicture(resource)
                            toggleMonitorAreaMode(false)
                            finish.isEnabled = true;
                            hideLoadingBar()
                        }
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        remotePictureLoadActionCompleted = true
                        alertErrorGetMonitorPicture()
                    }
                })
    }

    private fun alertErrorGetMonitorPicture() {
        finish.isEnabled = localPictureLoadActionCompleted || remotePictureLoadActionCompleted
        if (localPictureLoadActionCompleted || remotePictureLoadActionCompleted) {
            ToastUtil.showToast(getString(R.string.DETECTION_AREA_FAILED_LOAD))
        } else {
            AlertDialog.Builder(context!!)
                    .setMessage(R.string.DETECTION_AREA_FAILED_LOAD_RETRY)
                    .setCancelable(false)
                    .setPositiveButton(R.string.WELL_OK, { _, _ -> exitToParent() })
                    .create()
                    .show()
        }
    }

    //radio高宽比
    fun updateMonitorAreaRadio() {
        val radio = Math.min(pictureRadio, screenRadio)
        monitorPictureWidth = (screenHeight / radio).toInt()
        monitorPictureHeight = (screenWidth * radio).toInt()
        val params = monitor_container.layoutParams
        params?.width = monitorPictureWidth
        params?.height = monitorPictureHeight
        monitor_container?.layoutParams = params
        restoreMonitorAreaIfNeeded()
    }

    fun updateMonitorAreaPicture(drawable: Bitmap) {
        monitor_picture.setImageBitmap(drawable)
        pictureRadio = drawable.height.toFloat() / drawable.width.toFloat()
        updateMonitorAreaRadio()
    }

    private fun toggleMonitorAreaMode(restore: Boolean) {
        this.restoreMonitorLayout = restore
        if (restoreMonitorLayout) {
            monitor_toggle.visibility = View.GONE
            drag_and_drop.visibility = View.VISIBLE
            pop_tips.visibility = View.GONE
            effect_view.visibility = View.GONE
        } else {
            monitor_toggle.visibility = View.VISIBLE
            drag_and_drop.visibility = View.GONE
            effect_view.visibility = View.VISIBLE
            restoreMonitorAreaIfNeeded()
            decideShowPopTips()
        }
    }

    private fun restoreMonitorAreaIfNeeded() {
        if (monitorAreaArray.size > 0) {
            val rect4F = monitorAreaArray[0]
            val layoutParams = effect_view.layoutParams as FrameLayout.LayoutParams
            layoutParams.gravity = Gravity.NO_GRAVITY
            layoutParams.setMargins((monitorPictureWidth * rect4F.left).toInt(), (monitorPictureHeight * rect4F.top).toInt(), 0, 0)
            layoutParams.width = Math.max((monitorPictureWidth * (rect4F.right - rect4F.left)).toInt(), effect_view.minimumWidth)
            layoutParams.height = Math.max((monitorPictureHeight * (rect4F.bottom - rect4F.top)).toInt(), effect_view.minimumHeight)
            effect_view.layoutParams = layoutParams
            AppLogger.w("区域侦测:恢复区域为:$rect4F,width:${layoutParams.width},height:${layoutParams.height},container width:$monitorPictureWidth,container height:$monitorPictureHeight")
        }
    }

    private fun decideShowPopTips() {
        var showPopTips = PreferencesUtils.getBoolean(JConstant.SHOW_MONITOR_AREA_TIPS, true)
        if (showPopTips) {
            PreferencesUtils.putBoolean(JConstant.SHOW_MONITOR_AREA_TIPS, false)
            pop_tips.visibility = View.VISIBLE
        }
    }

    @OnClick(R.id.pop_tips)
    fun clickedPopTips() {
        AppLogger.w("点击了 pop tips")
        pop_tips.visibility = View.GONE
    }

    override fun performBackIntercept(willExit: Boolean): Boolean {
        ViewUtils.setRequestedOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        return super.performBackIntercept(willExit)
    }

    @OnClick(R.id.back)
    fun exitToParent() {
        AppLogger.w("点击了返回按钮")
        back.post { ViewUtils.setRequestedOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) }
        back.post { fragmentManager?.popBackStack() }
    }

    @OnClick(R.id.monitor_toggle)
    fun clickedToggleMonitor() {
        AppLogger.w("点击了切换")
        toggleMonitorAreaMode(true)
    }

    @OnClick(R.id.drag_and_drop)
    fun clickedDropAndDrag() {
        AppLogger.w("点击了进入选择侦测区域按钮")
        toggleMonitorAreaMode(false)
    }

    @OnClick(R.id.finish)
    fun clickedFinish() {
        AppLogger.w("点击了完成按钮")
        val rects = effect_container.motionArea.map { DpMsgDefine.Rect4F(it[0], it[1], it[2], it[3]) }
        AppLogger.w("区域侦测:选择区域为:$rects")
        monitorAreaArray.clear()
        monitorAreaArray.addAll(rects)
        presenter.setMonitorArea(uuid, !restoreMonitorLayout, monitorAreaArray)
    }

    override fun onSetMonitorAreaSuccess() {
        AppLogger.w("设置侦测区域成功了")
        ToastUtil.showToast(getString(R.string.PWD_OK_2))
        callBack?.callBack(null)
        exitToParent()
    }

    override fun onSetMonitorAreaError() {
        AppLogger.w("设置侦测区域失败了")
        ToastUtil.showToast(getString(R.string.SETTINGS_FAILED))
    }

    override fun onRestoreMonitorAreaSetting(rects: List<DpMsgDefine.Rect4F>) {
        AppLogger.w("onRestoreMonitorAreaSetting:$rects")
        isMotionAreaSettingCompleted = true
        monitorAreaArray.clear()
        monitorAreaArray.addAll(rects)
        if (remotePictureLoadActionCompleted) {
            toggleMonitorAreaMode(false)
        }
    }

    override fun onRestoreDefaultMonitorAreaSetting() {
        AppLogger.w("onRestoreDefaultMonitorAreaSetting")
        isMotionAreaSettingCompleted = true
        if (remotePictureLoadActionCompleted) {
            toggleMonitorAreaMode(true)
        }
    }

    override fun onLoadMotionAreaSettingFinished(remoteURL: String?, motionAreaSetting: List<DpMsgDefine.Rect4F>?) {

    }

    override fun showLoadingBar() {
        load_bar.visibility = View.VISIBLE
    }

    override fun hideLoadingBar() {
        load_bar.visibility = View.GONE
    }

    companion object {
        @JvmStatic
        fun newInstance(uuid: String): MonitorAreaSettingFragment {
            val fragment = MonitorAreaSettingFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = argument
            return fragment
        }
    }
}