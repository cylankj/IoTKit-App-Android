package com.cylan.jiafeigou.n.mvp.impl;

import java.lang.ref.WeakReference;

import rx.Subscription;

/**
 * 一个基本模型的Presenter
 * Created by cylan-hunt on 16-6-30.
 */
public abstract class AbstractPresenter<T> {
    WeakReference<T> weakReference;

    public AbstractPresenter(T t) {
        weakReference = new WeakReference<>(t);
    }

    public T getView() {
        return weakReference == null ? null : weakReference.get();
    }

    protected void unSubscribe(Subscription... subscriptions) {
        if (subscriptions != null) {
            for (Subscription subscription : subscriptions) {
                if (subscription != null && subscription.isUnsubscribed())
                    subscription.unsubscribe();
            }
        }
    }

}
