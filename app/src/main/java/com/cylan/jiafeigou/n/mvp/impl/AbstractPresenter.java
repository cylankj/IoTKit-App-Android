package com.cylan.jiafeigou.n.mvp.impl;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.CallSuper;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.support.network.NetMonitor;

import java.util.HashMap;
import java.util.Map;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * 一个基本模型的Presenter
 * Created by cylan-hunt on 16-6-30.
 */
public abstract class AbstractPresenter<T extends BaseView> implements BasePresenter,
        NetMonitor.NetworkCallback {

    protected final String TAG = this.getClass().getSimpleName();
    protected T mView;//弱引用会被强制释放,我们的view需要我们手动释放,不适合弱引用
    protected String uuid;
    private CompositeSubscription compositeSubscription;
    private Map<String, Subscription> refCacheMap = new HashMap<>();

    public AbstractPresenter(T view) {
        mView = view;
    }

    public AbstractPresenter(T view, String uuid) {
        mView = view;
        this.uuid = uuid;
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
                if (s != null)
                    compositeSubscription.add(s);
        }
        String[] action = registerNetworkAction();
        if (action != null && action.length > 0) {
            NetMonitor.getNetMonitor().registerNet(this, action);
        }
    }

    protected String[] registerNetworkAction() {
        return null;
    }

    protected void addSubscription(Subscription subscription) {
        if (subscription != null)
            compositeSubscription.add(subscription);
    }

    protected void addSubscription(Subscription subscription, String tag) {
        if (subscription != null)
            refCacheMap.put(tag, subscription);
    }

    protected boolean unSubscribe(String tag) {
        Subscription subscription = refCacheMap.get(tag);
        if (subscription != null && subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            return true;
        }
        return false;
    }

    @CallSuper
    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
        NetMonitor.getNetMonitor().unregister();
    }

    protected Subscription[] register() {
        return null;
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {

    }
}
