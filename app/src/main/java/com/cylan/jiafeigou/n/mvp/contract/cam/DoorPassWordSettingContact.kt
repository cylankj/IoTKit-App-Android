package com.cylan.jiafeigou.n.mvp.contract.cam

import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.view.JFGView

/**
 * Created by yanzhendong on 2017/11/20.
 */

interface DoorPassWordSettingContact {
    interface View : JFGView {
        fun onChangePasswordSuccess()
        fun onChangePasswordError()
        fun onOldPasswordError()
    }

    interface Presenter : JFGPresenter {
        fun changePassWord(oldPassword: String, newPassword: String)
    }
}