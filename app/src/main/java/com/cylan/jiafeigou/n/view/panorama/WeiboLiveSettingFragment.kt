package com.cylan.jiafeigou.n.view.panorama

import android.os.Bundle
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.view.JFGView
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.misc.JConstant

/**
 * Created by yanzhendong on 2017/9/7.
 */
class WeiboLiveSettingFragment : BaseFragment<BasePresenter<JFGView>>() {
    override fun setFragmentComponent(fragmentComponent: FragmentComponent?) {

    }

    override fun getContentViewID(): Int {
        return R.layout.layout_weibo
    }

    companion object {
        fun newInstance(uuid: String): WeiboLiveSettingFragment {
            val fragment = WeiboLiveSettingFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = argument
            return fragment
        }
    }
}