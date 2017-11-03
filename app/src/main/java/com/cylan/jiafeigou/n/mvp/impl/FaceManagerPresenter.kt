package com.cylan.jiafeigou.n.mvp.impl

import android.text.TextUtils
import com.cylan.jfgapp.interfases.AppCmd
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.dp.DpUtils
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.n.view.cam.FaceManagerContact
import com.cylan.jiafeigou.rx.RxBus
import com.cylan.jiafeigou.rx.RxEvent
import com.cylan.jiafeigou.support.OptionsImpl
import com.cylan.jiafeigou.support.Security
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.AESUtil
import com.cylan.jiafeigou.utils.MiscUtils
import com.cylan.jiafeigou.utils.PreferencesUtils
import com.google.gson.Gson
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheMode
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/10/10.
 */
class FaceManagerPresenter @Inject constructor(view: FaceManagerContact.View) : BasePresenter<FaceManagerContact.View>(view), FaceManagerContact.Presenter {
    @Inject lateinit var appCmd: AppCmd

    override fun deleteFace(personId: String?, listOf: List<String>) {
        val method = method()
        val subscribe = Observable.create<DpMsgDefine.ResponseHeader> { subscriber ->
            try {
                val account = DataSourceManager.getInstance().account.account
                val vid = Security.getVId()
                val serviceKey = OptionsImpl.getServiceKey(vid)
                val timestamp = (System.currentTimeMillis() / 1000).toString()//这里的时间是秒
                val seceret = OptionsImpl.getServiceSeceret(vid)
                val sessionId = BaseApplication.getAppComponent().getCmd().sessionId
                if (TextUtils.isEmpty(serviceKey) || TextUtils.isEmpty(seceret)) {
                    subscriber.onError(IllegalArgumentException("ServiceKey或Seceret为空"))
                } else {
                    val sign = AESUtil.sign(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_DELETE_API, seceret, timestamp)
                    var url = OptionsImpl.getRobotServer() + JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_DELETE_API
                    if (!url.startsWith("http://")) {
                        url = "http://" + url
                    }
                    val response = OkGo.post(url)
                            .cacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_VID, vid)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICE_KEY, serviceKey)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_BUSINESS, "1")
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICETYPE, "1")
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SIGN, sign)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_TIMESTAMP, timestamp)

                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_ACCOUNT, account)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SN, uuid)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_PERSON_ID, personId)
                            .params(JConstant.RobotCloudApi.ACCESS_TOKEN, sessionId)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_ID, listOf.toString())
                            .execute()


                    val body = response.body()

                    val string = body?.string()
                    AppLogger.w(string)
                    val gson = Gson()
                    val header = gson.fromJson<DpMsgDefine.ResponseHeader>(string, DpMsgDefine.ResponseHeader::class.java)
                    subscriber.onNext(header)
                    subscriber.onCompleted()
                }
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }
                .subscribeOn(Schedulers.io())
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(applyLoading(R.string.LOADING, method))
                .subscribe({
                    when {
                        it.ret == 0 -> {
                            AppLogger.w("删除面孔成功了!")
                            mView.onDeleteFaceSuccess()
                        }
                        it.ret == -1 -> {
                            AppLogger.w("face_id 不存在")
                            mView.onDeleteFaceError()
                        }
                    }
                }, {
                    mView.onDeleteFaceError()
                    AppLogger.e(MiscUtils.getErr(it))
                })
        addDestroySubscription(subscribe)
    }

    override fun loadFaceByPersonIdByDP(personId: String) {
        val method = method()
        val subscribe = Observable.just(method)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map {
                    appCmd.sendUniservalDataSeq(11, DpUtils.pack(DpMsgDefine.AcquaintanceListReq(uuid, personId)))
                }
                .flatMap { seq -> RxBus.getCacheInstance().toObservable(RxEvent.UniversalDataRsp::class.java).first { seq == it.seq } }
                .map {
                    val acquaintanceListRsp = DpUtils.unpackData(it.data, DpMsgDefine.AcquaintanceListRsp::class.java)
                    acquaintanceListRsp
                }
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .compose(applyLoading(R.string.LOADING, method))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    when (it) {
                        null -> {
                            AppLogger.w("加载面孔列表: null")
                        }
                        else -> {
                            mView.onAcquaintanceReady(it.acquaintanceItems)
                        }
                    }
                }) {

                }
        addStopSubscription(subscribe)
    }

    override fun loadFacesByPersonId(personId: String) {
        val method = method()
        val subscribe =
                Observable.create<DpMsgDefine.ResponseHeader> { subscriber ->
                    try {
                        val account = DataSourceManager.getInstance().account.account
                        val vid = Security.getVId()
                        val serviceKey = OptionsImpl.getServiceKey(vid)
                        val timestamp = (System.currentTimeMillis() / 1000).toString()//这里的时间是秒
                        val seceret = OptionsImpl.getServiceSeceret(vid)
                        val sessionId = BaseApplication.getAppComponent().getCmd().sessionId
                        if (TextUtils.isEmpty(serviceKey) || TextUtils.isEmpty(seceret)) {
                            subscriber.onError(IllegalArgumentException("ServiceKey或Seceret为空"))
                        } else {
                            val sign = AESUtil.sign(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_QUERY_API, seceret, timestamp)
                            var url = OptionsImpl.getRobotServer() + JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_QUERY_API
                            if (!url.startsWith("http://")) {
                                url = "http://" + url
                            }
                            val response = OkGo.post(url)
                                    .cacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)
                                    .params(JConstant.RobotCloudApi.ROBOTSCLOUD_VID, vid)
                                    .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICE_KEY, serviceKey)
                                    .params(JConstant.RobotCloudApi.ROBOTSCLOUD_BUSINESS, "1")
                                    .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICETYPE, "1")
                                    .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SIGN, sign)
                                    .params(JConstant.RobotCloudApi.ROBOTSCLOUD_TIMESTAMP, timestamp)

                                    .params(JConstant.RobotCloudApi.ROBOTSCLOUD_ACCOUNT, account)
                                    .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SN, uuid)
                                    .params(JConstant.RobotCloudApi.ROBOTSCLOUD_PERSON_ID, personId)
                                    .params(JConstant.RobotCloudApi.ACCESS_TOKEN, sessionId)
                                    .execute()
                            AppLogger.w("FaceManagerPresenter:$response")
                            val body = response.body()

                            if (body != null) {
                                val string = body.string()
                                AppLogger.w(string)
                                val gson = Gson()
                                val response = try {
                                    gson.fromJson<DpMsgDefine.FaceQueryResponse>(string, DpMsgDefine.FaceQueryResponse::class.java)
                                } catch (e: Exception) {
                                    gson.fromJson<DpMsgDefine.GenericResponse>(string, DpMsgDefine.GenericResponse::class.java)
                                }
                                subscriber.onNext(response)
                                subscriber.onCompleted()
                            } else {
                                subscriber.onNext(null)
                                subscriber.onCompleted()
                            }
                        }
                    } catch (e: Exception) {
                        subscriber.onError(e)
                    }
                }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(applyLoading(R.string.LOADING, method))
                        .subscribe({ rsp ->
                            when {
                                rsp == null -> {
                                    AppLogger.w("加载面孔列表: null")
                                }
                                rsp.ret == 0 -> {
                                    if (rsp is DpMsgDefine.FaceQueryResponse) {
                                        mView.onFaceInformationReady(rsp.data)
                                    } else {
                                        AppLogger.w("未知错误" + rsp)
                                    }
                                }
                                rsp.ret == 100 -> {
                                    PreferencesUtils.remove(JConstant.ROBOT_SERVICES_KEY)
                                    PreferencesUtils.remove(JConstant.ROBOT_SERVICES_SECERET)
                                    mView.onAuthorizationError()
                                }
                            }
                        }

                        ) { e -> AppLogger.e(MiscUtils.getErr(e)) }
        addDestroySubscription(subscribe)
    }
}