package com.cylan.jiafeigou.n.mvp.impl

import android.text.TextUtils
import android.util.Log
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.module.Command
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
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.net.ssl.X509TrustManager

/**
 * Created by yanzhendong on 2017/10/14.
 */
class CreateNewFacePresenter @Inject constructor(view: CreateFaceContact.View) : BasePresenter<CreateFaceContact.View>(view), CreateFaceContact.Presenter {


    init {
        setSSL()
    }

    override fun createNewFace(faceId: String, faceName: String) {
        val method = method()
        AppLogger.d("正在创建 Face,face id:$faceId,face name:$faceName")
        val subscribe = Observable.create<DpMsgDefine.GenericResponse> { subscriber ->
            val account = DataSourceManager.getInstance().account.account
            var vid = Security.getVId()
            vid = "0001"
            val serviceKey = OptionsImpl.getServiceKey(vid)
            val timestamp = (System.currentTimeMillis() / 1000).toString()//这里的时间是秒
            val seceret = OptionsImpl.getServiceSeceret(vid)
            var imageUrl = String.format(Locale.getDefault(), "/7day/%s/%s/AI/%s/%s.jpg", vid, account, uuid, faceId)
            imageUrl = Command.getInstance().getSignedCloudUrl(DataSourceManager.getInstance().storageType, imageUrl)
            val sessionId = Command.getInstance().sessionId
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
                .compose(applyLoading(true, R.string.LOADING))
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


    override fun createNewFaceV2(faceId: String, faceName: String) {
        val subscribe = Observable.create<Int> {
            val authToken: String
            val time = System.currentTimeMillis() / 1000L
            try {
                val server = ("https://" + OptionsImpl.getServer() + ":8085").replace(":443", "")
                val authPath = "/authtoken"
                val authApi = server + authPath
                var tokenParams = JSONObject()
                val vid = OptionsImpl.getVid()
                val serviceKey = OptionsImpl.getServiceKey(vid)
                val serviceSeceret = OptionsImpl.getServiceSeceret(vid)
                val account = DataSourceManager.getInstance().account.account
                tokenParams.put("service_key", serviceKey)
                tokenParams.put("time", time)
                tokenParams.put("sign", AESUtil.HmacSHA1Encrypt(String.format(Locale.getDefault(), "%s\n%d", authPath, time), serviceSeceret))
                var execute = OkGo.post(authApi)
                        .requestBody(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), tokenParams.toString()))
                        .execute()
                var jsonObject = JSONObject(execute.body()!!.string())
                Log.e("RegisterFacePresenter", "get token response:" + jsonObject)
                var code = jsonObject.getInt("code")
                if (code != 200) {
                    it.onNext(code)
                    it.onCompleted()
                    return@create
                }

                authToken = jsonObject.getString("auth_token")
                val aiAppApi = server + "/aiapp"
                tokenParams = JSONObject()
                tokenParams.put("action", "RegisterByFaceID")
                tokenParams.put("auth_token", /*authToken*/"JFG_SERVER_PASS_TOKEN_x20180124x")
                tokenParams.put("time", time)
                tokenParams.put("account", account)
                tokenParams.put("cid", uuid)
                tokenParams.put("face_id", faceId)
                tokenParams.put("person_name", faceName)
                execute = OkGo.post(aiAppApi)
                        .requestBody(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), tokenParams.toString()))
                        .execute()
                jsonObject = JSONObject(execute.body()!!.string())
                Log.e("RegisterFacePresenter", "register face response:" + jsonObject)
                code = jsonObject.getInt("code")
                it.onNext(code)
                it.onCompleted()
            } catch (e: Exception) {
                e.printStackTrace()
                AppLogger.e(e)
                it.onNext(-1)
                it.onCompleted()
            }
        }
                .subscribeOn(Schedulers.io())
                .timeout(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(applyLoading(false, R.string.LOADING))
                .subscribe({
                    AppLogger.d("修改面孔信息返回的结果为:$it, face id is :$faceId, uuid is:$uuid")
                    when (it) {
                        200 -> {
                            //移动 face 成功了
                            mView.onCreateNewFaceSuccess("todo:person_id")
                            AppLogger.w("修改面孔信息成功了")
                        }
                        -1 -> {
                            //face_id 不存在
                            mView.onFaceNotExistError()
                            AppLogger.w("修改面孔信息失败:面孔不存在")
                        }
                        100 -> {
                            //授权失败了
                            mView.onAuthorizationError()
                            AppLogger.w("修改面孔信息失败:授权失败")
                        }
                        else -> {
                            mView.onCreateNewFaceError(it)
                            AppLogger.w("创建面孔失败了,错误码")
                        }
                    }
                }) {
                    mView.onCreateNewFaceTimeout()
                    AppLogger.e(MiscUtils.getErr(it))
                }
        addDestroySubscription(subscribe)
    }

    private fun setSSL() {
        OkGo.getInstance().setHostnameVerifier { hostname, session -> true }
        OkGo.getInstance().setCertificates(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }
        })
    }

}