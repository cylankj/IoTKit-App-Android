package com.cylan.jiafeigou.module

import com.cylan.entity.jniCall.JFGDPMsg
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.rx.RxBus
import com.cylan.jiafeigou.rx.RxEvent
import rx.Observable
import rx.schedulers.Schedulers

/**
 * Created by yanzhendong on 2017/11/16.
 */
object DoorLockHelper {

    fun changePassword(uuid: String, oldPassword: String, newPassword: String): Observable<Boolean> {
        return Observable.just("changePassword")
                .observeOn(Schedulers.io())
                .map {
                    var params = arrayListOf<JFGDPMsg>()
                    return@map BaseApplication.getAppComponent().getCmd().robotSetData(uuid, params)
                }
                .flatMap { seq -> RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp::class.java).first { it.seq == seq } }
                .first()
                .map {
                    it.rets.getOrNull(0)?.ret == 0
                }
    }

    fun openDoor(uuid: String, password: String) {

    }
}