package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.NetUtils
import com.cylan.jiafeigou.utils.ToastUtil
import kotlinx.android.synthetic.main.fragment_set_face_name.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class SetFaceNameFragment : BaseFragment<SetFaceNameContact.Presenter>() {

    private var faceId: String? = null
    private var personId: String? = null

    override fun setFragmentComponent(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_set_face_name, container, false)
        return view
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        faceId = arguments.getString("face_id")
        personId = arguments.getString("person_id")
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
        if (NetUtils.getNetType(context) == -1) {
            AppLogger.w("无网络连接")
            ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1))
        } else {
            //TODO 修改或者新建面孔名称
            presenter.setFaceName(faceId ?: "", personId ?: "", edit_face_name.text.toString().trim())
        }
    }

    companion object {
        fun newInstance(uuid: String, faceId: String, personId: String): SetFaceNameFragment {
            val fragment = SetFaceNameFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            argument.putString("face_id", faceId)
            argument.putString("person_id", personId)
            fragment.arguments = argument
            return fragment
        }
    }
}