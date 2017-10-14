package com.cylan.jiafeigou.n.view.cam

import android.graphics.Bitmap
import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.view.JFGView

/**
 * Created by yanzhendong on 2017/10/9.
 */


interface SetFaceNameContact {

    interface View : JFGView {

    }

    interface Presenter : JFGPresenter<View> {
        fun setFaceName(trim: CharSequence)
    }
}

interface FaceManagerContact {

    interface View : JFGView {

    }

    interface Presenter : JFGPresenter<View> {

    }
}

interface CreateFaceContact {
    interface View : JFGView {

        fun onCreateNewFaceResponse(ret: Int)

    }

    interface Presenter : JFGPresenter<View> {

        fun createNewFace(faceId: String, faceName: String, picture: Bitmap)

    }
}

