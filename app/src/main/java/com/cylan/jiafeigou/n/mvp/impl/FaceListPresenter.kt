package com.cylan.jiafeigou.n.mvp.impl

import android.text.TextUtils
import com.cylan.jfgapp.interfases.AppCmd
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.dp.DpUtils
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.module.Command
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
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/10/16.
 */
class FaceListPresenter @Inject constructor(view: FaceListContact.View) : BasePresenter<FaceListContact.View>(view), FaceListContact.Presenter {

    @Inject lateinit var appCmd: AppCmd
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
        val method = method()
        val subscribe = Observable.create<DpMsgDefine.ResponseHeader> { subscriber ->
            try {
                var vid = Security.getVId()
                vid = "0001"
                val serviceKey = OptionsImpl.getServiceKey(vid)
                val timestamp = (System.currentTimeMillis() / 1000).toString()//这里的时间是秒
                val seceret = OptionsImpl.getServiceSeceret(vid)
                val sessionId = Command.getInstance().sessionId
                val account = DataSourceManager.getInstance().account.account
                if (TextUtils.isEmpty(serviceKey) || TextUtils.isEmpty(seceret)) {
                    subscriber.onError(IllegalArgumentException("ServiceKey或Seceret为空"))
                } else {
                    val sign = AESUtil.sign(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_ADD_API, seceret, timestamp)
                    val serverRsp = OptionsImpl.getRobotServer(uuid, vid)
                    var url = serverRsp.host + ":" + serverRsp.port + JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_ADD_API
                    if (!url.startsWith("http://")) {
                        url = "http://" + url
                    }
                    val response = OkGo.post(url)
                            .cacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)
                            //TODO 现在 VID 写死成 0001
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
                .subscribeOn(Schedulers.io())
                .timeout(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(applyLoading(false, R.string.LOADING))
                .subscribe({ rsp ->
                    AppLogger.d("修改面孔信息返回的结果为:$rsp,person id is :$personId, face id is :$faceId, uuid is:$uuid")
                    when {
                        rsp == null -> {
                            //返回结果为空?
                            mView.onMoveFaceError()
                            AppLogger.w("修改面孔信息返回了 null, 可能是超时或者服务器错误! ")
                        }
                        rsp.ret == 0 -> {
                            //移动 face 成功了
                            mView.onMoveFaceToPersonSuccess("todo:person_id")
                            AppLogger.w("修改面孔信息成功了")
                        }
                        rsp.ret == -1 -> {
                            //face_id 不存在
                            mView.onFaceNotExistError()
                            AppLogger.w("修改面孔信息失败:面孔不存在")
                        }
                        rsp.ret == 100 -> {
                            //授权失败了
                            mView.onAuthorizationError()
                            AppLogger.w("修改面孔信息失败:授权失败")
                        }
                    }
                }
                ) { e ->
                    mView.onMoveFaceError()
                    AppLogger.e(MiscUtils.getErr(e))
                }
        addDestroySubscription(subscribe)
    }

    override fun loadPersonItems(account: String, uuid: String) {
        val method = method()
        val subscribe = Observable.create<DpMsgDefine.FaceQueryResponse> { subscriber ->
            try {
                var vid = Security.getVId()
                vid = "0001"
                val serviceKey = OptionsImpl.getServiceKey(vid)
                val timestamp = (System.currentTimeMillis() / 1000).toString()//这里的时间是秒
                val seceret = OptionsImpl.getServiceSeceret(vid)
                val sessionId = Command.getInstance().sessionId
                if (TextUtils.isEmpty(serviceKey) || TextUtils.isEmpty(seceret)) {
                    subscriber.onError(IllegalArgumentException("ServiceKey或Seceret为空"))
                } else {
                    val sign = AESUtil.sign(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_QUERY_API, seceret, timestamp)
                    val serverRsp = OptionsImpl.getRobotServer(uuid, vid)
                    var url = serverRsp.host + ":" + serverRsp.port + JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_QUERY_API
                    if (!url.startsWith("http://")) {
                        url = "http://" + url
                    }
                    val response = OkGo.post(url)
                            .cacheMode(CacheMode.NO_CACHE)
                            //TODO 现在 VID 写死成 0001
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(applyLoading(false,R.string.LOADING))
                .subscribe({ rsp ->
                    if (rsp != null && rsp.ret == 0) {
                        mView.onFaceInformationReady(rsp.data)
                    } else {
                        // TODO: 2017/10/13 怎么处理呢? 最好不处理
                    }
                }

                ) { e -> AppLogger.e(MiscUtils.getErr(e)) }
        addDestroySubscription(subscribe)
    }

    override fun loadPersonItem2() {
        val method = method()
        val subscribe = Observable.just("")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map {
                    Command.getInstance().sendUniservalDataSeq(5, DpUtils.pack(DpMsgDefine.ReqContent(uuid, System.currentTimeMillis())))
                }
                .flatMap { seq ->
                    RxBus.getCacheInstance().toObservable(RxEvent.UniversalDataRsp::class.java)
                            .first { it.seq == seq }
                            .map {
                                val visitorList = DpUtils.unpackData(it.data, DpMsgDefine.VisitorList::class.java)
                                visitorList.dataList
                            }
                }
                .first()
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(applyLoading(false,R.string.LOADING))
                .subscribe({

                    mView.onVisitorInformationReady(it)
                }, {
                    when (it) {
                        is TimeoutException -> {
                            //超时了
                        }

                    }
                    it.printStackTrace()
                    mView.onVisitorInformationReady(listOf())
                    AppLogger.e(MiscUtils.getErr(it))
                })
        addDestroySubscription(subscribe)
    }
}