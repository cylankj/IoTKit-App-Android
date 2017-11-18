package com.cylan.jiafeigou.widget.dialog

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnTextChanged
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ToastUtil
import kotlinx.android.synthetic.main.layout_door_lock_alert.*

/**
 * Created by yanzhendong on 2017/11/18.
 */
class DoorLockDialog : BaseDialog<String>() {
    lateinit var uuid: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.uuid = arguments.getString(JConstant.KEY_DEVICE_ITEM_UUID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_door_lock_alert, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    @OnTextChanged(R.id.edit_door_pass_word)
    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        ok.isEnabled = !TextUtils.isEmpty(s)
    }

    @OnClick(R.id.ok)
    fun onOkClicked() {
        AppLogger.w("点击了门锁确定按钮")
        val password = edit_door_pass_word.text.toString().trim()
        when {
            TextUtils.isEmpty(password) -> ToastUtil.showToast("语言包:密码不能为空")
            password.length < 6 -> ToastUtil.showToast("语言包:请输入6~16位密码")
            else -> {
                dialog.dismiss()
                action?.onDialogAction(R.id.ok, password)
            }
        }
    }

    @OnClick(R.id.cancel)
    fun onCancelClicked() {
        AppLogger.w("点击了门锁取消按钮")
        dismiss()
    }

    companion object {
        fun newInstance(uuid: String): DoorLockDialog {
            val dialog = DoorLockDialog()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            dialog.arguments = argument
            return dialog
        }
    }

}