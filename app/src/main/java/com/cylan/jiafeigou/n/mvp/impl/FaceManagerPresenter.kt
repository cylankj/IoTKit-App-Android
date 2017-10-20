package com.cylan.jiafeigou.n.mvp.impl

import android.text.TextUtils
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.n.view.cam.FaceManagerContact
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

/**
 * Created by yanzhendong on 2017/10/10.
 */
class FaceManagerPresenter : BasePresenter<FaceManagerContact.View>(), FaceManagerContact.Presenter {
    override fun loadFacesByPersonId(personId: String) {
        val subscribe = Observable.create<DpMsgDefine.FaceQueryResponse> { subscriber ->
            try {
                val account = DataSourceManager.getInstance().account.account
                val vid = Security.getVId()
                val serviceKey = OptionsImpl.getServiceKey(vid)
                val timestamp = (System.currentTimeMillis() / 1000).toString()//这里的时间是秒
                val seceret = OptionsImpl.getServiceSeceret(vid)
                val sessionId = BaseApplication.getAppComponent().cmd.sessionId
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

                    val body = response.body()

                    if (body != null) {
                        val string = body.string()
                        AppLogger.w(string)
                        val gson = Gson()
                        val header = gson.fromJson<DpMsgDefine.ResponseHeader>(string, DpMsgDefine.ResponseHeader::class.java)
                        if (header.ret == 0) {
                            val queryResponse = Gson().fromJson<DpMsgDefine.FaceQueryResponse>(string, DpMsgDefine.FaceQueryResponse::class.java)
                            subscriber.onNext(queryResponse)
                            subscriber.onCompleted()
                        } else {
                            if (header.ret == 100) {
                                PreferencesUtils.remove(JConstant.ROBOT_SERVICES_KEY)
                                PreferencesUtils.remove(JConstant.ROBOT_SERVICES_SECERET)
                            }
                            subscriber.onError(IllegalArgumentException("ret:" + header.ret + ",msg:" + header.msg))
                        }
                    } else {
                        subscriber.onError(null)
                    }
                }
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ rsp ->
                    if (rsp != null && rsp.ret == 0) {
                        mView.onFaceInformationReady(rsp.data)
                    } else {
                        // TODO: 2017/10/13 怎么处理呢? 最好不处理
                    }
                }

                ) { e -> AppLogger.e(MiscUtils.getErr(e)) }
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_DESTROY, "FaceManagerPresenter#loadFacesByPersonId", subscribe)
    }
}