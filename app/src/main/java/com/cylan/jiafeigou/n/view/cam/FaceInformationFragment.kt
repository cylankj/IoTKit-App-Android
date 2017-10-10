package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.OnClick
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ActivityUtils
import kotlinx.android.synthetic.main.fragment_face_information.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class FaceInformationFragment : BaseFragment<JFGPresenter<*>>() {
    override fun setFragmentComponent(fragmentComponent: FragmentComponent?) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_face_information, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        custom_toolbar.setBackAction {
            fragmentManager.popBackStack()
        }
    }

    @OnClick(R.id.setting_item_face_manager)
    fun enterFaceManager() {
        //todo 进入到面孔管理页面
        val fragment = FaceManagerFragment.newInstance(uuid)

        ActivityUtils.addFragmentSlideInFromRight(fragmentManager, fragment, android.R.id.content)
    }

    @OnClick(R.id.setting_item_face_name)
    fun enterSetFaceName() {
        AppLogger.w("enterSetFaceName")
        val fragment = SetFaceNameFragment.newInstance(uuid)

        ActivityUtils.addFragmentSlideInFromRight(fragmentManager, fragment, android.R.id.content)
    }

    companion object {
        fun newInstance(uuid: String): FaceInformationFragment {
            val fragment = FaceInformationFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = argument
            return fragment
        }
    }
}