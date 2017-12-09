package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.text.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.module.GlideApp
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.IMEUtils
import com.cylan.jiafeigou.utils.JFGFaceGlideURL
import com.cylan.jiafeigou.utils.ToastUtil
import kotlinx.android.synthetic.main.fragment_face_create.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class CreateNewFaceFragment : BaseFragment<CreateFaceContact.Presenter>(), CreateFaceContact.View {
    override fun onFaceNotExistError() {
        AppLogger.w("face_id 不存在 ,创建失败了")
        ToastUtil.showToast(getString(R.string.LIVE_CREATE_FAIL_TIPS))
    }

    data class HH(var a: String, var b: String, var c: String)


    override fun onCreateNewFaceTimeout() {
        AppLogger.w("创建面孔超时了")
        ToastUtil.showToast(getString(R.string.LIVE_CREATE_FAIL_TIPS))
    }

    override fun onCreateNewFaceSuccess(personId: String) {
        AppLogger.w("创建面孔返回值为:$personId")
        ToastUtil.showToast(getString(R.string.PWD_OK_2))
        resultCallback?.invoke(personId)
        fragmentManager?.popBackStack()

    }

    override fun onCreateNewFaceError(ret: Int) {
        AppLogger.w("创建面孔失败了")
        ToastUtil.showToast(getString(R.string.LIVE_CREATE_FAIL_TIPS))
    }

    private var strangerVisitor: DpMsgDefine.StrangerVisitor? = null
    private var faceId: String? = null
    var resultCallback: ((personId: String) -> Unit)? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_face_create, container, false)
    }


    override fun initViewAndListener() {
        super.initViewAndListener()
        strangerVisitor = arguments!!.getParcelable("strangerVisitor")
        faceId = strangerVisitor?.faceId ?: ""
        GlideApp.with(this)
                .load(JFGFaceGlideURL("", strangerVisitor?.image_url, strangerVisitor?.ossType ?: 0, true))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .dontAnimate()
                .into(picture)
        custom_toolbar.setRightAction {
            val text = name.text.toString().trim()
            if (faceId == null) {
                ToastUtil.showToast("语言包: face_id is null")
                AppLogger.w("FaceId :$faceId ")
            } else if (TextUtils.isEmpty(text)) {
                ToastUtil.showToast("语言包:名称不能为空")
            } else {
                IMEUtils.hide(activity)
                presenter.createNewFace(faceId!!, text)
            }
        }
        //默认不可点击,需要输入名称后才能点击
        custom_toolbar.setRightEnable(!TextUtils.isEmpty(name.text.toString().trim()))
        custom_toolbar.setBackAction {
            sendResultIfNeed()
            fragmentManager?.popBackStack()
        }
        name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var empty = TextUtils.isEmpty(s) || TextUtils.isEmpty(s?.trim())
                custom_toolbar.setRightEnable(!empty)
                clear_icon.visibility = if (empty) View.INVISIBLE else View.VISIBLE
            }
        })

        name.filters = arrayOf(InputFilter { source, _, _, dest, _, _ ->
            val originWidth = BoringLayout.getDesiredWidth("$dest", name.paint)
            val measuredWidth = name.measuredWidth
            var result = "$source"
            var width = BoringLayout.getDesiredWidth(result, name.paint)

            Log.i(JConstant.CYLAN_TAG, "source:$source,dest:$dest,usedWidth:$originWidth inputWidth:$width,acceptWidth:${name.measuredWidth}")

            while (originWidth + width > measuredWidth) {
                result = result.dropLast(1)
                width = BoringLayout.getDesiredWidth(result, name.paint)
            }
            result
        })

    }

    override fun onDetach() {
        super.onDetach()
        IMEUtils.hide(activity)
    }

    private fun sendResultIfNeed() {

    }

    @OnClick(R.id.clear_icon)
    fun clearInputText() {
        name.setText("")
    }

    companion object {

        fun newInstance(uuid: String, strangerVisitor: DpMsgDefine.StrangerVisitor?): CreateNewFaceFragment {
            val fragment = CreateNewFaceFragment()
//            HmacSHA1Signature().computeSignature()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            argument.putParcelable("strangerVisitor", strangerVisitor)
            fragment.arguments = argument
            return fragment
        }

    }
}