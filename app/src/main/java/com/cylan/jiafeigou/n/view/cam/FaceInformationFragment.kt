package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ActivityUtils
import kotlinx.android.synthetic.main.fragment_face_information.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class FaceInformationFragment : BaseFragment<JFGPresenter<*>>() {

    private var faceInformation: DpMsgDefine.FaceInformation? = null

    override fun setFragmentComponent(fragmentComponent: FragmentComponent?) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_face_information, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        faceInformation = arguments.getParcelable("face_information")
        custom_toolbar.setBackAction {
            fragmentManager.popBackStack()
        }
        setting_item_face_name.subTitle = faceInformation?.face_name ?: ""
        face_name.text = faceInformation?.face_name ?: ""
        Glide.with(context)
                .load(faceInformation?.image_url)
                .error(R.drawable.icon_mine_head_normal)
                .placeholder(R.drawable.icon_mine_head_normal)
                .into(face_icon)
    }

    @OnClick(R.id.setting_item_face_manager)
    fun enterFaceManager() {
        //todo 进入到面孔管理页面
        if (faceInformation != null) {
            val fragment = FaceManagerFragment.newInstance(uuid, faceInformation!!.person_id)
            ActivityUtils.addFragmentSlideInFromRight(fragmentManager, fragment, android.R.id.content)
        } else {
            ///todo 为空怎么处理呢
        }
    }

    @OnClick(R.id.setting_item_face_name)
    fun enterSetFaceName() {
        AppLogger.w("enterSetFaceName")
        val fragment = SetFaceNameFragment.newInstance(uuid)

        ActivityUtils.addFragmentSlideInFromRight(fragmentManager, fragment, android.R.id.content)
    }

    companion object {
        fun newInstance(uuid: String, faceInformation: DpMsgDefine.FaceInformation?): FaceInformationFragment {
            val fragment = FaceInformationFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            argument.putParcelable("face_information", faceInformation)
            fragment.arguments = argument
            return fragment
        }
    }
}