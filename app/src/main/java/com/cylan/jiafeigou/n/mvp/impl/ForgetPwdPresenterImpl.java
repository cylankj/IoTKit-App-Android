package com.cylan.jiafeigou.n.mvp.impl;

import android.util.Log;

import com.cylan.jiafeigou.n.mvp.contract.login.ForgetPwdContract;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;
import com.cylan.utils.RandomUtils;

import org.msgpack.annotation.NotNullable;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public class ForgetPwdPresenterImpl implements ForgetPwdContract.ForgetPwdPresenter {

    WeakReference<ForgetPwdContract.ForgetPwdView> forgetPwdViewWeakReference;
    Subscription subscription;

    public ForgetPwdPresenterImpl(ForgetPwdContract.ForgetPwdView view) {
        this.forgetPwdViewWeakReference = new WeakReference<>(view);
        view.setPresenter(this);
    }

    ForgetPwdContract.ForgetPwdView getView() {
        if (forgetPwdViewWeakReference != null)
            return forgetPwdViewWeakReference.get();
        return null;
    }

    @Override
    public void executeSubmitAccount(@NotNullable String account) {
        subscription = Observable.just(account)
                .delay(3000, TimeUnit.MILLISECONDS)
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        doStuff();
                    }
                });
    }

    @Override
    public void submitPhoneNumAndCode(String account, String code) {
        subscription = Observable.just(account)
                .delay(3000, TimeUnit.MILLISECONDS)
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        doStuff();
                    }
                });
    }

    private void doStuff() {
        RequestResetPwdBean bean = testResult();
        if (getView() != null) {
            getView().submitResult(bean);
        } else {
            Log.e("", "");
        }
    }

    private RequestResetPwdBean testResult() {
        RequestResetPwdBean bean = new RequestResetPwdBean();
        bean.ret = RandomUtils.getRandom(4);
            bean.ret = 2;
        if (bean.ret == 1) {

        } else if (bean.ret == 2) {

        }
        return bean;
    }

    private void unSubscribe(Subscription... subscriptions) {
        if (subscriptions != null) {
            for (Subscription subscription : subscriptions) {
                if (subscription != null && subscription.isUnsubscribed())
                    subscription.unsubscribe();
            }
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        unSubscribe(subscription);
    }
}
