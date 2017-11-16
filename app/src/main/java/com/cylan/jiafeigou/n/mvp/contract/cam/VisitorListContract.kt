package com.cylan.jiafeigou.n.mvp.contract.cam

import com.cylan.jiafeigou.n.mvp.BaseFragmentView
import com.cylan.jiafeigou.n.mvp.BasePresenter
import com.cylan.jiafeigou.n.view.cam.item.FaceItem

/**
 * Created by hds on 17-10-20.
 */
interface VisitorListContract {

    interface View : BaseFragmentView {
        fun onVisitorListReady(visitorList: MutableList<FaceItem>)
        fun onStrangerVisitorListReady(visitorList: MutableList<FaceItem>)
        fun onVisitsTimeRsp(faceId: String, cnt: Int, type: Int)
        fun onDeleteFaceSuccess(type: Int, delMsg: Int)
        fun onDeleteFaceError()
    }

    interface Presenter : BasePresenter {
        fun fetchVisitorList()
        fun fetchStrangerVisitorList()
        fun fetchVisitsCount(faceId: String?, type: Int)
        //    cid, type, id, delMsg
        fun deleteFace(type: Int, id: String, delMsg: Int)
    }

}
