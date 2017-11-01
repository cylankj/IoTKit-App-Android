package com.cylan.jiafeigou.n.mvp.contract.cam

import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.n.mvp.BaseFragmentView
import com.cylan.jiafeigou.n.mvp.BasePresenter

/**
 * Created by hds on 17-10-20.
 */
interface VisitorListContract {

    interface View : BaseFragmentView {
        fun onVisitorListReady(visitorList: DpMsgDefine.VisitorList?)
        fun onVisitorListReady(visitorList: DpMsgDefine.StrangerVisitorList?)
        fun onVisitsTimeRsp(faceId: String, cnt: Int)
        fun isNormalVisitor(): Boolean
        fun onDeleteFaceSuccess(type: Int, delMsg: Int)
        fun onDeleteFaceError()
    }

    interface Presenter : BasePresenter {
        fun fetchVisitorList()
        fun fetchStrangerVisitorList()
        fun fetchVisitsCount(faceId: String)
        //    cid, type, id, delMsg
        fun deleteFace(type: Int, id: String, delMsg: Int)
    }

}

interface FaceStrangerContract {

    interface View : BaseFragmentView {
        fun onStrangerVisitorListReady(visitorList: DpMsgDefine.StrangerVisitorList)
    }

    interface Presenter : BasePresenter {
        fun fetchVisitorList()
    }

}