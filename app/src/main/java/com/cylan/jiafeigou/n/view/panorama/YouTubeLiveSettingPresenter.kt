package com.cylan.jiafeigou.n.view.panorama

import com.cylan.entity.jniCall.JFGDPMsg
import com.cylan.entity.jniCall.RobotoGetDataRsp
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.dp.DpUtils
import com.cylan.jiafeigou.module.Command
import com.cylan.jiafeigou.rtmp.youtube.util.EventData
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/9/7.
 */
class YouTubeLiveSettingPresenter @Inject constructor(view: YouTubeLiveSetting.View) : BasePresenter<YouTubeLiveSetting.View>(view), YouTubeLiveSetting.Presenter {
    override fun getLiveList(credential: GoogleAccountCredential, liveBroadcastID: String?) {
        AppLogger.w("YOUTUBE:getLiveList ,the id is $liveBroadcastID")
        val subscribe =
                Observable.create<List<EventData>> { subscriber ->
                    val youTube = YouTube.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            JacksonFactory.getDefaultInstance(),
                            credential
                    )
                            .setApplicationName(ContextUtils.getContext().getString(R.string.app_name))
                            .build()
                    val liveEvents = YouTubeApi.getLiveEvents(youTube, liveBroadcastID)
                    subscriber.onNext(liveEvents)
                    subscriber.onCompleted()
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
        addDestroySubscription(subscribe)
    }

    override fun getLiveFromDevice() {
        val subscribe =
                Observable.create<RobotoGetDataRsp> { subscriber ->
                    val seq = Command.getInstance()
                            .robotGetData(uuid, arrayListOf(JFGDPMsg(517, 0, byteArrayOf(0))), 1, false, 0)
                    RxBus.getCacheInstance().toObservable(RobotoGetDataRsp::class.java).filter { it.seq == seq }.subscribe {
                        subscriber.onNext(it)
                        subscriber.onCompleted()
                    }
                }
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
                            AppLogger.e(MiscUtils.getErr(it))
                        })
        addDestroySubscription(subscribe)
    }
}