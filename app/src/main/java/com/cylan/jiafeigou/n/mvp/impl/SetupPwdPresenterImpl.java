package com.cylan.jiafeigou.n.mvp.impl;

import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.login.SetupPwdContract;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.rx.RxBus;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

;

/**
 * Created by cylan-hunt on 16-6-30.
 */
public class SetupPwdPresenterImpl extends AbstractPresenter<SetupPwdContract.View>
        implements SetupPwdContract.Presenter {

    private Subscription subscription;

    private CompositeSubscription compositeSubscription;

    public SetupPwdPresenterImpl(SetupPwdContract.View view) {
        super(view);
        view.setPresenter(this);
        initComposeSubscription();
    }

    private void initComposeSubscription() {
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(RxBus.getCacheInstance().toObservable(RxEvent.ResultRegister.class)
                .observeOn(AndroidSchedulers.mainThread())
                .throttleFirst(1000L, TimeUnit.MICROSECONDS)
                .subscribe(new Action1<RxEvent.ResultRegister>() {
                    @Override
                    public void call(RxEvent.ResultRegister register) {
                        getView().submitResult(register);
                    }
                }));
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        unSubscribe(subscription, compositeSubscription);
    }

    @Override
    public void register(final String account, final String pwd, final int type, final String token) {
        subscription = Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object s) {
                        JfgCmdInsurance.getCmd().register(account, pwd, type, token);
                        LoginAccountBean bean = new LoginAccountBean();
                        bean.userName = account;
                        bean.pwd = pwd;
                        JCache.tmpAccount = bean;
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        JCache.tmpAccount = null;
                        AppLogger.e("god..." + throwable.getLocalizedMessage());
                    }
                });
    }
}
