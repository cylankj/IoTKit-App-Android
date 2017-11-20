package com.cylan.jiafeigou.n.mvp.impl.cam

import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.module.DoorLockHelper
import com.cylan.jiafeigou.n.mvp.contract.cam.DoorPassWordSettingContact
import com.cylan.jiafeigou.support.log.AppLogger
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/11/20.
 */
class DoorPasswordSettingPresenter @Inject constructor(view: DoorPassWordSettingContact.View) :
        BasePresenter<DoorPassWordSettingContact.View>(view),
        DoorPassWordSettingContact.Presenter {
    override fun changePassWord(oldPassword: String, newPassword: String) {
        val method = method()
        val subscribe = DoorLockHelper.changePassword(uuid, oldPassword, newPassword)
                .compose(applyLoading(R.string.LOADING, method))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    when (it) {
                        true -> {
                            mView.onChangePasswordSuccess()
                        }
                        false -> {
                            mView.onChangePasswordError()
                        }
                    }
                    AppLogger.w("修改门锁密码返回值:$it")
                }) {
                    it.printStackTrace()
                    AppLogger.e(it)
                }
        addDestroySubscription(subscribe)
    }
}