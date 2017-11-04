package com.cylan.jiafeigou.n.view.cam

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ActivityUtils
import com.cylan.jiafeigou.utils.JFGFaceGlideURL
import kotlinx.android.synthetic.main.fragment_face_information.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class FaceInformationFragment : BaseFragment<JFGPresenter>() {

    private var faceName: String? = null
    private var personId: String? = null
    private var visitor: DpMsgDefine.Visitor? = null
    override fun useDaggerInject(): Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_face_information, container, false)
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        visitor = arguments.getParcelable("visitor")
        faceName = visitor?.personName
        personId = visitor?.personId
        custom_toolbar.setBackAction {
            fragmentManager.popBackStack()
        }
        setting_item_face_name.subTitle = faceName
        face_name.text = faceName
        val visitorDetail = visitor?.detailList?.getOrNull(0)
        Glide.with(this)
                .load(JFGFaceGlideURL("", visitorDetail?.imgUrl, visitorDetail?.ossType ?: 0, false))
                .error(R.drawable.icon_mine_head_normal)
                .placeholder(R.drawable.icon_mine_head_normal)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .into(face_icon)
    }

    @OnClick(R.id.setting_item_face_manager)
    fun enterFaceManager() {
        //todo 进入到面孔管理页面
        val fragment = FaceManagerFragment.newInstance(uuid, personId ?: "")
        ActivityUtils.addFragmentSlideInFromRight(fragmentManager, fragment, android.R.id.content)

    }

    @OnClick(R.id.setting_item_face_name)
    fun enterSetFaceName() {
        AppLogger.w("enterSetFaceName")
        val fragment = SetFaceNameFragment.newInstance(uuid, setting_item_face_name.subTitle.toString(), personId ?: "")
        fragment.setTargetFragment(this, REQ_SET_FACE_NAME)

        ActivityUtils.addFragmentSlideInFromRight(fragmentManager, fragment, android.R.id.content)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_SET_FACE_NAME -> {
                if (resultCode == Activity.RESULT_OK) {
                    val newName = data?.getStringExtra("name")
                    if (!TextUtils.isEmpty(newName)) {
                        face_name.text = newName
                        setting_item_face_name.subTitle = newName
                    }
                }

            }
        }
    }

    companion object {
        const val REQ_SET_FACE_NAME = 1000
        fun newInstance(uuid: String, visitor: DpMsgDefine.Visitor?): FaceInformationFragment {
            val fragment = FaceInformationFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            argument.putParcelable("visitor", visitor)
            fragment.arguments = argument
            return fragment
        }
    }
}