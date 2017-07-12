package com.cylan.jiafeigou.n.mvp.impl.bind;

import com.cylan.jiafeigou.n.mvp.contract.bind.SnContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractFragmentPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 17-7-12.
 */

public class SnPresenter extends AbstractFragmentPresenter<SnContract.View> implements SnContract.Presenter {

    public SnPresenter(SnContract.View view) {
        super(view);
    }

    @Override
    public void getPid(String sn) {
        long seq = 0;
        Subscription subscription = RxBus.getCacheInstance().toObservable(RxEvent.UniversalDataRsp.class)
                .filter(ret -> ret.seq == seq)
                .subscribeOn(Schedulers.io())
                .subscribe(ret -> {
                    unSubscribe("getPid");
                }, AppLogger::e);
        addSubscription(subscription, "getPid");
    }
}
