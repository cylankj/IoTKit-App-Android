package com.cylan.jiafeigou.n.view.panorama

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import butterknife.OnClick
import butterknife.OnTextChanged
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.base.view.JFGView
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.PreferencesUtils
import com.google.gson.Gson
import com.umeng.socialize.UMAuthListener
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.bean.SHARE_MEDIA
import kotlinx.android.synthetic.main.layout_weibo.*

/**
 * Created by yanzhendong on 2017/9/7.
 */
class WeiboLiveSettingFragment : BaseFragment<BasePresenter<JFGView>>(), UMAuthListener {
    override fun onStart(p0: SHARE_MEDIA?) {

    }

    override fun onComplete(p0: SHARE_MEDIA?, p1: Int, p2: MutableMap<String, String>?) {
        if (p1 == UMAuthListener.ACTION_GET_PROFILE) {
            AppLogger.w("p1 is:$p1,${Gson().toJson(p2)}")
            PreferencesUtils.putString(JConstant.OPEN_LOGIN_MAP + SHARE_MEDIA.SINA.toString(), Gson().toJson(p2))
            account = p2!!["name"]
            accessToken = p2!!["accessToken"]
        } else if (p1 == UMAuthListener.ACTION_DELETE) {
            PreferencesUtils.remove(JConstant.OPEN_LOGIN_MAP + SHARE_MEDIA.SINA.toString())
        }
    }

    override fun onCancel(p0: SHARE_MEDIA?, p1: Int) {
    }

    override fun onError(p0: SHARE_MEDIA?, p1: Int, p2: Throwable?) {
    }

    override fun setFragmentComponent(fragmentComponent: FragmentComponent?) {

    }

    override fun getContentViewID(): Int {
        return R.layout.layout_weibo
    }

    open var accessToken: String? = null

    var account: String? = null
        set(value) {
            field = value
            setting_weibo_account_item.subTitle = if (TextUtils.isEmpty(field)) getString(R.string.NO_SET) else field
        }

    override fun initViewAndListener() {
        super.initViewAndListener()
        setting_weibo_account_item.title = getString(R.string.LIVE_ACCOUNT, getString(R.string.LIVE_PLATFORM_WEIBO))

        val map = PreferencesUtils.getString(JConstant.OPEN_LOGIN_MAP + SHARE_MEDIA.SINA.toString(), null)
        val fromJson = Gson().fromJson(map, Map::class.java)
        if (fromJson != null) {
            //检测 token 有效性
            val expiration = (fromJson["expires_in"] as String).toLong()
            if (expiration - System.currentTimeMillis() <= 0) {
                //过期了
                account = null
                PreferencesUtils.remove(JConstant.OPEN_LOGIN_MAP + SHARE_MEDIA.SINA.toString())
            } else {
                accessToken = fromJson["accessToken"] as String
                account = fromJson["name"] as String
            }
        }
    }

    override fun onStart() {
        super.onStart()
        account = account
    }

    @OnTextChanged(R.id.setting_weibo_description)
    fun onDescriptionChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        setting_weibo_description_remain_count.text = "${110 - s.length}"
    }

    fun getWeiboLiveDescription(): String {
        val editable = setting_weibo_description.text
        return if (TextUtils.isEmpty(editable.trim())) getString(R.string.LIVE_DETAIL_DEFAULT_CONTENT) else editable.trim().toString()
    }


    @OnClick(R.id.setting_weibo_account_item)
    fun selectAccount() {
        when {
            DataSourceManager.getInstance().loginType == 4 -> {
                AlertDialog.Builder(context)
                        .setMessage("缺少语言包:当前加菲狗/doby使用新浪微博登录，暂不能解绑该账号")
                        .setCancelable(false)
                        .setPositiveButton(R.string.OK, null)
                        .show()
            }
            account != null -> AlertDialog.Builder(context)
                    .setMessage(getString(R.string.LIVE_UNBIND_ACCOUNT, getString(R.string.LIVE_PLATFORM_WEIBO)))
                    .setCancelable(false)
                    .setPositiveButton(R.string.OK, { _, _ ->
                        account = null
                        UMShareAPI.get(context).deleteOauth(activity, SHARE_MEDIA.SINA, this)
                    })
                    .setNegativeButton(R.string.CANCEL, { dialog, _ ->
                        dialog.dismiss()
                    })
                    .show()
            else -> UMShareAPI.get(context).getPlatformInfo(activity, SHARE_MEDIA.SINA, this)
        }
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