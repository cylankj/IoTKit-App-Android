package com.cylan.jiafeigou.n.mvp.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.CallSuper;
import android.support.v4.content.LocalBroadcastManager;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.view.misc.MapSubscription;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.NetMonitor;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.lang.ref.WeakReference;
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
    private MapSubscription refCacheMap = new MapSubscription();
    private TimeTick timeTick;

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

    protected boolean registerTimeTick() {
        return false;
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
        AppLogger.d(TAG + ": register: " + compositeSubscription.isUnsubscribed());
        String[] action = registerNetworkAction();
        if (action != null && action.length > 0) {
            NetMonitor.getNetMonitor().registerNet(this, action);
        }
        if (registerTimeTick()) {
            if (timeTick == null) timeTick = new TimeTick(this);
            LocalBroadcastManager.getInstance(ContextUtils.getContext())
                    .registerReceiver(timeTick, new IntentFilter(JConstant.KEY_TIME_TICK_));
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
            refCacheMap.add(subscription, tag);
    }

    protected boolean unSubscribe(String tag) {
        refCacheMap.unsubscribe();
        return true;
    }

    @CallSuper
    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
        NetMonitor.getNetMonitor().unregister();
        if (registerTimeTick()) {
            if (timeTick != null)
                LocalBroadcastManager.getInstance(ContextUtils.getContext()).unregisterReceiver(timeTick);
        }
    }

    protected Subscription[] register() {
        return null;
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {

    }

    protected void onTimeTick() {
    }

    private static class TimeTick extends BroadcastReceiver {
        private WeakReference<AbstractPresenter> abstractPresenter;

        public TimeTick(AbstractPresenter abstractPresenter) {
            this.abstractPresenter = new WeakReference<>(abstractPresenter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (abstractPresenter != null && abstractPresenter.get() != null)
                abstractPresenter.get().onTimeTick();
        }
    }
}
