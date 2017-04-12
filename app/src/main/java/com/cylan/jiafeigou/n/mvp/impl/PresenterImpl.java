package com.cylan.jiafeigou.n.mvp.impl;

import com.cylan.jiafeigou.n.mvp.contract.login.RstPwdContract;
import com.cylan.jiafeigou.support.log.AppLogger;

import org.msgpack.annotation.NotNullable;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public class PresenterImpl extends AbstractPresenter<RstPwdContract.View> implements RstPwdContract.Presenter {

    Subscription subscription;

    public PresenterImpl(RstPwdContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void executeSubmitNewPwd(String account, @NotNullable String pwdInMd5) {
        subscription = Observable.just(account, pwdInMd5)
                .subscribeOn(Schedulers.io())
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
                },e-> AppLogger.d(e.getMessage()));
    }

    private void doStuff() {
        getView().submitResult(1);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if (subscription != null)
            subscription.unsubscribe();
    }
}
