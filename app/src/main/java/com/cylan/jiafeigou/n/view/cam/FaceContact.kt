package com.cylan.jiafeigou.n.view.cam

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

