package com.cylan.jiafeigou.n.view.panorama

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import butterknife.OnClick
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.base.view.JFGView
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ActivityUtils
import com.cylan.jiafeigou.utils.PreferencesUtils
import com.google.gson.Gson
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

    override fun setFragmentComponent(fragmentComponent: FragmentComponent?) {
    }

    override fun getContentViewID(): Int {
        return R.layout.layout_facebook
    }

    private var account: String? = null
        set(value) {
            field = value
            setting_facebook_account_item.subTitle = if (TextUtils.isEmpty(field)) getString(R.string.NO_SET) else field
            setting_facebook_permission.visibility = if (TextUtils.isEmpty(field)) View.GONE else View.VISIBLE
        }

    private var permission: Int = 0
        set(value) {
            field = value
            when (field) {
                0 -> {
                }
                1 -> {
                }
                2 -> {
//                    JConstant.FACEBOOK_PERMISSION_PUBLIC
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
    }

    @OnClick(R.id.setting_facebook_permission)
    fun setFacebookPermission() {
        val instance = FacebookLivePermissionFragment.newInstance()
        instance.setCallBack {
            //          PreferencesUtils.putString(JConstant.facebook_PREF_PERMISSION_KEY,it)
        }
        ActivityUtils.addFragmentSlideInFromRight(fragmentManager, instance, android.R.id.content)
    }

    @OnClick(R.id.setting_facebook_account_item)
    fun selectAccount() {
        when {
            DataSourceManager.getInstance().loginType == 7 -> {
                AlertDialog.Builder(context)
                        .setMessage("缺少语言包:当前加菲狗/doby使用Facebook登录，暂不能解绑该账号")
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