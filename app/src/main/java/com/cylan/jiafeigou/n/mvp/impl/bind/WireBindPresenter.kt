package com.cylan.jiafeigou.n.mvp.impl.bind

import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.misc.pty.PropertiesLoader
import com.cylan.jiafeigou.module.SubscriptionSupervisor
import com.cylan.jiafeigou.n.mvp.contract.bind.WireBindContract
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.APObserver
import com.cylan.jiafeigou.widget.LoadingDialog
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/11/28.
 */
class WireBindPresenter @Inject constructor(view: WireBindContract.View) : BasePresenter<WireBindContract.View>(view)
        , WireBindContract.Presenter {
    var isScan: Boolean = false
    override fun scanDogWiFi() {
        val traceElement = Thread.currentThread().stackTrace[2]
        val method = javaClass.name + ":stop:" + traceElement.methodName
        val subscribe = APObserver.scanDogWiFi()
                .map {
                    it?.filter {
                        PropertiesLoader.getInstance().hasProperty(it.os, "WIREDMODE")
                                && !DataSourceManager.getInstance().getDevice(it.uuid).available()
                    }?.toMutableList()
                }
                .timeout(7, TimeUnit.SECONDS, Observable.just(null))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    isScan = true
                    LoadingDialog.showLoading(mView.activity(), mContext.getString(R.string.addvideo_searching), true, { _ ->
                     SubscriptionSupervisor.unsubscribe(this@WireBindPresenter,SubscriptionSupervisor.CATEGORY_STOP,method)
                    })
                }
                .doOnTerminate {
                    LoadingDialog.dismissLoading()
                    isScan=false
                }
                .doOnUnsubscribe {
                    LoadingDialog.dismissLoading()
                    isScan=false
                }
//                .compose(applyLoading(true, R.string.addvideo_searching))
                .subscribe({
                    when (it) {
                        null -> {
                            mView.onScanDogWiFiTimeout()
                        }
                        else -> {
                            mView.onScanDogWiFiFinished(it)
                        }
                    }
                }) {
                    it.printStackTrace()
                    AppLogger.e(it)
                }
        AppLogger.w("scanDogWiFi")
        addStopSubscription(subscribe)
    }

    override fun isScanning(): Boolean {
        return false
    }
}