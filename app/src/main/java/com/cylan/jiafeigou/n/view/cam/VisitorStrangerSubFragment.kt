package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.base.IBaseFragment
import com.cylan.jiafeigou.n.mvp.contract.cam.FaceStrangerContract
import com.cylan.jiafeigou.n.mvp.impl.cam.VisitorStrangerPresenter
import com.cylan.jiafeigou.n.view.cam.item.FaceItem
import com.cylan.jiafeigou.utils.ListUtils
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [VisitorStrangerSubFragment.OnListCallback] interface
 * to handle interaction events.
 * Use the [VisitorStrangerSubFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VisitorStrangerSubFragment : IBaseFragment<FaceStrangerContract.Presenter>(),
        FaceStrangerContract.View {


    lateinit var onListCallback: OnListCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        basePresenter = VisitorStrangerPresenter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_face_stranger, container, false)
    }

    companion object {
        fun newInstance(uuid: String): VisitorStrangerSubFragment {
            val fragment = VisitorStrangerSubFragment()
            val args = Bundle()
            args.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onStrangerVisitorListReady(visitorList: DpMsgDefine.StrangerVisitorList) {
        val list = ArrayList<FaceItem>()
        for (visitor in visitorList.strangerVisitors) {
            val allFace = FaceItem()
            allFace.withFaceType(FaceItem.FACE_TYPE_STRANGER)
            allFace.withStrangerVisitor(visitor)
            list.add(allFace)
        }
        if (ListUtils.isEmpty(list)) {
            return
        }
//        camMessageFaceAdapter.appendFaceItems(list)
    }


    interface OnListCallback {
        fun onItemClick(type: FaceItem?, dataList: ArrayList<String>?)
        fun onPageScroll(currentItem: Int, total: Int)
    }

}// Required empty public constructor
