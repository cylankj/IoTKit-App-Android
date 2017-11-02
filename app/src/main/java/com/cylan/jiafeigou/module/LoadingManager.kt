package com.cylan.jiafeigou.module

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import com.cylan.jiafeigou.n.view.misc.MapSubscription
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.MiscUtils
import com.cylan.jiafeigou.widget.LoadingDialog
import rx.Observable
import rx.android.MainThreadSubscription
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.SerialSubscription
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by yanzhendong on 2017/10/27.
 */
@Singleton
class LoadingManager @Inject constructor() : ILoadingManager {

    private var subscriptions: MapSubscription = MapSubscription()

    override fun showAlert(dialog: Dialog) {
        Observable.create<Void> { subscriber ->
            dialog.setOnDismissListener {
                dialog.setOnDismissListener(null)
                if (!subscriber.isUnsubscribed) {
                    subscriber.unsubscribe()
                }
            }
            if (!subscriber.isUnsubscribed) {
                dialog.show()
                subscriber.onNext(null)
                subscriber.onCompleted()
            }
            subscriber.add(object : MainThreadSubscription() {
                override fun onUnsubscribe() {
                    dialog.dismiss()
                }
            })
            serialSubscription.set(subscriber)
        }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({}) {
                    AppLogger.e(MiscUtils.getErr(it))
                }
    }

    override fun showAlertRx(dialog: Dialog): Observable<Void> {
        return Observable.create<Void> { subscriber ->
            dialog.setOnDismissListener {
                dialog.setOnDismissListener(null)
                if (!subscriber.isUnsubscribed) {
                    subscriber.unsubscribe()
                }
            }
            if (!subscriber.isUnsubscribed) {
                dialog.show()
                subscriber.onNext(null)
            }
            subscriber.add(object : MainThreadSubscription() {
                override fun onUnsubscribe() {
                    dialog.dismiss()
                }
            })
            serialSubscription.set(subscriber)
        }.subscribeOn(AndroidSchedulers.mainThread())
    }

    //保证同一时间只会有一个 alert 或者 loading
    private val serialSubscription: SerialSubscription = SerialSubscription()

    override fun hideLoading() {
        LoadingDialog.dismissLoading()
    }

    override fun showLoading(context: Context, resId: Int, cancelable: Boolean, vararg args: Any) {
        val subscribe = Observable.OnSubscribe<Void> { subscriber ->
            val listener = DialogInterface.OnCancelListener {
                if (!subscriber.isUnsubscribed) {
                    subscriber.unsubscribe()
                }
            }
            val dialog = LoadingDialog(context)
            if (!subscriber.isUnsubscribed) {
                dialog.setMessageText(context.getString(resId, *args))
                dialog.setCancelable(cancelable)
                dialog.setOnCancelListener(listener)
                dialog.show()
                subscriber.onNext(null)
            }
            subscriber.add(object : MainThreadSubscription() {
                override fun onUnsubscribe() {
                    dialog.setOnCancelListener(null)
                    dialog.dismiss()
                }
            })
            serialSubscription.set(subscriber)
        }
        Observable.just("").doOnUnsubscribe {  }
        Observable.create(subscribe).subscribe({}) {
            AppLogger.e(MiscUtils.getErr(it))
        }
    }

    override fun showLoadingRx(context: Context, resId: Int, cancelable: Boolean, vararg args: Any): Observable<Void> {
        return Observable.create { subscriber ->
            val dialog = LoadingDialog(context)
            subscriber.add(object : MainThreadSubscription() {
                override fun onUnsubscribe() {
                    AppLogger.w("will unsubscribed")
                    dialog.setOnCancelListener(null)
                    dialog.dismiss()
                }
            })
            dialog.setMessageText(context.getString(resId, *args))
            dialog.setCancelable(cancelable)
            dialog.setOnCancelListener { subscriber.onCompleted() }
            dialog.show()
            subscriber.onNext(null)
        }
    }
}