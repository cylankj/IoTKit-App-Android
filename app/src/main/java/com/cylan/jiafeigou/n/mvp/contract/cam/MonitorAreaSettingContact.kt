package com.cylan.jiafeigou.n.mvp.contract.cam

import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.view.JFGView

/**
 * Created by yanzhendong on 2017/11/21.
 */
interface MonitorAreaSettingContact {

    interface View : JFGView {

        fun onGetMonitorPictureSuccess(url: String)

        fun onGetMonitorPictureError()

        fun showLoadingBar()

        fun hideLoadingBar()

        fun onSetMonitorAreaSuccess()

        fun onSetMonitorAreaError()
    }

    interface Presenter : JFGPresenter {

        fun loadMonitorPicture()

        fun setMonitorArea(uuid: String, rects: FloatArray)

    }
}