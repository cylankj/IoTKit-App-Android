package com.cylan.jiafeigou.module

/**
 * Created by yzd on 17-12-3.
 */
object AccountSupervisor {
//    private val TAG = AccountSupervisor::class.java.simpleName
//    private val publisher = PublishSubject.create<Account>().toSerialized()
//    private var account: Account? = null
//
//    init {
//        monitorAccount()
//    }
//
//    private fun monitorAccount() {
//        AppCallbackSupervisor.observe(JFGAccount::class.java).subscribe(AccountSupervisor::updateAccount) {
//            it.printStackTrace()
//            AppLogger.e(it)
//            monitorAccount()
//        }
//    }
//
//    private fun updateAccount(accountEvent: JFGAccount) {
//        Log.d(TAG, "account update:${accountEvent}")
//        val token = accountEvent.token
//        val alias = accountEvent.alias
//        val enablePush = if (accountEvent.isEnablePush) 1 else 0
//        val enableSound = if (accountEvent.isEnableSound) 1 else 0
//        val email = accountEvent.email
//        val enableVibrate = if (accountEvent.isEnableVibrate) 1 else 0
//        val phone = accountEvent.phone
//        val photoUrl = accountEvent.photoUrl
//        val account = accountEvent.account
//        val wxPush = accountEvent.wxPush
//        val wxOpenID = accountEvent.wxOpenID
//        val accountBox = AccountBox(0, token, alias, enablePush, enableSound, email, enableVibrate, phone, photoUrl, account, wxPush, wxOpenID)
//        DBSupervisor.saveAccount(accountBox)
//        AccountSupervisor.account = Account(accountBox)
//        publisher.onNext(AccountSupervisor.account)
//    }
//
//    @JvmStatic
//    fun getAccount(): Account? {
//        return account
//    }
//
//    @JvmStatic
//    fun observeAccount(): Observable<Account> {
//        return publisher.asObservable()
//    }
}