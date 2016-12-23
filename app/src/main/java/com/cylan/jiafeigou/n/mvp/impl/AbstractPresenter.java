package com.cylan.jiafeigou.n.mvp.impl;

import com.cylan.jiafeigou.BuildConfig;

import rx.Subscription;

/**
 * 一个基本模型的Presenter
 * Created by cylan-hunt on 16-6-30.
 */
public abstract class AbstractPresenter<T> {

    protected final String TAG = this.getClass().getSimpleName();
    protected T mView;//弱引用会被强制释放,我们的view需要我们手动释放,不适合弱引用

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

    protected void checkNull() {
        if (getView() == null && BuildConfig.DEBUG)
            throw new NullPointerException("view is null");
    }


}
