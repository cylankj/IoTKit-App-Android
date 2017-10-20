package com.cylan.jiafeigou.n.mvp.impl

import android.text.TextUtils
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

/**
 * Created by yanzhendong on 2017/10/14.
 */
class CreateNewFacePresenter : BasePresenter<CreateFaceContact.View>(), CreateFaceContact.Presenter {


    override fun createNewFace(faceId: String, faceName: String) {
        val subscribe = Observable.create<DpMsgDefine.GenericResponse> { subscriber ->
            val account = DataSourceManager.getInstance().account.account
            val vid = Security.getVId()
            val serviceKey = OptionsImpl.getServiceKey(vid)
            val timestamp = (System.currentTimeMillis() / 1000).toString()//这里的时间是秒
            val seceret = OptionsImpl.getServiceSeceret(vid)
            var imageUrl = String.format(Locale.getDefault(), "/7day/%s/%s/AI/%s/%s.jpg", vid, account, uuid, faceId)
            imageUrl = BaseApplication.getAppComponent().cmd.getSignedCloudUrl(DataSourceManager.getInstance().storageType, imageUrl)
            if (TextUtils.isEmpty(serviceKey) || TextUtils.isEmpty(seceret)) {
                throw IllegalArgumentException("ServiceKey或Seceret为空")
            }

            val sign = AESUtil.sign(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_ADD_API, seceret, timestamp)
            var url = OptionsImpl.getRobotServer() + JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_ADD_API
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (!TextUtils.isEmpty(response.data)) {
                        //成功了
                        mView.onCreateNewFaceSuccess(response.data)

                    } else {
                        if (response.ret == 100) {
                            PreferencesUtils.remove(JConstant.ROBOT_SERVICES_KEY)
                            PreferencesUtils.remove(JConstant.ROBOT_SERVICES_SECERET)
                        }
                        mView.onCreateNewFaceError(response.ret)
                    }

                }) { e ->
                    mView.onCreateNewFaceError(-1)
                    AppLogger.e(MiscUtils.getErr(e))
                }
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_DESTROY, "CreateNewFacePresenter#createNewFace", subscribe)
    }


}