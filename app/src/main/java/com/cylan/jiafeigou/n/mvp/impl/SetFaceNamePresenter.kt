package com.cylan.jiafeigou.n.mvp.impl

import android.text.TextUtils
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.n.view.cam.SetFaceNameContact
import com.cylan.jiafeigou.support.OptionsImpl
import com.cylan.jiafeigou.support.Security
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.AESUtil
import com.cylan.jiafeigou.utils.MiscUtils
import com.google.gson.Gson
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheMode
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/10/9.
 */
class SetFaceNamePresenter @Inject constructor(view: SetFaceNameContact.View) : BasePresenter<SetFaceNameContact.View>(view), SetFaceNameContact.Presenter {

    /**
     * 4.人脸修改接口（客户端）

    参数	说明
    face_id	人脸注册图像标识【必填项】
    person_id	人唯一标识【必填项】
    image_url	图像文件【必填项】客户端抠完之后的小图（只有一张人脸）
    face_name	人脸注册名称【必填项】
    group_id	人脸分组标识（选填项）
    account	用户账户标识（选填项）
    sn	设备标识（选填项）cid
    access_token	【必填项】
     * */
    override fun setFaceName( personId: String, faceName: String) {
        val subscribe = Observable.create<DpMsgDefine.ResponseHeader> { subscriber ->
            try {
                val account = DataSourceManager.getInstance().account.account
                val vid = Security.getVId()
                val serviceKey = OptionsImpl.getServiceKey(vid)
                val timestamp = (System.currentTimeMillis() / 1000).toString()//这里的时间是秒
                val seceret = OptionsImpl.getServiceSeceret(vid)
                val accessToken = BaseApplication.getAppComponent().getCmd().sessionId
                if (TextUtils.isEmpty(serviceKey) || TextUtils.isEmpty(seceret)) {
                    subscriber.onError(IllegalArgumentException("ServiceKey或Seceret为空"))
                } else {
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
                            //上面是全局参数,下面是接口参数
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_ACCOUNT, account)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SN, uuid)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_PERSON_ID, personId)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_NAME, faceName)
                            .params(JConstant.RobotCloudApi.ACCESS_TOKEN, accessToken)
                            .execute()

                    val body = response.body()

                    if (body != null) {
                        val string = body.string()
                        AppLogger.w(string)
                        val gson = Gson()
                        val header = gson.fromJson<DpMsgDefine.ResponseHeader>(string, DpMsgDefine.ResponseHeader::class.java)
                        subscriber.onNext(header)
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
                .subscribe({ rsp ->
                    if (rsp != null && rsp.ret == 0) {
                        mView.onSetFaceNameSuccess()
                    } else {
                        // TODO: 2017/10/13 怎么处理呢? 最好不处理
                        mView.onSetFaceNameError(rsp?.ret)
                    }
                }

                ) { e -> AppLogger.e(MiscUtils.getErr(e)) }
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_DESTROY, "SetFaceNamePresenter#setFaceName", subscribe)

    }
}