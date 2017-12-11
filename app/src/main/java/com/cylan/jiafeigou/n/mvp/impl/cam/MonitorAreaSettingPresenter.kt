package com.cylan.jiafeigou.n.mvp.impl.cam

import com.cylan.entity.jniCall.JFGDPMsg
import com.cylan.entity.jniCall.RobotoGetDataRsp
import com.cylan.jfgapp.interfases.AppCmd
import com.cylan.jiafeigou.BuildConfig
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.module.DataSourceManager
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
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/11/22.
 */
class MonitorAreaSettingPresenter @Inject constructor(view: MonitorAreaSettingContact.View)
    : BasePresenter<MonitorAreaSettingContact.View>(view), MonitorAreaSettingContact.Presenter {

    override fun loadMonitorAreaSetting() {
        val subscribe = CompositeSubscription()
        val subscribe1 = loadSavedMonitorPicture().subscribe({
            if (it?.ret != 0) {
                mView.onGetMonitorPictureError()
            } else if (it.ret == 0) {
                mView.onGetMonitorPictureSuccess("cylan:///$uuid/tmp/${it.time}.jpg?regionType=${it.ossType}")
            }
        }) {}
        val subscribe2 = loadSavedMonitorArea()
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .subscribe({
                    if (it?.enable == true) {
                        mView.onRestoreMonitorAreaSetting(it.rects!!)
                    } else {
                        mView.onRestoreDefaultMonitorAreaSetting()
                    }
                }) {}
        subscribe.add(subscribe1)
        subscribe.add(subscribe2)
        addDestroySubscription(subscribe)
    }

    private fun loadSavedMonitorArea(): Observable<DpMsgDefine.DPCameraWarnArea?> {
        return Observable.create<Long> {
            val params = arrayListOf(JFGDPMsg(DpMsgMap.ID_519_CAM_WARNAREA, 0))
            val seq = appCmd.robotGetData(uuid, params, 1, false, 0)
            it.onNext(seq)
            it.onCompleted()
        }
                .subscribeOn(Schedulers.io())
                .flatMap { seq -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp::class.java).filter { it.seq == seq } }
                .first()
                .map {
                    val msg = it?.map?.get(DpMsgMap.ID_519_CAM_WARNAREA)?.getOrNull(0)
                    var warnArea: DpMsgDefine.DPCameraWarnArea? = null
                    if (msg != null) {
                        warnArea = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPCameraWarnArea::class.java)
                        AppLogger.w("读取服务器上保存的侦测区域值为:$warnArea")
                    }
                    return@map warnArea
                }
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun loadSavedMonitorPicture(): Observable<DpMsgDefine.DPCameraTakePictureRsp> {
        val dpList = DPList()
        dpList.add(DPMessage(DpMsgMap.ID_521_CAMERA_TAKEPICTURE, 0, DpUtils.pack(true)))
        return RobotForwardDataV3Request(callee = uuid, action = 41, values = dpList)
                .execute()
                .doOnSubscribe { mView.tryGetLocalMonitorPicture() }
                .map {
                    it.values.singleOrNull { it.msgId == DpMsgMap.ID_522_CAMERA_TAKEPICTURE_RSP }
                            .let { DpUtils.unpackDataWithoutThrow(it?.value, DpMsgDefine.DPCameraTakePictureRsp::class.java, null) }
                }
                .first()
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())

    }

    fun loadMonitorSetting() {
        val subscribe = Observable.create<Long> {
            val params = arrayListOf(JFGDPMsg(DpMsgMap.ID_519_CAM_WARNAREA, 0))
            val seq = appCmd.robotGetData(uuid, params, 1, false, 0)
            it.onNext(seq)
            it.onCompleted()
        }
                .subscribeOn(Schedulers.io())
                .flatMap { seq -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp::class.java).filter { it.seq == seq } }
                .first()
                .map {
                    val msg = it?.map?.get(DpMsgMap.ID_519_CAM_WARNAREA)?.getOrNull(0)
                    var warnArea: DpMsgDefine.DPCameraWarnArea? = null
                    if (msg != null) {
                        warnArea = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPCameraWarnArea::class.java)
                    }
                    return@map warnArea
                }
                .timeout(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    when {
                        it == null -> {
                            mView.onRestoreDefaultMonitorAreaSetting()
                        }
                        it.enable -> {
                            mView.onRestoreMonitorAreaSetting(it.rects)
                        }
                    }
                }) {
                    it.printStackTrace()
                }
        addDestroySubscription(subscribe)
    }

    @Inject lateinit var appCmd: AppCmd
    private val TAKE_PICTURE_ACTION =
            +1 shl 0/*转发给对端:0-否，1-是*/ +
                    +1 shl 1/*get/set:0-get，1-set*/ +
                    +1 shl 2 /*对端应答:0-否，1-是*/

    override fun setMonitorArea(uuid: String, enable: Boolean, rects: MutableList<DpMsgDefine.Rect4F>) {
        AppLogger.w("正在设置侦测区域:$rects")
        val warnArea = DpMsgDefine.DPCameraWarnArea(enable, rects)
        val subscribe = Observable.create<Long> {
            val params = arrayListOf(JFGDPMsg(519, 0, DpUtils.pack(warnArea)))
            it.onNext(appCmd.robotSetData(uuid, params))
            it.onCompleted()
        }
                .subscribeOn(Schedulers.io())
                .flatMap { seq -> RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp::class.java).filter { it.seq == seq } }
                .first()
                .map {
                    val success = it?.rets?.getOrNull(0)?.ret == 0
                    val version = it?.rets?.getOrNull(0)?.version ?: System.currentTimeMillis()
                    if (success) {
                        val device = DataSourceManager.getInstance().getDevice(uuid)
                        val dpEntity = device.getEmptyProperty(DpMsgMap.ID_519_CAM_WARNAREA)
                        dpEntity.setValue(warnArea, DpUtils.pack(warnArea), version)
                        device.updateProperty(DpMsgMap.ID_519_CAM_WARNAREA, dpEntity)
                    }
                    success
                }
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(applyLoading(false, R.string.LOADING))
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

    fun loadMonitorPicture() {
        val dpList = DPList()
        dpList.add(DPMessage(DpMsgMap.ID_521_CAMERA_TAKEPICTURE, 0, DpUtils.pack(true)))
        val subscribe = RobotForwardDataV3Request(callee = uuid, action = TAKE_PICTURE_ACTION, values = dpList)
                .execute()
                .map {
                    it.values.singleOrNull { it.msgId == DpMsgMap.ID_522_CAMERA_TAKEPICTURE_RSP }
                            .let { DpUtils.unpackDataWithoutThrow(DpUtils.pack(it?.value), DpMsgDefine.DPCameraTakePictureRsp::class.java, null) }
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