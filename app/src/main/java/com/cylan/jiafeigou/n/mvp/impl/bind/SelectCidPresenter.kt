package com.cylan.jiafeigou.n.mvp.impl.bind

import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.dp.DpUtils
import com.cylan.jiafeigou.misc.JFGRules
import com.cylan.jiafeigou.module.Command
import com.cylan.jiafeigou.n.mvp.contract.bind.SelectCidContract
import com.cylan.jiafeigou.rx.RxBus
import com.cylan.jiafeigou.rx.RxEvent
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.APObserver
import com.cylan.jiafeigou.utils.BindHelper
import com.cylan.jiafeigou.utils.MiscUtils
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/11/28.
 */
class SelectCidPresenter @Inject constructor(view: SelectCidContract.View) : BasePresenter<SelectCidContract.View>(view), SelectCidContract.Presenter {
    override fun sendDogConfig(scanResult: APObserver.ScanResult) {
        val subscribe = BindHelper.sendServerConfig(scanResult.uuid, scanResult.mac, JFGRules.getLanguageType())
                .timeout(5, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(applyLoading(false,R.string.LOADING))
                .subscribe({
                    when (it) {
                        null -> {
                            AppLogger.d("超时了")
                        }
                        else -> {
                            mView.onSendDogConfigFinished()
                        }
                    }
                }) {
                    it.printStackTrace()
                    AppLogger.e(it)
                }
        addStopSubscription(subscribe)
    }

    override fun refreshDogWiFi() {
        val subscribe = APObserver.scanDogWiFi()
                .timeout(7, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(applyLoading(false,R.string.addvideo_searching))
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

    fun getDevicdPid(sn: String) {
        Observable.create<Long> { subscriber ->
            val data = DpUtils.pack(sn)
            val seq = Command.getInstance().sendUniservalDataSeq(1, data)
            subscriber.onNext(seq)
            subscriber.onCompleted()
        }
                .subscribeOn(Schedulers.io())
                .flatMap { seq -> RxBus.getCacheInstance().toObservable(RxEvent.UniversalDataRsp::class.java).filter { it.seq == seq } }
                .map { DpUtils.unpackDataWithoutThrow(it.data, Int::class.java, -1) }
    }
}