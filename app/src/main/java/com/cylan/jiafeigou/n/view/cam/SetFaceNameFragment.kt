package com.cylan.jiafeigou.n.view.cam

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.IMEUtils
import com.cylan.jiafeigou.utils.NetUtils
import com.cylan.jiafeigou.utils.ToastUtil
import kotlinx.android.synthetic.main.fragment_set_face_name.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class SetFaceNameFragment : BaseFragment<SetFaceNameContact.Presenter>(), SetFaceNameContact.View {
    override fun onSetFaceNameError(ret: Int?) {
        AppLogger.e("设置 faceName 失败了:ret $ret")
        IMEUtils.hide(activity)
        ToastUtil.showToast("语言包:设置 FaceName 失败了")
    }

    override fun onSetFaceNameSuccess(faceName: String) {
        AppLogger.w("设置面孔名称成功了")
        ToastUtil.showToast(getString(R.string.PWD_OK_2))
        val intent = Intent()
        intent.putExtra("name", faceName)
        setResult(Activity.RESULT_OK, intent)
        fragmentManager.popBackStack()
    }

    private var personId: String? = null
    private var oldName: String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_set_face_name, container, false)
        return view
    }


    override fun initViewAndListener() {
        super.initViewAndListener()
        personId = arguments.getString("person_id")
        oldName = arguments.getString("old_name")
        edit_face_name.setText(oldName)
        custom_toolbar.setRightEnable(!TextUtils.isEmpty(edit_face_name.text.toString()))
        custom_toolbar.setRightAction { setFaceName() }
        custom_toolbar.setBackAction { fragmentManager.popBackStack() }
        edit_face_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var empty = TextUtils.isEmpty(s) || TextUtils.isEmpty(s?.trim())
                custom_toolbar.setRightEnable(!empty)
            }
        })
    }


    private fun setFaceName() {
        AppLogger.w("正在修改面孔名称")
        val newName = edit_face_name.text.toString().trim()
        if (NetUtils.getNetType(context) == -1) {
            AppLogger.w("无网络连接")
            ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1))
        } else if (TextUtils.isEmpty(newName)) {
            //TODO 修改或者新建面孔名称
            ToastUtil.showToast("语言包:名称不能为空")
        } else {
            presenter.setFaceName(personId ?: "", newName)
        }
    }

    companion object {
        fun newInstance(uuid: String, oldName: String, personId: String): SetFaceNameFragment {
            val fragment = SetFaceNameFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            argument.putString("person_id", personId)
            argument.putString("old_name", oldName)
            fragment.arguments = argument
            return fragment
        }
    }
}