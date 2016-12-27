package com.cylan.jiafeigou.n.mvp.impl;

import android.support.annotation.CallSuper;

import com.cylan.jiafeigou.n.mvp.BasePresenter;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * 一个基本模型的Presenter
 * Created by cylan-hunt on 16-6-30.
 */
public abstract class AbstractPresenter<T> implements BasePresenter {

    protected final String TAG = this.getClass().getSimpleName();
    protected T mView;//弱引用会被强制释放,我们的view需要我们手动释放,不适合弱引用
    private CompositeSubscription compositeSubscription;

    public AbstractPresenter(T view) {
        mView = view;
    }

    public T getView() {
        return mView;
    }

    protected void unSubscribe(Subscription... subscriptions) {
        if (subscriptions != null) {
            for (Subscription subscription : subscriptions) {
                if (subscription != null && !subscription.isUnsubscribed())
                    subscription.unsubscribe();
            }
        }
    }

    @CallSuper
    @Override
    public void start() {
        if (compositeSubscription == null)
            compositeSubscription = new CompositeSubscription();
        if (compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
            compositeSubscription = new CompositeSubscription();
        }
        Subscription[] subs = register();
        if (subs != null) {
            for (Subscription s : subs)
                compositeSubscription.add(s);
        }
    }

    @CallSuper
    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
    }

    protected Subscription[] register() {
        return null;
    }
}
