package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant

/**
 * Created by yanzhendong on 2017/11/17.
 */
class DoorPassWordSettingFragment : BaseFragment<JFGPresenter>() {

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
}