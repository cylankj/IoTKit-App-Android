package com.cylan.jiafeigou.n.view.panorama

import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.support.log.AppLogger
import kotlinx.android.synthetic.main.fragment_live_permission.*

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
        fragment_facebook_live_permission.setOnCheckedChangeListener { _, checkId ->
            this.checkId = checkId
        }
    }

    var checkId: Int = -1


    override fun onDetach() {
        super.onDetach()
        if (callBack != null) {
//            when (checkId) {
//                R.id.live_permission_public -> {
//                }
//                R.id.live_permission_friends -> {
//
//                }
//                R.id.live_permission_only_me -> {
//
//                }
//                R.id.live_permission_close_friends -> {
//
//                }
//                R.id.live_permission_acquaintance -> {
//
//                }
//                R.id.live_permission_family -> {
//
//                }
//                R.id.live_permission_good_friends -> {
//
//                }
//
//            }
            AppLogger.w("$checkId")
//            callBack.callBack()
        }

    }

    companion object {

        const val FACEBOOK_PERMISSION_PUBLIC = 0
        const val FACEBOOK_PERMISSION_FRIENDS = 1
        const val FACEBOOK_PERMISSION_ONLY_ME = 2
        const val FACEBOOK_PERMISSION_CLOSE_FRIENDS = 3
        const val FACEBOOK_PERMISSION_ACQUAINTANCE = 4
        const val FACEBOOK_PERMISSION_FAMILY = 5
        const val FACEBOOK_PERMISSION_GOOD_FRIENDS = 6

        fun newInstance(): FacebookLivePermissionFragment {
            val fragment = FacebookLivePermissionFragment()
            R.id.live_permission_close_friends
            return fragment
        }
    }
}