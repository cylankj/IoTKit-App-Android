package com.cylan.jiafeigou.n.mvp.impl.bind;

import com.cylan.jiafeigou.n.mvp.contract.bind.ScanContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-7.
 */
public class ScanContractImpl extends AbstractPresenter<ScanContract.View> implements ScanContract.Presenter {

    Subscription subscription;

    public ScanContractImpl(ScanContract.View v) {
        super(v);
        v.setPresenter(this);
    }

    @Override
    public void start() {
        subscription = Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        getView().onStartScan();
                    }
                });
    }

    @Override
    public void stop() {
        unSubscribe(subscription);
    }

}
