package com.cylan.jiafeigou.n.view.cam

import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.view.JFGView
import com.cylan.jiafeigou.dp.DpMsgDefine

/**
 * Created by yanzhendong on 2017/10/9.
 */


interface SetFaceNameContact {

    interface View : JFGView {
        fun onSetFaceNameError(ret: Int?)
        fun onSetFaceNameSuccess()
    }

    interface Presenter : JFGPresenter<View> {
        fun setFaceName(faceId: String, personId: String, faceName: String)
    }
}

interface FaceManagerContact {

    interface View : JFGView {
        fun onFaceInformationReady(data: List<DpMsgDefine.FaceInformation>)

    }

    interface Presenter : JFGPresenter<View> {

        fun loadFacesByPersonId(personId: String)
    }
}

interface CreateFaceContact {
    interface View : JFGView {

        fun onCreateNewFaceSuccess(personId: String)
        fun onCreateNewFaceError(ret: Int)

    }

    interface Presenter : JFGPresenter<View> {

        fun createNewFace(faceId: String, faceName: String)

    }
}

interface FaceListContact {

    interface View : JFGView {
        fun onFaceInformationReady(data: List<DpMsgDefine.FaceInformation>)

    }

    interface Presenter : JFGPresenter<View> {
        // todo 是否需要 uuid?
        fun loadPersonItems(account: String, uuid: String)

        fun moveFaceToPerson(personId: String, faceId: String)

    }
}

