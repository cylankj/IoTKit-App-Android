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
import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import kotlinx.android.synthetic.main.fragment_face_create.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class CreateNewFaceFragment : BaseFragment<JFGPresenter<*>>() {

    var resultCallback: ((a: Any, b: Any, c: Any) -> Unit)? = null
    override fun setFragmentComponent(fragmentComponent: FragmentComponent?) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_face_create, container, false)
        return view
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        custom_toolbar.setRightAction {
            sendResultIfNeed()
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

        fun newInstance(uuid: String): CreateNewFaceFragment {
            val fragment = CreateNewFaceFragment()

            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = argument
            return fragment
        }

    }
}