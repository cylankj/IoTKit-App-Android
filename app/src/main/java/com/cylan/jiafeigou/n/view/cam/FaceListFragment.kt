package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.view.JFGView
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.view.cam.item.FaceItem
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import kotlinx.android.synthetic.main.fragment_facelist.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class FaceListFragment : BaseFragment<JFGPresenter<JFGView>>() {

    lateinit var adapter: FastItemAdapter<FaceItem>
    override fun setFragmentComponent(fragmentComponent: FragmentComponent?) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_facelist, container, false)
        return view
    }

    lateinit var layoutManager: LinearLayoutManager
    override fun initViewAndListener() {
        super.initViewAndListener()
        adapter = FastItemAdapter()
        adapter.withMultiSelect(false)
        adapter.withAllowDeselection(false)
        adapter.itemAdapter.withComparator { a, b ->
    0
//            return@withComparator Pinyin.toPinyin(a.faceText, "").compareTo(Pinyin.toPinyin(b.faceText, ""))
        }
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
        face_list_items.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                //TODO 更新右边的 slider
                val itemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                val faceItem = adapter.getItem(itemPosition)
//                val pinyin = Pinyin.toPinyin(faceItem.faceText, "")

            }

        })

    }

    var resultCallback: ((a: Any, b: Any, c: Any) -> Unit)? = null

    companion object {
        const val TYPE_ADD_TO = 1
        const val TYPE_MOVE_TO = 2
        fun newInstance(uuid: String, type: Int = TYPE_ADD_TO): FaceListFragment {
            val fragment = FaceListFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            argument.putInt("type", type)
            fragment.arguments = argument
            return fragment
        }
    }


}