package com.cylan.jiafeigou.n.view.panorama

import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.utils.PreferencesUtils
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

    private var permission: String? = null
        set(value) {
            field = value
            when (field) {
                FACEBOOK_PERMISSION.EVERYONE.name -> {
                    fragment_facebook_live_permission.check(R.id.live_permission_public)
                }
                FACEBOOK_PERMISSION.ALL_FRIENDS.name -> {
                    fragment_facebook_live_permission.check(R.id.live_permission_friends)
                }
                FACEBOOK_PERMISSION.FRIENDS_OF_FRIENDS.name -> {
                    fragment_facebook_live_permission.check(R.id.live_permission_friends_of_friends)
                }
                FACEBOOK_PERMISSION.SELF.name -> {
                    fragment_facebook_live_permission.check(R.id.live_permission_only_me)
                }
            }
        }

    override fun initViewAndListener() {
        super.initViewAndListener()
        custom_toolbar.setBackAction { activity.onBackPressed() }
        initSelectedPermission()
        fragment_facebook_live_permission.setOnCheckedChangeListener { _, checkId ->
            when (checkId) {
                R.id.live_permission_friends_of_friends -> {
                    permission = FACEBOOK_PERMISSION.FRIENDS_OF_FRIENDS.name
//                    PreferencesUtils.putString(JConstant.FACEBOOK_PREF_PERMISSION_KEY + ":" + uuid, FACEBOOK_PERMISSION.FRIENDS_OF_FRIENDS.name)
                }
                R.id.live_permission_friends -> {
                    permission = FACEBOOK_PERMISSION.ALL_FRIENDS.name
//                    PreferencesUtils.putString(JConstant.FACEBOOK_PREF_PERMISSION_KEY + ":" + uuid, FACEBOOK_PERMISSION.ALL_FRIENDS.name)
                }
                R.id.live_permission_only_me -> {
                    permission = FACEBOOK_PERMISSION.SELF.name
//                    PreferencesUtils.putString(JConstant.FACEBOOK_PREF_PERMISSION_KEY + ":" + uuid, FACEBOOK_PERMISSION.SELF.name)
                }
                R.id.live_permission_public -> {
                    permission = FACEBOOK_PERMISSION.EVERYONE.name
//                    PreferencesUtils.putString(JConstant.FACEBOOK_PREF_PERMISSION_KEY + ":" + uuid, FACEBOOK_PERMISSION.EVERYONE.name)
                }
            }
        }
    }

    private fun initSelectedPermission() {
        permission = PreferencesUtils.getString(JConstant.FACEBOOK_PREF_PERMISSION_KEY + ":" + uuid, FACEBOOK_PERMISSION.EVERYONE.name)
    }


    override fun onDetach() {
        super.onDetach()
        callBack.callBack(permission)
    }


    companion object {
        enum class FACEBOOK_PERMISSION { EVERYONE, ALL_FRIENDS, FRIENDS_OF_FRIENDS, SELF }

        fun newInstance(): FacebookLivePermissionFragment {
            val fragment = FacebookLivePermissionFragment()
            return fragment
        }
    }
}