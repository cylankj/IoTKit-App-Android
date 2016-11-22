package com.cylan.jiafeigou.n.mvp.impl;

import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.ActivityResultContract;
import com.cylan.jiafeigou.rx.RxBus;

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
        if (RxBus.getCacheInstance().hasObservers()) {
            RxBus.getCacheInstance().post(result);
        }
    }

    @Override
    public void start() {
        subscription = getActivityResultSub();
    }

    private Subscription getActivityResultSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ActivityResult.class)
                .throttleFirst(3000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<RxEvent.ActivityResult>() {
                    @Override
                    public void call(RxEvent.ActivityResult o) {
                        getView().onActivityResult(o);
                    }
                });
    }

    @Override
    public void stop() {
        unSubscribe(subscription);
    }

}
