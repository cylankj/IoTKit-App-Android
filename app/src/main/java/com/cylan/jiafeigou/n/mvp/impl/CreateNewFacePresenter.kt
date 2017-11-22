package com.cylan.jiafeigou.n.mvp.impl

import android.text.TextUtils
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.n.view.cam.CreateFaceContact
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
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/10/14.
 */
class CreateNewFacePresenter @Inject constructor(view: CreateFaceContact.View) : BasePresenter<CreateFaceContact.View>(view), CreateFaceContact.Presenter {


    override fun createNewFace(faceId: String, faceName: String) {
        val method = method()
        AppLogger.d("正在创建 Face,face id:$faceId,face name:$faceName")
        val subscribe = Observable.create<DpMsgDefine.GenericResponse> { subscriber ->
            val account = DataSourceManager.getInstance().account.account
            var vid = Security.getVId()
            vid="0001"
            val serviceKey = OptionsImpl.getServiceKey(vid)
            val timestamp = (System.currentTimeMillis() / 1000).toString()//这里的时间是秒
            val seceret = OptionsImpl.getServiceSeceret(vid)
            var imageUrl = String.format(Locale.getDefault(), "/7day/%s/%s/AI/%s/%s.jpg", vid, account, uuid, faceId)
            imageUrl = BaseApplication.getAppComponent().getCmd().getSignedCloudUrl(DataSourceManager.getInstance().storageType, imageUrl)
            val sessionId = BaseApplication.getAppComponent().getCmd().sessionId
            if (TextUtils.isEmpty(serviceKey) || TextUtils.isEmpty(seceret)) {
                throw IllegalArgumentException("ServiceKey或Seceret为空")
            }

            val sign = AESUtil.sign(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_ADD_API, seceret, timestamp)
            val serverRsp = OptionsImpl.getRobotServer(uuid, vid)
            var url = serverRsp.host + ":" + serverRsp.port + JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_ADD_API
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

                    .params(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_ID, faceId)
                    .params(JConstant.RobotCloudApi.ROBOTSCLOUD_IMAGE_URL, imageUrl)
                    .params(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_NAME, faceName)
                    .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SN, uuid)
                    .params(JConstant.RobotCloudApi.ROBOTSCLOUD_PERSON_NAME, faceName)
                    .params(JConstant.RobotCloudApi.ROBOTSCLOUD_ACCOUNT, account)
                    .params(JConstant.RobotCloudApi.ACCESS_TOKEN, sessionId)
                    .execute()

            val body = response.body()

            val string = body?.string()
            AppLogger.w(string)
            val gson = Gson()
            val header = gson.fromJson<DpMsgDefine.GenericResponse>(string, DpMsgDefine.GenericResponse::class.java)
            subscriber.onNext(header)
            subscriber.onCompleted()
        }
                .subscribeOn(Schedulers.io())
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(applyLoading(R.string.LOADING, method))
                .subscribe({ response ->
                    when {
                        response == null -> {
                            //返回的结果为空,可能是超时了或者出错了
                            mView.onCreateNewFaceTimeout()
                        }
                        response.ret == 0 -> {
                            //操作成功了
                            if (!TextUtils.isEmpty(response.data)) {
                                mView.onCreateNewFaceSuccess(response.data)
                            } else {
                                //这是什么情况呢? 操作成功了数据没有?
                                mView.onCreateNewFaceError(-1)
                            }
                        }
                        response.ret == 100 -> {
                            //auth failed 可能会有多种情况,1:accessToken error;2:service_key,service_secret error;这里无法判断
                            PreferencesUtils.remove(JConstant.ROBOT_SERVICES_KEY)
                            PreferencesUtils.remove(JConstant.ROBOT_SERVICES_SECERET)
                            mView.onCreateNewFaceError(response.ret)
                        }
                        response.ret == -1 -> {
                            //face_id not found
                            mView.onFaceNotExistError()
                        }

                    }

                }) { e ->
                    mView.onCreateNewFaceError(-1)
                    AppLogger.e(MiscUtils.getErr(e))
                }
        addDestroySubscription(subscribe)
    }


}