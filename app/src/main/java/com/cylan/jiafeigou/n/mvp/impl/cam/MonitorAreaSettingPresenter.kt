package com.cylan.jiafeigou.n.mvp.impl.cam

import com.cylan.entity.jniCall.JFGDPMsg
import com.cylan.entity.jniCall.RobotoGetDataRsp
import com.cylan.jfgapp.interfases.AppCmd
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
import com.cylan.jiafeigou.utils.MiscUtils
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/11/22.
 */
class MonitorAreaSettingPresenter @Inject constructor(view: MonitorAreaSettingContact.View)
    : BasePresenter<MonitorAreaSettingContact.View>(view), MonitorAreaSettingContact.Presenter {

    override fun loadMonitorAreaSetting() {
        val subscribe = Observable.zip(loadSavedMonitorArea(), loadSavedMonitorPicture(), { areaSetting, savedPicture -> Pair(areaSetting, savedPicture) })
                .timeout(32, TimeUnit.SECONDS, Observable.just(null))
                .subscribe({
//                    var remoteURL = if (it?.second?.ret != 0) null else "cylan:///$uuid/tmp/${it.second?.time}.jpg?regionType=${it.second?.ossType}"
//                    var motionAreaSetting = if (it?.first?.enable == true) it.first?.rects!! else null
//                    mView.onLoadMotionAreaSettingFinished(remoteURL,motionAreaSetting)
                    if (it?.second?.ret != 0) {
                        mView.onGetMonitorPictureError();
                    } else if (it.second?.ret == 0) {
                        mView.onGetMonitorPictureSuccess("cylan:///$uuid/tmp/${it.second?.time}.jpg?regionType=${it.second?.ossType}")
                    }
                    if (it?.first?.enable == true) {
                        mView.onRestoreMonitorAreaSetting(it.first?.rects!!)
                    } else {
                        mView.onRestoreDefaultMonitorAreaSetting()
                    }
                })
                {
                    it.printStackTrace()
                    AppLogger.e(MiscUtils.getErr(it))
                }
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

    @Inject
    lateinit var appCmd: AppCmd

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
}