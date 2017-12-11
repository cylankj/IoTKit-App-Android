package com.cylan.jiafeigou.n.mvp.impl.cam

import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.module.DoorLockHelper
import com.cylan.jiafeigou.n.mvp.contract.cam.DoorPassWordSettingContact
import com.cylan.jiafeigou.support.log.AppLogger
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
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
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(applyLoading(false, R.string.LOADING))
                .subscribe({
                    when (it) {
                        true -> {
                            mView.onChangePasswordSuccess()
                        }
                        null, false -> {
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