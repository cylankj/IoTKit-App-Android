package com.cylan.jiafeigou.module

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.misc.JError
import com.cylan.jiafeigou.misc.JFGRules
import com.cylan.jiafeigou.push.PushPickerIntentService
import com.cylan.jiafeigou.rx.RxBus
import com.cylan.jiafeigou.rx.RxEvent
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ContextUtils
import com.cylan.jiafeigou.utils.MD5Util
import com.cylan.jiafeigou.utils.NetUtils
import com.cylan.jiafeigou.utils.PreferencesUtils
import rx.Observable
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by yanzhendong on 2017/12/1.
 */

data class User(var username: String, var password: String, var signType: Int, var display: String? = if (signType == 1) username else "")

object LoginHelper {
    private val TAG = LoginHelper::class.java.simpleName
    @JvmStatic
    var loginType: Int = -1
        private set

    @JvmStatic
    fun getUser(): User? {
        var signType = PreferencesUtils.getInt(JConstant.KEY_SIGN_TYPE, 1)
        var account = PreferencesUtils.getString(JConstant.KEY_PHONE)
        if (TextUtils.isEmpty(account)) {
            account = ContextUtils.getContext().getSharedPreferences("config_pref", Context.MODE_PRIVATE).getString(JConstant.KEY_PHONE, "")
            if (!TextUtils.isEmpty(account)) {
                PreferencesUtils.putString(JConstant.KEY_PHONE, account)
            }
        }
        if (TextUtils.isEmpty(account)) {
            return null
        }
        var password = PreferencesUtils.getString(JConstant.KEY_PSW)
        if (TextUtils.isEmpty(password)) {
            password = ContextUtils.getContext().getSharedPreferences("config_pref", Context.MODE_PRIVATE).getString(JConstant.KEY_PSW, "")
            if (!TextUtils.isEmpty(password)) {
                PreferencesUtils.putString(JConstant.KEY_PSW, password)
            }
        }
        return User(account, password, signType)
    }

    @JvmStatic
    fun saveUser(username: String?, password: String?, signType: Int = 0) {
        if (TextUtils.isEmpty(username)) {
            PreferencesUtils.remove(JConstant.KEY_PHONE)
            PreferencesUtils.remove(JConstant.KEY_PSW)
            PreferencesUtils.remove(JConstant.KEY_SIGN_TYPE)
            return
        }

        PreferencesUtils.putString(JConstant.KEY_PHONE, username)
        PreferencesUtils.putString(JConstant.KEY_PSW, MD5Util.lowerCaseMD5(password))
        PreferencesUtils.putInt(JConstant.KEY_SIGN_TYPE, signType)
    }

    @JvmStatic
    fun clearPassword() {
        PreferencesUtils.remove(JConstant.KEY_PSW)
    }

    @JvmStatic
    fun performLogin(username: String?, password: String?, signType: Int = 0): Observable<RxEvent.AccountArrived> {
        return Observable.create<RxEvent.AccountArrived> { subscriber ->
            val languageType = JFGRules.getLanguageType(ContextUtils.getContext())
            var subscribe = RxBus.getCacheInstance().toObservable(RxEvent.ResultLogin::class.java)
                    .subscribe({
                        if (it.code == 0) {
                            val subscription = Schedulers.io().createWorker().schedulePeriodically({
                                val account = Command.getInstance().account
                                Log.d(JConstant.CYLAN_TAG, "performLogin:login successful,starting get account with username:$username,ret:$account")
                            }, 0, 2, TimeUnit.SECONDS)
                            subscriber.add(subscription)
                            PushPickerIntentService.start()
                        } else {
                            if (it.code == JError.ErrorLoginInvalidPass) {
                                Log.d(JConstant.CYLAN_TAG, "performLogin: login failed,the username or password is invalid,username is:$username")
                                clearPassword()
                            }
                            subscriber.onError(RxEvent.HelperBreaker(it.code, it))
                        }
                    }) {
                        it.printStackTrace()
                        AppLogger.e(it)
                        if (!subscriber.isUnsubscribed) {
                            subscriber.onError(it)
                        }
                    }
            subscriber.add(subscribe)

            subscribe = RxBus.getCacheInstance().toObservable(RxEvent.AccountArrived::class.java)
//                    .first { TextUtils.equals(it.account.account, username) }
                    .first()
                    .subscribe({
                        Log.d(JConstant.CYLAN_TAG, "performLogin:account is arrived,login process is completed.the account is:$it")
                        loginType = signType
                        subscriber.onNext(it)
                        subscriber.onCompleted()
                    }) {
                        it.printStackTrace()
                        AppLogger.e(it)
                        if (!subscriber.isUnsubscribed) {
                            subscriber.onError(it)
                        }
                    }
            subscriber.add(subscribe)

            when {
                TextUtils.isEmpty(username) || TextUtils.isEmpty(password) -> {
                    Log.d(JConstant.CYLAN_TAG, "performLogin error:username or password is empty," +
                            "username is:$username,password is:$password,signType is:$signType")

                    subscriber.onError(IllegalArgumentException("performLogin error:username or password is empty," +
                            "username is:$username,password is:$password,signType is:$signType"))
                }
                NetUtils.getNetType(ContextUtils.getContext()) == -1 -> {
                    Log.d(JConstant.CYLAN_TAG, "performLogin network offline,starting offline login")
                    DataSourceManager.getInstance().initFromDB()
                }
                signType >= 3 -> {
                    Log.d(JConstant.CYLAN_TAG, "performLogin with open loginType:$signType,with username:$username,with password:$password")
                    Command.getInstance().openLogin(languageType, username!!, password!!, signType)
                    subscribe = Schedulers.io().createWorker().schedule({
                        if (!isLoginSuccessful()) {
                            val user = getUser()
                            if (user != null && !TextUtils.isEmpty(user.username) && !TextUtils.isEmpty(user.password)) {
                                Log.d(JConstant.CYLAN_TAG, "performLogin with open loginType:$signType,with username:$username,with password:$password,timeout,starting offline login")
                                DataSourceManager.getInstance().initFromDB()
                            }
                        }
                    }, 5, TimeUnit.SECONDS)
                    subscriber.add(subscribe)
                }
                else -> {
                    Log.d(JConstant.CYLAN_TAG, "performLogin with normal loginType:$signType,with username:$username,with password:$password")
                    Command.getInstance().login(languageType, username!!, password!!, true)
                    subscribe = Schedulers.io().createWorker().schedule({
                        if (!isLoginSuccessful()) {
                            val user = getUser()
                            if (user != null && !TextUtils.isEmpty(user.username) && !TextUtils.isEmpty(user.password)) {
                                Log.d(JConstant.CYLAN_TAG, "performLogin with normal loginType:$signType,with username:$username,with password:$password,timeout,starting offline login")
                                DataSourceManager.getInstance().initFromDB()
                            }
                        }
                    }, 7, TimeUnit.SECONDS)
                    subscriber.add(subscribe)
                }
            }

        }
                .subscribeOn(Schedulers.io())
    }

    @JvmStatic
    fun performAutoLogin(): Observable<RxEvent.AccountArrived> {
        val user = getUser()
        return performLogin(user?.username, user?.password, user?.signType ?: 0)
    }

    @JvmStatic
    fun isLoginSuccessful(): Boolean {
        return loginType > 0
    }

    @JvmStatic
    fun isFirstUseApp(): Boolean {
        return ContextUtils.getContext().resources.getBoolean(R.bool.show_guide) && PreferencesUtils.getBoolean(JConstant.KEY_FRESH, true)
    }


    @JvmStatic
    fun performLogout() {
        loginType = -1
        clearPassword()
        Command.getInstance().logout()
        DataSourceManager.getInstance().logout()
    }

}