package com.cylan.jiafeigou.n.mvp.impl

import android.text.TextUtils
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.dp.DpUtils
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.n.view.cam.FaceListContact
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
import java.util.concurrent.TimeoutException

/**
 * Created by yanzhendong on 2017/10/16.
 */
class FaceListPresenter : BasePresenter<FaceListContact.View>(), FaceListContact.Presenter {

    /**
     * face_id	人脸注册图像标识【必填项】
    person_id	人唯一标识【必填项】
    image_url	图像文件【必填项】客户端抠完之后的小图（只有一张人脸）
    face_name	人脸注册名称【必填项】
    group_id	人脸分组标识（选填项）
    account	用户账户标识（必填项）
    sn	设备标识（必填项）cid
    access_token	【必填项】
     * */

    override fun moveFaceToPerson(personId: String, faceId: String) {
        val subscribe = Observable.create<DpMsgDefine.ResponseHeader> { subscriber ->
            try {
                val vid = Security.getVId()
                val serviceKey = OptionsImpl.getServiceKey(vid)
                val timestamp = (System.currentTimeMillis() / 1000).toString()//这里的时间是秒
                val seceret = OptionsImpl.getServiceSeceret(vid)
                val sessionId = BaseApplication.getAppComponent().cmd.sessionId
                val account = DataSourceManager.getInstance().account.account
                if (TextUtils.isEmpty(serviceKey) || TextUtils.isEmpty(seceret)) {
                    subscriber.onError(IllegalArgumentException("ServiceKey或Seceret为空"))
                } else {
                    val sign = AESUtil.sign(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_UPDATE_API, seceret, timestamp)
                    var url = OptionsImpl.getRobotServer() + JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_UPDATE_API
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
                            .params(JConstant.RobotCloudApi.ACCESS_TOKEN, sessionId)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_ID, faceId)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_PERSON_ID, personId)
                            .execute()

                    val body = response.body()

                    if (body != null) {
                        val string = body.string()
                        AppLogger.w(string)
                        val gson = Gson()
                        val header = gson.fromJson<DpMsgDefine.ResponseHeader>(string, DpMsgDefine.ResponseHeader::class.java)
                        if (header.ret == 100) {
                            PreferencesUtils.remove(JConstant.ROBOT_SERVICES_KEY)
                            PreferencesUtils.remove(JConstant.ROBOT_SERVICES_SECERET)
                        }
                        subscriber.onNext(header)
                        subscriber.onCompleted()
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
                    when {
                        rsp == null -> {
                            //返回结果为空?
                            AppLogger.w("修改面孔信息返回了 null, 可能是超时或者服务器错误!")
                        }
                        rsp.ret == 0 -> {
                            //移动 face 成功了
                            mView.onMoveFaceToPersonSuccess("todo:person_id")
                        }
                        rsp.ret == -1 -> {
                            //face_id 不存在
                            mView.onFaceNotExistError()
                        }
                    }
                }

                ) { e -> AppLogger.e(MiscUtils.getErr(e)) }
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_DESTROY, "FaceListPresenter#moveFaceToPerson", subscribe)
    }

    override fun loadPersonItems(account: String, uuid: String) {
        val subscribe = Observable.create<DpMsgDefine.FaceQueryResponse> { subscriber ->
            try {
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
                            .cacheMode(CacheMode.NO_CACHE)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_VID, vid)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICE_KEY, serviceKey)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_BUSINESS, "1")
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICETYPE, "1")
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SIGN, sign)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_TIMESTAMP, timestamp)

                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_ACCOUNT, account)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SN, uuid)
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
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_DESTROY, "FaceManagerPresenter#loadPersonItems", subscribe)
    }

   override fun loadPersonItem2() {
        val subscribe = Observable.just("loadPersonItem2")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .flatMap {
                    val seq = BaseApplication.getAppComponent().cmd
                            .sendUniservalDataSeq(5, DpUtils.pack(DpMsgDefine.ReqContent(uuid, System.currentTimeMillis())))
                    RxBus.getCacheInstance().toObservable(RxEvent.UniversalDataRsp::class.java)
                            .first { it.seq == seq }.timeout(30, TimeUnit.SECONDS)
                            .map {
                                val visitorList = DpUtils.unpackData(it.data, DpMsgDefine.VisitorList::class.java)
                                visitorList.dataList
                            }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mView.onVisitorInformationReady(it)
                }, {
                    when (it) {
                        is TimeoutException -> {
                            //超时了
                        }

                    }

                    AppLogger.e(MiscUtils.getErr(it))
                })

        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_DESTROY, "FaceListPresenter#loadPersonItem2", subscribe)
    }
}