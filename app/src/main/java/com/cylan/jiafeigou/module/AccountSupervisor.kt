package com.example.yzd.helloworld

import com.cylan.jiafeigou.module.Account
import rx.Observable
import rx.subjects.PublishSubject

/**
 * Created by yzd on 17-12-3.
 */
object AccountSupervisor {
    private val publisher = PublishSubject.create<Account>().toSerialized()
    @JvmStatic
    private var account: Account? = null

    init {
        AppCallbackSupervisor.observeOnAccountUpdate().subscribe()
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