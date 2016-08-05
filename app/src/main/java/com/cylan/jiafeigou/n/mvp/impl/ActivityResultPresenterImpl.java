package com.cylan.jiafeigou.n.mvp.impl;

import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.ActivityResultContract;
import com.cylan.jiafeigou.support.rxbus.RxBus;

import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by cylan-hunt on 16-8-4.
 */

public class ActivityResultPresenterImpl extends
        AbstractPresenter<ActivityResultContract.View>
        implements ActivityResultContract.Presenter {
    public ActivityResultPresenterImpl(ActivityResultContract.View view) {
        super(view);
    }

    private Subscription subscription;

    @Override
    public void setActivityResult(RxEvent.ActivityResult result) {
        if (RxBus.getInstance().hasObservers()) {
            RxBus.getInstance().send(result);
        }
    }

    @Override
    public void start() {
        subscription = RxBus.getInstance().toObservable()
                .throttleFirst(3000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (getView() == null)
                            return;
                        if (o != null && o instanceof RxEvent.ActivityResult) {
                            getView().onActivityResult((RxEvent.ActivityResult) o);
                        }
                    }
                });
    }

    @Override
    public void stop() {
        unSubscribe(subscription);
    }

}
