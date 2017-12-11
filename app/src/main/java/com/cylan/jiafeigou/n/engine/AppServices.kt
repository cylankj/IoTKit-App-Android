package com.cylan.jiafeigou.n.engine

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ContextUtils
import rx.Observable

/**
 * Created by yanzhendong on 2017/12/1.
 */
object AppServices : IntentService("AppServices") {
    private val TAG = "AppServices"
    private val APP_ACTION = "APP_ACTION"
    private val APP_ACTION_GET_ADS = "APP_ACTION_GET_ADS"

    override fun onHandleIntent(intent: Intent) {
        Log.d(TAG, intent.toString())
        when (intent.getStringExtra(APP_ACTION)) {
            APP_ACTION_GET_ADS -> {
                fetchAds()
            }
        }
    }

    @JvmStatic
    fun startFetchAds() {
        val intent = Intent(ContextUtils.getContext(), AppServices::class.java)
        intent.putExtra(APP_ACTION, APP_ACTION_GET_ADS)
        ContextUtils.getContext().startService(intent)
    }

    private fun fetchAds() {
        AppLogger.d("fetch ads")
        Observable.create<Any> {

        }
    }


}