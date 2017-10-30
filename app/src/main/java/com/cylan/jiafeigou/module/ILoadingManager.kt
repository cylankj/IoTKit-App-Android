package com.cylan.jiafeigou.module

import android.app.Dialog
import android.content.Context
import android.support.annotation.StringRes
import rx.Observable

/**
 * Created by yanzhendong on 2017/10/27.
 */
interface ILoadingManager {


    fun hideLoading()

    fun showLoading(context: Context, @StringRes resId: Int, cancelable: Boolean, vararg args: Any)

    fun showLoadingRx(context: Context, @StringRes resId: Int, cancelable: Boolean, vararg args: Any): Observable<Void>

    fun showAlert(dialog: Dialog)

    fun showAlertRx(dialog: Dialog): Observable<Void>

}