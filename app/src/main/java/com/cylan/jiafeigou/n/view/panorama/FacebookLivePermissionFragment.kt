package com.cylan.jiafeigou.n.view.panorama

import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import kotlinx.android.synthetic.main.activity_live_setting.*

/**
 * Created by yanzhendong on 2017/9/5.
 */
class FacebookLivePermissionFragment : BaseFragment<LivePremissionContract.Presenter>(), LivePremissionContract.View {
    override fun setFragmentComponent(fragmentComponent: FragmentComponent?) {

    }

    override fun getContentViewID(): Int {
        return R.layout.fragment_live_permission
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        custom_toolbar.setBackAction { activity.onBackPressed() }
    }


    companion object {
        fun newInstance(): FacebookLivePermissionFragment {
            val fragment = FacebookLivePermissionFragment()
            R.id.live_permission_close_friends
            return fragment
        }
    }
}