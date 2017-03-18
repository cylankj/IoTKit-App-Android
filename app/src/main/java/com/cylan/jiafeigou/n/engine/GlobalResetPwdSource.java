package com.cylan.jiafeigou.n.engine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.SmartcallActivity;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;

import rx.Observable;
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
    private Dialog dialog;
    private Subscription clearSub;

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
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }

        if (clearSub != null && !clearSub.isUnsubscribed()){
            clearSub.unsubscribe();
            clearSub = null;
        }

        if (dialog != null){
            dialog.dismiss();
            dialog = null;
        }
    }

    public void pwdResetedDialog(int code){
        if (code == 16008 || code == 1007){
            AppLogger.d("pwdResetedDialog:16008");
            AlertDialog.Builder builder = new AlertDialog.Builder(ContextUtils.getContext().getApplicationContext());
            LayoutInflater mLayoutInflater=LayoutInflater.from(ContextUtils.getContext().getApplicationContext());
            View view=mLayoutInflater.inflate(R.layout.dialog_reset_pwd_tab_view,null);
            builder.setView(view);
            dialog = builder.create();
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            dialog.show();
            view.findViewById(R.id.tv_dialog_btn_left).setOnClickListener(v->{
                //跳转到SmartcallActivity
                jump2LoginFragment();
                dialog.dismiss();
            });
        }
    }

    private void jump2LoginFragment() {
        clearPwd();
        RxBus.getCacheInstance().removeAllStickyEvents();
        RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(JError.StartLoginPage));
        RxBus.getCacheInstance().post(new RxEvent.LogOutByResetPwdTab(true));

        Intent intent = new Intent(ContextUtils.getContext(), SmartcallActivity.class);
        intent.putExtra("from_log_out", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ContextUtils.getContext().getApplicationContext().startActivity(intent);
    }

    public void clearPwd(){
        clearSub = Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(o->{
                    AutoSignIn.getInstance().autoSave(DataSourceManager.getInstance().getJFGAccount().getAccount(), 1, "")
                            .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                            .subscribe();
                });
    }
}
