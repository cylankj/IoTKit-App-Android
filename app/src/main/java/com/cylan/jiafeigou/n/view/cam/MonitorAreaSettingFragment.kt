package com.cylan.jiafeigou.n.view.cam

import android.app.Activity
import android.content.pm.ActivityInfo
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
import com.cylan.jiafeigou.BuildConfig
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
    private var enable: Boolean = false
    private var monitorWidth: Int = 0
    private var monitorHeight: Int = 0
    private var monitorAreaArray = mutableListOf<DpMsgDefine.Rect4F>()
    private var isLocalLoadSuccess: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_monitor_area_setting, container, false)
    }


    override fun initViewAndListener() {
        super.initViewAndListener()
        ViewUtils.setRequestedOrientation(activity as Activity, ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE)
        effect_container.setSizeUpdateListener(this::onMonitorAreaChanged)
        effect_container.setOnSystemUiVisibilityChangeListener { ViewUtils.setSystemUiVisibility(monitor_picture, false) }
        monitorWidth = context.resources.getDimensionPixelSize(R.dimen.y206)
        monitorHeight = context.resources.getDimensionPixelSize(R.dimen.y136)
        presenter.loadMonitorAreaSetting()
    }

    override fun onStart() {
        super.onStart()
        ViewUtils.setSystemUiVisibility(monitor_picture, false)
    }

    private fun onMonitorAreaChanged(shaper: Shaper?, width: Int, height: Int) {
        AppLogger.w("onMonitorAreaChanged:width:$width,height:$height")
        val hintView = shaper?.shaper?.findViewById(R.id.effect_hint)
        if (hintView != null) {
            //长和宽 任何一个小于默认尺寸,则隐藏提示文字
            hintView.visibility = if (width >= monitorWidth && height >= monitorHeight) View.VISIBLE else View.GONE
        }
    }

    override fun onGetMonitorPictureSuccess(url: String) {
        AppLogger.w("onGetMonitorPictureSuccess:$url")
        tryGetMonitorPicture(url)
    }


    override fun onGetMonitorPictureError() {
        AppLogger.w("onGetMonitorPictureError")
        finish.isEnabled = false
        hideLoadingBar()
        alertErrorGetMonitorPicture(!isLocalLoadSuccess)
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
                                isLocalLoadSuccess = true
                                updateMonitorAreaPicture(this)
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
                            AppLogger.w("设置区域设置图片")
                            PreferencesUtils.putString(JConstant.MONITOR_AREA_PICTURE + ":$uuid", url)
                            hideLoadingBar()
                            updateMonitorAreaPicture(resource)
                            effect_container.post { toggleMonitorAreaMode(enable) }
                        }
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        hideLoadingBar()
                        alertErrorGetMonitorPicture(!isLocalLoadSuccess)
                    }

                })
    }

    private fun alertErrorGetMonitorPicture(focusToExit: Boolean) {
        if (focusToExit) {
            AlertDialog.Builder(context)
                    .setMessage(R.string.DETECTION_AREA_FAILED_LOAD_RETRY)
                    .setCancelable(false)
                    .setPositiveButton(R.string.WELL_OK, { _, _ -> exitToParent() })
                    .create()
                    .show()
        } else {
            ToastUtil.showToast(getString(R.string.DETECTION_AREA_FAILED_LOAD))
        }
    }

    fun updateMonitorAreaPicture(drawable: Bitmap) {
        monitor_picture.setImageBitmap(drawable)
        val params = monitor_picture.layoutParams
        var pictureRadio: Float = drawable.height.toFloat() / drawable.width.toFloat()
        val metrics = Resources.getSystem().displayMetrics
        var screenRadio: Float = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
        if (pictureRadio >= screenRadio) {
            //图片的高宽比 大于 屏幕的 高宽比 则此时需要高度铺满屏幕高度,宽度计算出来
            params.height = metrics.heightPixels
            params.width = (metrics.heightPixels.toFloat() / pictureRadio).toInt()
        } else {
            //图片的高宽比小于屏幕的高宽比 则此时需要宽度铺满屏幕宽度,高度计算出来
            params.width = metrics.widthPixels
            params.height = (metrics.heightPixels.toFloat() * pictureRadio).toInt()
        }
        monitor_picture.post { monitor_picture.layoutParams = params }
    }

    fun toggleMonitorAreaMode(readyToSelect: Boolean) {
        load_bar.visibility = View.GONE
        finish.isEnabled = true
        enable = readyToSelect
        if (readyToSelect) {
            effect_container.removeAllViews()
            val effectView = LayoutInflater.from(context).inflate(R.layout.layout_motion_shaper, effect_container, false)
            restoreMonitorAreaIfNeeded(effectView)
            monitor_toggle.visibility = View.VISIBLE
            drag_and_drop.visibility = View.GONE
            decideShowPopTips()
        } else {
            monitor_toggle.visibility = View.GONE
            drag_and_drop.visibility = View.VISIBLE
            pop_tips.visibility = View.GONE
            effect_container.removeAllViews()
        }
    }

    private fun restoreMonitorAreaIfNeeded(effectView: View) {
        if (monitorAreaArray.size > 0) {
            val rect4F = monitorAreaArray[0]
            val width = effect_container.width.toFloat()
            val height = effect_container.height.toFloat()
            val layoutParams = effectView.layoutParams as FrameLayout.LayoutParams
            layoutParams.gravity = Gravity.NO_GRAVITY
            layoutParams.setMargins((width * rect4F.left).toInt(), (height * rect4F.top).toInt(), 0, 0)
            layoutParams.width = Math.max((width * (rect4F.right - rect4F.left)).toInt(), effectView.minimumWidth)
            layoutParams.height = Math.max((height * (rect4F.bottom - rect4F.top)).toInt(), effectView.minimumHeight)
            AppLogger.w("区域侦测:恢复区域为:$rect4F,width:${layoutParams.width},height:${layoutParams.height},container width:$width,container height:$height")
            if (layoutParams.width < monitorWidth || layoutParams.height < monitorHeight) {
                effectView.findViewById(R.id.effect_hint).visibility = View.GONE
            }
            effectView.layoutParams = layoutParams
        }
        effect_container.addView(effectView)
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
        back.post { fragmentManager.popBackStack() }

    }

    @OnClick(R.id.monitor_toggle)
    fun clickedToggleMonitor() {
        AppLogger.w("点击了切换")
        toggleMonitorAreaMode(false)
    }

    @OnClick(R.id.drag_and_drop)
    fun clickedDropAndDrag() {
        AppLogger.w("点击了进入选择侦测区域按钮")
        toggleMonitorAreaMode(true)
    }

    @OnClick(R.id.finish)
    fun clickedFinish() {
        AppLogger.w("点击了完成按钮")
        val rects = effect_container.motionArea.map { DpMsgDefine.Rect4F(it[0], it[1], it[2], it[3]) }
        AppLogger.w("区域侦测:选择区域为:$rects")
        monitorAreaArray.clear()
        monitorAreaArray.addAll(rects)
        presenter.setMonitorArea(uuid, enable, monitorAreaArray)
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
        monitorAreaArray.clear()
        monitorAreaArray.addAll(rects)
        if (monitorAreaArray.isNotEmpty()) {
            enable = true
        }
    }

    override fun onRestoreDefaultMonitorAreaSetting() {
        AppLogger.w("onRestoreDefaultMonitorAreaSetting")
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