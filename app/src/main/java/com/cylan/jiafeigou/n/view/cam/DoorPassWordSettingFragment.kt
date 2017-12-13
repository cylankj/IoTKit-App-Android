package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.mvp.contract.cam.DoorPassWordSettingContact
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ToastUtil
import kotlinx.android.synthetic.main.fragment_door_password_setting.*

/**
 * Created by yanzhendong on 2017/11/17.
 */
class DoorPassWordSettingFragment : BaseFragment<DoorPassWordSettingContact.Presenter>(), DoorPassWordSettingContact.View {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_door_password_setting, container, false)
    }


    companion object {
        fun newInstance(uuid: String): DoorPassWordSettingFragment {
            val fragment = DoorPassWordSettingFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = argument
            return fragment
        }
    }

    @OnClick(R.id.tv_toolbar_icon)
    fun onBackClick() {
        AppLogger.w("")
        fragmentManager?.popBackStack()
    }

    @OnClick(R.id.tv_toolbar_right)
    fun done() {
        AppLogger.w("done")
        val oldPassword = password1.getEditer().text.toString().trim()
        val newPassword = password2.getEditer().text.toString().trim()
        if (TextUtils.equals(oldPassword, newPassword)) {
            ToastUtil.showToast(getString(R.string.RET_ECHANGEPASS_SAME))
        } else {
            presenter.changePassWord(oldPassword, newPassword)
        }
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        password1.addTextChangedListener(textWatcher)
        password2.addTextChangedListener(textWatcher)
        custom_toolbar.setRightEnable(false)
    }

    fun setDoneEnable() {
        val text1 = password1.getEditer().text
        val text2 = password2.getEditer().text
        custom_toolbar.setRightEnable(text1.length >= 6 && text2.length >= 6)
    }

    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            setDoneEnable()
        }
    }

    override fun onChangePasswordSuccess() {
        AppLogger.w("onChangePasswordSuccess")
        ToastUtil.showToast(context?.getString(R.string.PWD_OK_1))
        fragmentManager?.popBackStack()
    }

    override fun onChangePasswordError() {
        AppLogger.w("onChangePasswordError")
        ToastUtil.showToast(context!!.getString(R.string.SETTINGS_FAILED))
    }

    override fun onOldPasswordError() {
        AppLogger.w("onOldPasswordError")
        ToastUtil.showToast(getString(R.string.RET_ECHANGEPASS_OLDPASS_ERROR))
    }
}