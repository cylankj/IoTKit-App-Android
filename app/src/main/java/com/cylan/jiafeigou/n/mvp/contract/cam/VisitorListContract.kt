package com.cylan.jiafeigou.n.mvp.contract.cam

import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.n.mvp.BaseFragmentView
import com.cylan.jiafeigou.n.mvp.BasePresenter

/**
 * Created by hds on 17-10-20.
 */
interface VisitorListContract {

    interface View : BaseFragmentView<Presenter> {
        fun onVisitorListReady(visitorList: DpMsgDefine.VisitorList?)
    }

    interface Presenter : BasePresenter {
        fun fetchVisitorList()
    }

}

interface FaceStrangerContract {

    interface View : BaseFragmentView<Presenter> {
        fun onStrangerVisitorListReady(visitorList: DpMsgDefine.StrangerVisitorList)
    }

    interface Presenter : BasePresenter {
        fun fetchVisitorList()
    }

}