package com.cylan.jiafeigou.n.engine;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;


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
            AppLogger.d("pwdResetedDialog:16008");
            AlertDialog.Builder builder = new AlertDialog.Builder(ContextUtils.getContext().getApplicationContext());
            builder.setTitle("密码已修改，重新登录");
            builder.setMessage("This is message");
            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            Dialog dialog=builder.create();
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            dialog.show();
        }
    }
}
