package com.cylan.jiafeigou.n.engine;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2017/3/14
 * 描述：
 */
public class GlobalResetPwdSource {
    private static GlobalResetPwdSource instance;
    private CompositeSubscription mSubscription;

    public static GlobalResetPwdSource getInstance() {
        if (instance == null) {
            instance = new GlobalResetPwdSource();
        }
        return instance;
    }

    public void register() {
        if (mSubscription == null) {
            mSubscription = new CompositeSubscription();
        }
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.PwdHasResetEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pwdHasResetEvent -> {
                    pwdResetedDialog(pwdHasResetEvent.code);
                });
        mSubscription.add(subscribe);
    }

    public void unRegister() {
        if (mSubscription != null && mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }

    public void pwdResetedDialog(int code){
        if (code == 16008){
          //TODO
        }
    }

}
