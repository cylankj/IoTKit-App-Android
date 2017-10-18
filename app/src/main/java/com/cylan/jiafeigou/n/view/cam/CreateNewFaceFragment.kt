package com.cylan.jiafeigou.n.view.cam

import android.graphics.Bitmap
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
import com.cylan.jiafeigou.utils.ToastUtil
import kotlinx.android.synthetic.main.fragment_face_create.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class CreateNewFaceFragment : BaseFragment<CreateFaceContact.Presenter>(), CreateFaceContact.View {
    override fun onCreateNewFaceResponse(ret: Int) {
        AppLogger.w("创建面孔返回值为:" + ret)
        if (ret == 0) {
            resultCallback?.invoke(ret)
            fragmentManager.popBackStack()
        } else {
            AppLogger.w("创建面孔失败了")
            ToastUtil.showToast("创建失败了")
        }
    }

    var faceId: String? = null
    var picture: Bitmap? = null
    var resultCallback: ((ret: Int) -> Unit)? = null


    override fun setFragmentComponent(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_face_create, container, false)
        return view
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        faceId = arguments.getString("face_id")
        picture = arguments.getParcelable("picture")
        custom_toolbar.setRightAction {
            if (faceId == null || picture == null) {
                AppLogger.w("FaceId :$faceId ,picture:$picture")
            } else {
                presenter.createNewFace(faceId!!, name.text.toString().trim(), picture!!)
            }
        }
        custom_toolbar.setBackAction {
            sendResultIfNeed()
            fragmentManager.popBackStack()
        }
        name.addTextChangedListener(object : TextWatcher {
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

    private fun sendResultIfNeed() {

    }

    companion object {

        fun newInstance(uuid: String, faceId: String, picture: Bitmap): CreateNewFaceFragment {
            val fragment = CreateNewFaceFragment()

            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            argument.putString("face_id", faceId)
            argument.putParcelable("picture", picture)
            fragment.arguments = argument
            return fragment
        }

    }
}