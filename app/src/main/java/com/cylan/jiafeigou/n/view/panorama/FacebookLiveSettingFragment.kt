package com.cylan.jiafeigou.n.view.panorama

import android.os.Bundle
import butterknife.OnClick
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.view.JFGView
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.utils.ActivityUtils

/**
 * Created by yanzhendong on 2017/9/7.
 */
class FacebookLiveSettingFragment : BaseFragment<BasePresenter<JFGView>>() {
    override fun setFragmentComponent(fragmentComponent: FragmentComponent?) {
    }

    override fun getContentViewID(): Int {
        return R.layout.layout_facebook
    }

    @OnClick(R.id.facebook_permission)
    fun setFacebookPermission() {
        val instance = FacebookLivePermissionFragment.newInstance()
        instance.setCallBack {

        }
        ActivityUtils.addFragmentSlideInFromRight(fragmentManager, instance, android.R.id.content)
    }

    companion object {
        fun newInstance(uuid: String): FacebookLiveSettingFragment {
            val fragment = FacebookLiveSettingFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = argument
            return fragment
        }
    }
}