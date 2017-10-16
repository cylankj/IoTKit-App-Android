package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.view.cam.item.FaceListItem
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import kotlinx.android.synthetic.main.fragment_facelist.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class FaceListFragment : BaseFragment<FaceListContact.Presenter>(), FaceListContact.View {

    lateinit var adapter: FastItemAdapter<FaceListItem>
    override fun setFragmentComponent(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_facelist, container, false)
        return view
    }

    lateinit var layoutManager: LinearLayoutManager
    override fun initViewAndListener() {
        super.initViewAndListener()
        account = arguments.getString("account")
        adapter = FastItemAdapter()
        adapter.withMultiSelect(false)
        adapter.withAllowDeselection(false)
        when (arguments?.getInt("type", TYPE_ADD_TO)) {
            TYPE_ADD_TO -> {
                custom_toolbar.setToolbarLeftTitle(R.string.MESSAGES_IDENTIFY_ADD_BTN)
            }
            TYPE_MOVE_TO -> {
                custom_toolbar.setToolbarLeftTitle(R.string.MESSAGES_FACE_MOVE)
            }
            else -> {
                custom_toolbar.setToolbarLeftTitle(R.string.MESSAGES_IDENTIFY_ADD_BTN)
            }
        }

        layoutManager = LinearLayoutManager(context)
        face_list_items.adapter = adapter
        face_list_items.layoutManager = layoutManager

    }

    override fun onStart() {
        super.onStart()
        presenter.loadPersonItems(account!!, uuid)
    }

    var resultCallback: ((a: Any, b: Any, c: Any) -> Unit)? = null

    private var account: String? = null

    companion object {
        const val TYPE_ADD_TO = 1
        const val TYPE_MOVE_TO = 2
        fun newInstance(account: String, uuid: String, type: Int = TYPE_ADD_TO): FaceListFragment {
            val fragment = FaceListFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            argument.putInt("type", type)
            argument.putString("account", account)
            fragment.arguments = argument
            return fragment
        }
    }


}