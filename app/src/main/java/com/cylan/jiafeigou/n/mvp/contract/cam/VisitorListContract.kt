package com.cylan.jiafeigou.n.mvp.contract.cam

import com.cylan.jiafeigou.n.mvp.BaseFragmentView
import com.cylan.jiafeigou.n.mvp.BasePresenter
import com.cylan.jiafeigou.n.view.cam.item.FaceItem

/**
 * Created by hds on 17-10-20.
 */
interface VisitorListContract {

    interface View : BaseFragmentView {
        fun onVisitorListReady(visitorList: MutableList<FaceItem>, version: Long)
        fun onStrangerVisitorListReady(visitorList: MutableList<FaceItem>, version: Long)
        fun onVisitsTimeRsp(faceId: String, cnt: Int, type: Int)
        fun onDeleteFaceSuccess(type: Int, delMsg: Int)
        fun onDeleteFaceError()
        fun onDeleteFaceErrorPermissionError()
        fun onDeleteFaceErrorInvalidParams()
        fun onDeleteFaceErrorServerInternalError()
        fun onDeleteFaceTimeout()
        fun onReceiveNewMessage(msgCount: Int)
    }

    interface Presenter : BasePresenter {
        fun fetchVisitorList(version: Long)
        fun fetchStrangerVisitorList(version: Long)
        fun fetchVisitsCount(faceId: String?, type: Int)
        //    cid, type, id, delMsg
        fun deleteFace(type: Int, id: String, delMsg: Int)

        fun deleteFaceV2(type: Int, id: String, delMsg: Int)
    }

}
