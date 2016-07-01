package com.cylan.jiafeigou.n.mvp.impl;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.login.ForgetPwdContract;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;
import com.cylan.utils.RandomUtils;
import com.superlog.SLog;

import org.msgpack.annotation.NotNullable;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public class ForgetPwdPresenterImpl extends AbstractPresenter<ForgetPwdContract.View> implements ForgetPwdContract.Presenter {

    Subscription subscription;

    public ForgetPwdPresenterImpl(ForgetPwdContract.View view) {
        super(view);
        view.setPresenter(this);
    }


    @Override
    public void executeSubmitAccount(@NotNullable String account) {
        subscription = Observable.just(account)
                .delay(3000, TimeUnit.MILLISECONDS)
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        return s;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if (getView().getContext() != null)
                            doStuff(TextUtils.isDigitsOnly(s) ? JConstant.TYPE_PHONE : JConstant.TYPE_EMAIL);
                    }
                });
    }

    @Override
    public void submitForVerificationCode(String account) {
        SLog.d("no thing happened");
    }

    @Override
    public void submitPhoneNumAndCode(String account, String code) {
        subscription = Observable.just(account)
                .delay(3000, TimeUnit.MILLISECONDS)
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        return s;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if (getView().getContext() != null)
                            doStuff(JConstant.TYPE_PHONE);
                    }
                });
    }

    private void doStuff(final int type) {
        RequestResetPwdBean bean = testResult();
        bean.ret = type;
        if (getView() != null) {
            getView().submitResult(bean);
        } else {
            Log.e("", "");
        }
    }

    private RequestResetPwdBean testResult() {
        RequestResetPwdBean bean = new RequestResetPwdBean();
        bean.ret = RandomUtils.getRandom(4);
        bean.ret = 1;
        if (bean.ret == 1) {

        } else if (bean.ret == 2) {

        }
        return bean;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        unSubscribe(subscription);
    }
}
