package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ActivityUtils
import kotlinx.android.synthetic.main.fragment_face_information.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class FaceInformationFragment : BaseFragment<JFGPresenter>() {

    private var faceName: String? = null
    private var personId: String? = null
    private var imageUrl: String? = null
    override fun supportInject(): Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_face_information, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        faceName = arguments?.getString("face_name")
        personId = arguments?.getString("person_id")
        imageUrl = arguments?.getString("image")
        custom_toolbar.setBackAction {
            fragmentManager.popBackStack()
        }
        setting_item_face_name.subTitle = faceName
        face_name.text = faceName
        Glide.with(context)
                .load(imageUrl)
                .error(R.drawable.icon_mine_head_normal)
                .placeholder(R.drawable.icon_mine_head_normal)
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

    companion object {
        const val REQ_SET_FACE_NAME = 1000
        fun newInstance(uuid: String, image: String, faceName: String?, personId: String): FaceInformationFragment {
            val fragment = FaceInformationFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            if (faceName != null) {
                argument.putString("face_name", faceName)
            }
            argument.putString("image", image)
            argument.putString("person_id", personId)
            fragment.arguments = argument
            return fragment
        }
    }
}