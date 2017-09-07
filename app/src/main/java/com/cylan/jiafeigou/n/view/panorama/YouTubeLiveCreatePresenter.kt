package com.cylan.jiafeigou.n.view.panorama

import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.rtmp.youtube.util.YouTubeApi
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.PreferencesUtils
import com.cylan.utils.ContextUtils
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeScopes
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

/**
 * Created by yanzhendong on 2017/9/7.
 */
class YouTubeLiveCreatePresenter : BasePresenter<YouTubeLiveCreateContract.View>(), YouTubeLiveCreateContract.Presenter {

    private val SCOPES = arrayOf(YouTubeScopes.YOUTUBE_READONLY)
    private val mCredential: GoogleAccountCredential by lazy {
        val credential = GoogleAccountCredential.usingOAuth2(
                mView.appContext, Arrays.asList(*SCOPES))
                .setBackOff(ExponentialBackOff())
        credential.selectedAccountName = PreferencesUtils.getString(JConstant.YOUTUBE_PREF_ACCOUNT_NAME)
        credential
    }


    override fun createLiveBroadcast(title: String?, description: String?, startTime: Long, endTime: Long) {
        val subscribe = Observable.just("createLiveBroadcast")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map {
                    val youTube = YouTube.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            JacksonFactory.getDefaultInstance(),
                            mCredential
                    )
                            .setApplicationName(ContextUtils.getContext().getString(R.string.app_name))
                            .build()
                    YouTubeApi.createLiveEvent(youTube, description, title, startTime, endTime)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    AppLogger.w("返回的 频道 ID 为 ${it.id}")
                }, {

                })
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_DESTROY, subscribe)
    }
}