package com.cylan.jiafeigou.n.mvp.impl.home;

import android.support.annotation.Nullable;

import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineContract;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by hunt on 16-5-23.
 */
public class HomeMinePresenterImpl implements HomeMineContract.Presenter {

    private WeakReference<HomeMineContract.View> viewWeakReference;

    private Subscription onRefreshSubscription;

    public HomeMinePresenterImpl(HomeMineContract.View view) {
        viewWeakReference = new WeakReference<>(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        Observable.just(null)
                .subscribeOn(Schedulers.io())
                .delay(3000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (getView() != null)
                            getView().onPortraitUpdate("zhe ye keyi");
                    }
                });
    }

    @Override
    public void stop() {
        unSubscribe();
    }

    private void unSubscribe(Subscription... subscriptions) {
        if (subscriptions != null)
            for (Subscription subscription : subscriptions) {
                if (subscription != null)
                    subscription.unsubscribe();
            }
    }

    @Nullable
    private HomeMineContract.View getView() {
        return viewWeakReference != null ? viewWeakReference.get() : null;
    }

    @Override
    public void requestLatestPortrait() {

    }
}
