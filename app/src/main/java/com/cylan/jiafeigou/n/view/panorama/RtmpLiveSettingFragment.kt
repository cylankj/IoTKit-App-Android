package com.cylan.jiafeigou.n.view.panorama

import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.CompoundButton
import android.widget.ScrollView
import butterknife.OnCheckedChanged
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.view.JFGView
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.softkeyboard.util.KPSwitchConflictUtil
import com.cylan.jiafeigou.support.softkeyboard.util.KeyboardUtil
import com.cylan.jiafeigou.utils.ViewUtils
import kotlinx.android.synthetic.main.activity_live_setting.*
import kotlinx.android.synthetic.main.layout_rtmp.*


/**
 * Created by yanzhendong on 2017/9/7.
 */
class RtmpLiveSettingFragment : BaseFragment<BasePresenter<JFGView>>() {


    override fun getContentViewID(): Int {
        return R.layout.layout_rtmp
    }

    override fun initViewAndListener() {
        super.initViewAndListener()

    }

    @OnCheckedChanged(R.id.rtmp_password_show)
    fun showOrHidePassword(buttonView: CompoundButton, isChecked: Boolean) {
        ViewUtils.showPwd(rtmp_et_miyao, isChecked)
        rtmp_et_miyao.setSelection(rtmp_et_miyao.length())
    }

    fun getRtmpServer(): String {
        return setting_rtmp_host.text.trim().toString()
    }

    fun getRtmpSecretKey(): String {
        return rtmp_et_miyao.text.trim().toString()
    }

    override fun onStart() {
        super.onStart()
        initKeyBoard()
    }

    override fun onStop() {
        super.onStop()
        KeyboardUtil.detach(activity, listener)
    }

    private lateinit var listener: ViewTreeObserver.OnGlobalLayoutListener

    private fun initKeyBoard() {
        listener = KeyboardUtil.attach(activity, activity.panel_root, { isShowing ->
            if (isShowing) {
                val focused = rtmp_et_miyao.isFocused
                activity.live_setting_scroller.fullScroll(ScrollView.FOCUS_DOWN)
                if (focused) {
                    rtmp_et_miyao.post { rtmp_et_miyao.requestFocus() }
                }

            }
        })
        KPSwitchConflictUtil.attach(activity.panel_root, rtmp_et_miyao)
    }

    companion object {
        fun newInstance(uuid: String): RtmpLiveSettingFragment {
            val fragment = RtmpLiveSettingFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = argument
            return fragment
        }
    }
}