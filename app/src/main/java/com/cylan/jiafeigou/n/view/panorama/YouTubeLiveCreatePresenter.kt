package com.cylan.jiafeigou.n.view.panorama

import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.rtmp.youtube.util.YouTubeApi
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.MiscUtils
import com.cylan.jiafeigou.utils.PreferencesUtils
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

/**
 * Created by yanzhendong on 2017/9/7.
 */
class YouTubeLiveCreatePresenter : BasePresenter<YouTubeLiveCreateContract.View>(), YouTubeLiveCreateContract.Presenter {
    override fun createLiveBroadcast(credential: GoogleAccountCredential, title: String?, description: String?, startTime: Long, endTime: Long) {
        val subscribe = Observable.just("createLiveBroadcast")
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
                    YouTubeApi.createLiveEvent(youTube, description, title, startTime, endTime)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { mView.showLoading(R.string.CREATING, false) }
                .doOnTerminate { mView.hideLoading() }
                .subscribe({
                    val json = JacksonFactory.getDefaultInstance().toString(it)
                    PreferencesUtils.putString(JConstant.YOUTUBE_PREF_CONFIGURE, json)
                    mView.onCreateLiveBroadcastSuccess(it)
                    AppLogger.w("返回的结果为:$json")
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
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_DESTROY, "YouTubeLiveCreatePresenter#createLiveBroadcast", subscribe)
    }
}