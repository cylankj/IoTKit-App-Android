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

    interface Presenter : JFGPresenter {
        fun setFaceName(personId: String, faceName: String)
    }
}

interface FaceManagerContact {

    interface View : JFGView {

        fun onFaceInformationReady(data: List<DpMsgDefine.FaceInformation>)

        fun onDeleteFaceError()

        fun onDeleteFaceSuccess()

    }

    interface Presenter : JFGPresenter {

        fun loadFacesByPersonId(personId: String)

        fun deleteFace(personId: String?, listOf: List<String>)

    }
}

interface CreateFaceContact {
    interface View : JFGView  {

        fun onCreateNewFaceSuccess(personId: String)

        fun onCreateNewFaceError(ret: Int)

        fun onCreateNewFaceTimeout()

        fun onFaceNotExistError()
    }

    interface Presenter : JFGPresenter {

        fun createNewFace(faceId: String, faceName: String)

    }
}

interface FaceListContact {

    interface View : JFGView {

        fun onFaceInformationReady(data: List<DpMsgDefine.FaceInformation>)

        fun onMoveFaceToPersonSuccess(personId: String)

        fun onFaceNotExistError()

        fun onVisitorInformationReady(visitors: List<DpMsgDefine.Visitor>?)

    }

    interface Presenter : JFGPresenter {
        // todo 是否需要 uuid?
        fun loadPersonItems(account: String, uuid: String)

        fun moveFaceToPerson(personId: String, faceId: String)

        fun loadPersonItem2()
    }
}

