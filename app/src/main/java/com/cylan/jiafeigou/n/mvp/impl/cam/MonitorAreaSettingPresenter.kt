package com.cylan.jiafeigou.n.mvp.impl.cam

import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.n.mvp.contract.cam.MonitorAreaSettingContact
import com.cylan.jiafeigou.support.log.AppLogger
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/11/22.
 */
class MonitorAreaSettingPresenter @Inject constructor(view: MonitorAreaSettingContact.View)
    : BasePresenter<MonitorAreaSettingContact.View>(view), MonitorAreaSettingContact.Presenter {
    override fun loadMonitorPicture() {
        val subscribe = Observable.just("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1511339880450&di=36b3d62c85f7253086dc18a7ca24bf3b&imgtype=0&src=http%3A%2F%2Ff.hiphotos.baidu.com%2Fimage%2Fpic%2Fitem%2Fd788d43f8794a4c2688360c704f41bd5ac6e39bd.jpg")
                .delay(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { mView.showLoadingBar() }
                .doOnTerminate { mView.hideLoadingBar() }
                .subscribe({
                    mView.onGetMonitorPictureSuccess(it)
                }) {
                    it.printStackTrace()
                    AppLogger.e(it)
                    mView.onGetMonitorPictureError()
                }
        addDestroySubscription(subscribe)
    }
}