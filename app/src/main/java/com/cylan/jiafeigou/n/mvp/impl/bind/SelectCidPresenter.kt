package com.cylan.jiafeigou.n.mvp.impl.bind

import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.n.mvp.contract.bind.SelectCidContract
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.APObserver
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/11/28.
 */
class SelectCidPresenter @Inject constructor(view: SelectCidContract.View) : BasePresenter<SelectCidContract.View>(view), SelectCidContract.Presenter {
    override fun refreshDogWiFi() {
        val subscribe = APObserver.scanDogWiFi()
                .timeout(7, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(applyLoading(R.string.LOADING))
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
        addDestroySubscription(subscribe)
    }
}