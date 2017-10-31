package com.cylan.jiafeigou.n.view.panorama

import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.rtmp.youtube.util.EventData
import com.cylan.jiafeigou.rtmp.youtube.util.YouTubeApi
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.support.oauth2.AuthStateManager
import com.cylan.jiafeigou.utils.MiscUtils
import com.cylan.jiafeigou.utils.PreferencesUtils
import com.cylan.utils.ContextUtils
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationService
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/9/7.
 */
class YouTubeLiveCreatePresenter @Inject constructor(view: YouTubeLiveCreateContract.View?) : BasePresenter<YouTubeLiveCreateContract.View>(view), YouTubeLiveCreateContract.Presenter {
    override fun createLiveBroadcast(credential: GoogleAccountCredential, title: String?, description: String?, startTime: Long, endTime: Long) {
         val subscribe = mSubscriptionManager.destroy(this)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .flatMap {
                    Observable.create<EventData> { subscriber ->
                        val youTube = YouTube.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                JacksonFactory.getDefaultInstance(),
                                credential
                        )
                                .setApplicationName(ContextUtils.getContext().getString(R.string.app_name))
                                .build()
                        val liveEvent = YouTubeApi.createLiveEvent(youTube, description, title, startTime, endTime)
                        subscriber.onNext(liveEvent)
                        subscriber.onCompleted()
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { mLoadingManager.showLoading(mView.activity(), R.string.CREATING, false) }
                .doOnTerminate { mLoadingManager.hideLoading() }
                .timeout(120, TimeUnit.SECONDS, Observable.just(null))
                .subscribe({
                    if (it == null) {
                        //120 秒超时了啊
                        mView.onCreateLiveBroadcastTimeout()
                    } else {
                        val json = JacksonFactory.getDefaultInstance().toString(it)
                        PreferencesUtils.putString(JConstant.YOUTUBE_PREF_CONFIGURE + ":" + uuid, json)
                        mView.onCreateLiveBroadcastSuccess(it)
                        AppLogger.w("返回的结果为:$json")
                    }
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
        addSubscription(subscribe)
    }

    override fun createLiveBroadcast(title: String?, description: String?, startTime: Long, endTime: Long) {
         val subscribe = mSubscriptionManager.destroy(this)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .flatMap {
                    return@flatMap Observable.create<EventData> {
                        val authStateManager = AuthStateManager.getInstance(mContext)
                        val authState = authStateManager.current
                        if (authState.isAuthorized) {
                            authState.performActionWithFreshTokens(AuthorizationService(mContext), { accessToken, _, ex ->
                                if (ex == null) {
                                    val youTube = YouTube.Builder(
                                            AndroidHttp.newCompatibleTransport(),
                                            JacksonFactory.getDefaultInstance(),
                                            HttpRequestInitializer { it.headers.authorization = "Bearer $accessToken" }
                                    )
                                            .setApplicationName(ContextUtils.getContext().getString(R.string.app_name))
                                            .build()
                                    //内部使用了 asyncTask 所以这里实在主线程了
                                    Schedulers.io().createWorker().schedule {
                                        try {
                                            val eventData = YouTubeApi.createLiveEvent(youTube, description, title, startTime, endTime)
                                            it.onNext(eventData)
                                            it.onCompleted()
                                        } catch (e: Exception) {
                                            it.onError(e)
                                        }
                                    }

                                } else {
                                    it.onError(ex)
                                }
                            })
                        } else {
                            it.onError(IllegalStateException("没有有效的验证信息,请确保已经登录授权过"))
                        }
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { mLoadingManager.showLoading(mView.activity(), R.string.CREATING, false) }
                .doOnTerminate { mLoadingManager.hideLoading() }
                .timeout(120, TimeUnit.SECONDS, Observable.just(null))
                .subscribe({
                    if (it == null) {
                        //120 秒超时了啊
                        mView.onCreateLiveBroadcastTimeout()
                    } else {
                        val json = JacksonFactory.getDefaultInstance().toString(it)
                        PreferencesUtils.putString(JConstant.YOUTUBE_PREF_CONFIGURE + ":" + uuid, json)
                        mView.onCreateLiveBroadcastSuccess(it)
                        AppLogger.w("返回的结果为:$json")
                    }
                }, {
                    when (it) {
                        is GooglePlayServicesAvailabilityIOException -> {
                            mView.showGooglePlayServicesAvailabilityErrorDialog(it.connectionStatusCode)
                        }
                        is UserRecoverableAuthIOException -> {
                            mView.onUserRecoverableAuthIOException(it)
                        }
                        is AuthorizationException -> {
                            mView.onAuthorizationException()
                        }
                        is IllegalStateException -> {
                            mView.onAuthorizationException()
                        }
                        else -> {
                            AppLogger.w(MiscUtils.getErr(it))
                        }
                    }
                })
        addSubscription(subscribe)
    }
}