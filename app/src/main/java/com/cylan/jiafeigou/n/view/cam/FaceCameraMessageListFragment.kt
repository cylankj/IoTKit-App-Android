package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.widget.NestedScrollView
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.base.IBaseFragment
import com.cylan.jiafeigou.n.mvp.BasePresenter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter

/**
 * Created by yanzhendong on 2017/9/29.
 */

class FaceCameraMessageListFragment : IBaseFragment<BasePresenter>() {
    @BindView(R.id.message_appbar)
    lateinit var messageAppbar: AppBarLayout
    @BindView(R.id.message_nested_parent)
    lateinit var messageNestedParent: NestedScrollView
    @BindView(R.id.message_refresh)
    lateinit var messageRefresh: SwipeRefreshLayout
    @BindView(R.id.message_content)
    lateinit var messageContent: RecyclerView

    private lateinit var messageAdapter: FastItemAdapter<*>


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_cam_message_face, container, false)
        ButterKnife.bind(this, view)
        initViewAndListener()
        return view
    }

    override fun initViewAndListener() {
        messageAdapter = FastItemAdapter<IItem<*, *>>()
        val manager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        messageContent.layoutManager = manager
        messageContent.adapter = messageAdapter


    }

    companion object {


        fun newInstance(uuid: String): FaceCameraMessageListFragment {
            val fragment = FaceCameraMessageListFragment()
            val bundle = Bundle()
            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = bundle

            return fragment
        }
    }
}
