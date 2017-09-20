package com.cylan.jiafeigou.n.view.panorama

import com.cylan.entity.jniCall.JFGDPMsg
import com.cylan.entity.jniCall.RobotoGetDataRsp
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.dp.DpUtils
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.rtmp.youtube.util.YouTubeApi
import com.cylan.jiafeigou.rx.RxBus
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.MiscUtils
import com.cylan.utils.ContextUtils
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by yanzhendong on 2017/9/7.
 */
class YouTubeLiveSettingPresenter : BasePresenter<YouTubeLiveSetting.View>(), YouTubeLiveSetting.Presenter {
    override fun getLiveList(credential: GoogleAccountCredential, liveBroadcastID: String?) {
        AppLogger.w("YOUTUBE:getLiveList ,the id is $liveBroadcastID")
        val subscribe = Observable.just("getLiveList")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map {

                    val youTube = YouTube.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            JacksonFactory.getDefaultInstance(),
                            credential
                    )
                            .setApplicationName(ContextUtils.getContext().getString(R.string.app_name))
                            .build()
                    YouTubeApi.getLiveEvents(youTube, liveBroadcastID)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mView.onLiveEventResponse(it[0])
                    AppLogger.w("YOUTUBE: ${it[0]},${it[0].title}")
                }, {
                    when (it) {
                        is GooglePlayServicesAvailabilityIOException -> {
                            mView.showGooglePlayServicesAvailabilityErrorDialog(it.connectionStatusCode)
                        }
                        is UserRecoverableAuthIOException -> {
                            mView.onUserRecoverableAuthIOException(it)
                        }
                        else -> {
                            AppLogger.w(MiscUtils.getErr(it))
                        }
                    }
                })
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_DESTROY, "YouTubeLiveSettingPresenter#getLiveList", subscribe)
    }

    override fun getLiveFromDevice() {
        val subscribe = Observable.just("getLiveFromDevice")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map { BaseApplication.getAppComponent().cmd.robotGetData(uuid, arrayListOf(JFGDPMsg(517, 0, byteArrayOf(0))), 1, false, 0) }
                .flatMap { seq -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp::class.java).filter { it.seq == seq } }
                .first()
                .map {
                    val dp = it.map[517]
                    val get = dp?.get(0)
                    DpUtils.unpackData(get?.packValue, DpMsgDefine.DPCameraLiveRtmpStatus::class.java)
                }
                .onErrorResumeNext(Observable.just(null))
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    //                    mView.onLiveEventResponse(it)
                }, {

                })
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_DESTROY, "YouTubeLiveSettingPresenter#getLiveFromDevice", subscribe)
    }
}