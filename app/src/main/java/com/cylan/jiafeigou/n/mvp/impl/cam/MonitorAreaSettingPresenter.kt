package com.cylan.jiafeigou.n.mvp.impl.cam

import com.cylan.entity.jniCall.JFGDPMsg
import com.cylan.jfgapp.interfases.AppCmd
import com.cylan.jiafeigou.BuildConfig
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.dp.DpMsgMap
import com.cylan.jiafeigou.dp.DpUtils
import com.cylan.jiafeigou.module.message.DPList
import com.cylan.jiafeigou.module.message.DPMessage
import com.cylan.jiafeigou.module.request.RobotForwardDataV3Request
import com.cylan.jiafeigou.n.mvp.contract.cam.MonitorAreaSettingContact
import com.cylan.jiafeigou.rx.RxBus
import com.cylan.jiafeigou.rx.RxEvent
import com.cylan.jiafeigou.support.log.AppLogger
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/11/22.
 */
class MonitorAreaSettingPresenter @Inject constructor(view: MonitorAreaSettingContact.View)
    : BasePresenter<MonitorAreaSettingContact.View>(view), MonitorAreaSettingContact.Presenter {
    @Inject lateinit var appCmd: AppCmd
    private val TAKE_PICTURE_ACTION =
            +1 shl 0/*转发给对端:0-否，1-是*/ +
                    +1 shl 1/*get/set:0-get，1-set*/ +
                    +1 shl 2 /*对端应答:0-否，1-是*/

    override fun setMonitorArea(uuid: String, rects: FloatArray) {
        AppLogger.w("正在设置侦测区域:${Arrays.toString(rects)}")
        val subscribe = Observable.create<Long> {
            val rect4F = DpMsgDefine.Rect4F(rects[0], rects[1], rects[2], rects[3])
            val warnArea = DpMsgDefine.DPCameraWarnArea(true, mutableListOf(rect4F))
            val params = arrayListOf(JFGDPMsg(519, 0, DpUtils.pack(warnArea)))
            it.onNext(appCmd.robotSetData(uuid, params))
            it.onCompleted()
        }
                .subscribeOn(Schedulers.io())
                .flatMap { seq -> RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp::class.java).filter { it.seq == seq } }
                .first()
                .map { it?.rets?.getOrNull(0)?.ret == 0 }
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(applyLoading(R.string.LOADING))
                .subscribe({
                    AppLogger.d("设置侦测区域返回值为:$it")
                    when (it) {
                        null, false -> {
                            mView.onSetMonitorAreaError()
                        }
                        true -> {
                            mView.onSetMonitorAreaSuccess()
                        }
                    }
                }) {
                    it.printStackTrace()
                    AppLogger.e(it)
                }
        addDestroySubscription(subscribe)
    }

    override fun loadMonitorPicture() {
        val dpList = DPList()
        dpList.add(DPMessage(DpMsgMap.ID_521_CAMERA_TAKEPICTURE, 0, DpUtils.pack(true)))
        val subscribe = RobotForwardDataV3Request(callee = uuid, action = TAKE_PICTURE_ACTION, values = dpList)
                .execute()
                .map {
                    it.values.singleOrNull { it.msgId == DpMsgMap.ID_522_CAMERA_TAKEPICTURE_RSP }
                            .let { DpUtils.unpackDataWithoutThrow(it?.value, DpMsgDefine.DPCameraTakePictureRsp::class.java, null) }
                }
                .first()
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { mView.showLoadingBar() }
                .doOnTerminate { mView.hideLoadingBar() }
                .subscribe({
                    when {
                        it == null || it.ret != 0 -> {
                            if (BuildConfig.DEBUG) {
                                mView.onGetMonitorPictureSuccess("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1511339880450&di=36b3d62c85f7253086dc18a7ca24bf3b&imgtype=0&src=http%3A%2F%2Ff.hiphotos.baidu.com%2Fimage%2Fpic%2Fitem%2Fd788d43f8794a4c2688360c704f41bd5ac6e39bd.jpg")
                            } else {
                                mView.onGetMonitorPictureError()
                            }
                        }
                        else -> {
                            mView.onGetMonitorPictureSuccess("cylan:///$uuid/tmp/${it.time}.jpg?regionType=${it.ossType}")
                        }
                    }
                }) {
                    it.printStackTrace()
                    AppLogger.e(it)
                    mView.onGetMonitorPictureError()
                }
        addDestroySubscription(subscribe)
    }
}