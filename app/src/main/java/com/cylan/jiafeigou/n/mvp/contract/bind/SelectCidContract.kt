package com.cylan.jiafeigou.n.mvp.contract.bind

import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.view.JFGView
import com.cylan.jiafeigou.utils.APObserver

/**
 * Created by yanzhendong on 2017/11/28.
 */
interface SelectCidContract {

    interface View : JFGView {

        fun onScanDogWiFiFinished(result: MutableList<APObserver.ScanResult>)

        fun onScanDogWiFiTimeout()

        fun onSendDogConfigFinished()

        fun onSendDogConfigError()
    }

    interface Presenter : JFGPresenter {

        fun refreshDogWiFi()

        fun sendDogConfig(scanResult: APObserver.ScanResult)
    }
}