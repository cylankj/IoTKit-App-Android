package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.support.v4.app.Fragment
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.view.cam.item.FaceItem


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [VisitorStrangerSubFragment.OnListCallback] interface
 * to handle interaction events.
 * Use the [VisitorStrangerSubFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VisitorStrangerSubFragment : VisitorListFragmentV2() {


    companion object {
        fun newInstance(uuid: String): VisitorStrangerSubFragment {
            val fragment = VisitorStrangerSubFragment()
            val args = Bundle()
            args.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = args
            return fragment
        }
    }

    /**
     * 提供数据，陌生人
     */
    override fun provideData(): ArrayList<FaceItem> {
        return FaceItemsProvider.get.strangerItems
    }

    override fun isNormalVisitor(): Boolean {
        return false
    }
    override fun cleanData(): Boolean {
        return false
    }

    override fun onVisitorListReady(visitorList: DpMsgDefine.StrangerVisitorList?) {
    }

}// Required empty public constructor
