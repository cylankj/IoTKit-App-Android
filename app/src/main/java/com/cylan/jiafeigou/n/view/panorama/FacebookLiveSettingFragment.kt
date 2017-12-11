package com.cylan.jiafeigou.n.view.panorama

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.view.JFGView
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.module.LoginHelper
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ActivityUtils
import com.cylan.jiafeigou.utils.PreferencesUtils
import com.google.gson.Gson
import com.umeng.facebook.AccessToken
import com.umeng.socialize.UMAuthListener
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.bean.SHARE_MEDIA
import kotlinx.android.synthetic.main.layout_facebook.*

/**
 * Created by yanzhendong on 2017/9/7.
 */
class FacebookLiveSettingFragment : BaseFragment<BasePresenter<JFGView>>(), UMAuthListener {
    override fun onStart(p0: SHARE_MEDIA?) {

    }

    override fun onComplete(p0: SHARE_MEDIA?, p1: Int, p2: MutableMap<String, String>?) {
        if (p1 == UMAuthListener.ACTION_GET_PROFILE) {
            AppLogger.w("p1 is:$p1,${Gson().toJson(p2)}")
            PreferencesUtils.putString(JConstant.OPEN_LOGIN_MAP + SHARE_MEDIA.FACEBOOK.toString(), Gson().toJson(p2))
            account = p2!!["name"]
        } else if (p1 == UMAuthListener.ACTION_DELETE) {
            PreferencesUtils.remove(JConstant.OPEN_LOGIN_MAP + SHARE_MEDIA.FACEBOOK.toString())
        }
    }

    override fun onCancel(p0: SHARE_MEDIA?, p1: Int) {
    }

    override fun onError(p0: SHARE_MEDIA?, p1: Int, p2: Throwable?) {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_facebook, container, false)
    }

    private var account: String? = null
        set(value) {
            field = value
            if (TextUtils.isEmpty(field)) {
                setting_facebook_account_item.subTitle = getString(R.string.LIVE_ACCOUNT_UNBOUND)
                setting_facebook_account_item.showDivider(false)
                setting_facebook_permission.visibility = View.GONE
            } else {
                setting_facebook_account_item.subTitle = field
                setting_facebook_account_item.showDivider(false)
                setting_facebook_permission.visibility = View.VISIBLE
            }
        }

    // enum{'EVERYONE', 'ALL_FRIENDS', 'FRIENDS_OF_FRIENDS', 'SELF'}

    private var permission: String = "EVERYONE"
        set(value) {
            field = value
            when (field) {
                "EVERYONE" -> {//公开
                    setting_facebook_permission.subTitle = getString(R.string.FACEBOOK_PERMISSIONS_PUBLIC)
                }
                "ALL_FRIENDS" -> {//好友
                    setting_facebook_permission.subTitle = getString(R.string.FACEBOOK_PERMISSIONS_FRIENDS)
                }
                "FRIENDS_OF_FRIENDS" -> {//朋友的朋友
                    setting_facebook_permission.subTitle = getString(R.string.FACEBOOK_PERMISSIONS_FRIENDS_OF_FRIENDS)
                }
                "SELF" -> {//仅自己
                    setting_facebook_permission.subTitle = getString(R.string.FACEBOOK_PERMISSIONS_ONLYME)
                }
                else -> {

                }
            }
        }


    override fun initViewAndListener() {
        super.initViewAndListener()
        setting_facebook_account_item.title = getString(R.string.LIVE_ACCOUNT, getString(R.string.LIVE_PLATFORM_FACEBOOK))

        val map = PreferencesUtils.getString(JConstant.OPEN_LOGIN_MAP + SHARE_MEDIA.FACEBOOK.toString(), null)
        if (map != null) {
            val fromJson = Gson().fromJson(map, Map::class.java)
            account = fromJson["name"] as String
        }

    }

    override fun onStart() {
        super.onStart()
        account = account
        permission = PreferencesUtils.getString(JConstant.FACEBOOK_PREF_PERMISSION_KEY + ":" + uuid, "EVERYONE")
    }

    fun getFacebookDescription(): String {
        val text = setting_facebook_description.text
        return if (TextUtils.isEmpty(text)) setting_facebook_description.hint.trim().toString() else text.trim().toString()
    }

    @OnClick(R.id.setting_facebook_permission)
    fun setFacebookPermission() {
        val instance = FacebookLivePermissionFragment.newInstance()
        instance.setCallBack {
            permission = it as? String ?: "EVERYONE"
        }
        ActivityUtils.addFragmentSlideInFromRight(fragmentManager, instance, android.R.id.content)
    }

    @OnClick(R.id.setting_facebook_account_item)
    fun selectAccount() {
        when {
            LoginHelper.loginType == 7 -> {
                AlertDialog.Builder(context)
                        .setMessage(getString(R.string.LIVE_UNABLE_UNBIND, getString(R.string.app_name), getString(R.string.LIVE_PLATFORM_FACEBOOK)))
                        .setCancelable(false)
                        .setPositiveButton(R.string.OK, null)
                        .show()
            }
            account != null -> AlertDialog.Builder(context)
                    .setMessage(getString(R.string.LIVE_UNBIND_ACCOUNT, getString(R.string.LIVE_PLATFORM_FACEBOOK)))
                    .setCancelable(false)
                    .setPositiveButton(R.string.OK, { _, _ ->
                        account = null
                        UMShareAPI.get(context).deleteOauth(activity, SHARE_MEDIA.FACEBOOK, this)
                    })
                    .setNegativeButton(R.string.CANCEL, { dialog, _ ->
                        dialog.dismiss()
                    })
                    .show()
            else -> UMShareAPI.get(context).getPlatformInfo(activity, SHARE_MEDIA.FACEBOOK, this)
        }
    }

    fun isFacebookAccountBinded(): Boolean {
        if (account == null) {
            return false
        }
        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken == null || accessToken.isExpired) {
            return false
        }
        return true
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