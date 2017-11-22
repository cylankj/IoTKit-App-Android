package com.cylan.jiafeigou.n.view.cam

import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.widget.PopupWindowCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import butterknife.OnClick
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.module.GlideApp
import com.cylan.jiafeigou.n.mvp.contract.cam.MonitorAreaSettingContact
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.PreferencesUtils
import com.cylan.jiafeigou.utils.ToastUtil
import com.cylan.jiafeigou.utils.ViewUtils
import com.cylan.jiafeigou.widget.crop.CropLayout
import kotlinx.android.synthetic.main.fragment_monitor_area_setting.*
import java.util.*

/**
 * Created by yanzhendong on 2017/11/15.
 */
class MonitorAreaSettingFragment : BaseFragment<MonitorAreaSettingContact.Presenter>(), MonitorAreaSettingContact.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewUtils.setRequestedOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_monitor_area_setting, container, false)
    }


    override fun initViewAndListener() {
        super.initViewAndListener()
        ViewUtils.setSystemUiVisibility(monitor_picture, false)
        effect_container.setSizeUpdateListener(this::onMonitorAreaChanged)
        effect_container.setOnSystemUiVisibilityChangeListener { ViewUtils.setSystemUiVisibility(monitor_picture, false) }
    }


    override fun onStart() {
        super.onStart()
        presenter.loadMonitorPicture()
    }

    private fun onMonitorAreaChanged(layout: CropLayout, width: Int, height: Int) {
        AppLogger.w("onMonitorAreaChanged:width:$width,height:$height")
        if (width < layout.monitorAreaWidth || height < layout.monitorAreaHeight) {
            effect_hint.visibility = View.GONE
        }
    }

    override fun onGetMonitorPictureSuccess(url: String) {
        AppLogger.w("onGetMonitorPictureSuccess:$url")
        GlideApp.with(this)
                .load(url)
                .placeholder(R.drawable.default_diagram_mask)
                .error(R.drawable.default_diagram_mask)
                .into(object : DrawableImageViewTarget(monitor_picture, true) {
                    override fun setResource(resource: Drawable?) {
                        if (resource != null) {
                            AppLogger.w("设置区域设置图片")
                            updateMonitorAreaPicture(resource)
                            updateViewVisibility(true)
                        }
                    }
                })
    }

    override fun onGetMonitorPictureError() {
        AppLogger.w("onGetMonitorPictureError")
        ToastUtil.showToast(getString(R.string.DETECTION_AREA_FAILED_LOAD))
    }

    fun updateMonitorAreaPicture(drawable: Drawable) {
        monitor_picture.setImageDrawable(drawable)
        val params = monitor_picture.layoutParams
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        monitor_picture.layoutParams = params
    }

    fun updateViewVisibility(ready: Boolean) {
        monitor_toggle.visibility = if (ready) View.VISIBLE else View.INVISIBLE
        decideShowPopTips()
    }

    private fun decideShowPopTips() {
        val showPopTips = PreferencesUtils.getBoolean(JConstant.SHOW_MONITOR_AREA_TIPS, true)
        if (showPopTips) {
            PreferencesUtils.putBoolean(JConstant.SHOW_MONITOR_AREA_TIPS, false)
            val popTips = PopupWindow()
            PopupWindowCompat.showAsDropDown(popTips, monitor_toggle, 0, 0, Gravity.TOP or Gravity.END)
        }
    }

    override fun performBackIntercept(willExit: Boolean): Boolean {
        ViewUtils.setRequestedOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        return super.performBackIntercept(willExit)
    }

    @OnClick(R.id.back)
    fun clickedBack() {
        AppLogger.w("点击了返回按钮")
        ViewUtils.setRequestedOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        fragmentManager.popBackStack()
    }

    @OnClick(R.id.monitor_toggle)
    fun clickedToggleMonitor() {
        AppLogger.w("点击了切换")
        if (effect_view.visibility == View.VISIBLE) {
            effect_view.visibility = View.GONE
        } else {
            val layoutParams = effect_view.layoutParams as RelativeLayout.LayoutParams
            layoutParams.width = effect_container.monitorAreaWidth
            layoutParams.height = effect_container.monitorAreaHeight
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
            effect_view.layoutParams = layoutParams
            effect_view.visibility = View.VISIBLE
            effect_hint.visibility = View.VISIBLE
        }
    }

    @OnClick(R.id.finish)
    fun clickedFinish() {
        AppLogger.w("点击了完成按钮")
        val monitorArea = FloatArray(4)
        effect_container.getMotionArea(monitorArea)
        AppLogger.w("选择区域为:" + Arrays.toString(monitorArea))
    }

    override fun showLoadingBar() {
        load_bar.visibility = View.VISIBLE
    }

    override fun hideLoadingBar() {
        load_bar.visibility = View.GONE
    }

    companion object {
        fun newInstance(uuid: String): MonitorAreaSettingFragment {
            val fragment = MonitorAreaSettingFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = argument
            return fragment
        }
    }
}