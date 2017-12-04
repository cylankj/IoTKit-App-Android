package com.cylan.jiafeigou.module

import android.util.Log
import com.cylan.jiafeigou.support.log.AppLogger
import rx.Observable
import rx.subjects.PublishSubject

/**
 * Created by yzd on 17-12-3.
 */
object AccountSupervisor {
    private val TAG = AccountSupervisor::class.java.simpleName
    private val publisher = PublishSubject.create<Account>().toSerialized()
    private var account: Account? = null

    init {
        monitorAccount()
    }

    private fun monitorAccount() {
        AppCallbackSupervisor.observeUpdateAccount().subscribe(AccountSupervisor::updateAccount) {
            it.printStackTrace()
            AppLogger.e(it)
            monitorAccount()
        }
    }

    private fun updateAccount(accountEvent: AppCallbackSupervisor.UpdateAccountEvent) {
        Log.d(TAG, "account update:${accountEvent.jfgAccount}")
        val jfgAccount = accountEvent.jfgAccount
        val token = jfgAccount.token
        val alias = jfgAccount.alias
        val enablePush = if (jfgAccount.isEnablePush) 1 else 0
        val enableSound = if (jfgAccount.isEnableSound) 1 else 0
        val email = jfgAccount.email
        val enableVibrate = if (jfgAccount.isEnableVibrate) 1 else 0
        val phone = jfgAccount.phone
        val photoUrl = jfgAccount.photoUrl
        val account = jfgAccount.account
        val wxPush = jfgAccount.wxPush
        val wxOpenID = jfgAccount.wxOpenID
        val accountBox = AccountBox(0, token, alias, enablePush, enableSound, email, enableVibrate, phone, photoUrl, account, wxPush, wxOpenID)
        DBSupervisor.saveAccount(accountBox)
        AccountSupervisor.account = Account(accountBox)
        publisher.onNext(AccountSupervisor.account)
    }

    @JvmStatic
    fun getAccount(): Account? {
        return account
    }

    @JvmStatic
    fun observeAccount(): Observable<Account> {
        return publisher.asObservable()
    }
}