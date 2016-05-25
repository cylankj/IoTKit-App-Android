package com.cylan.jiafeigou.n.mvp.impl.home;

import android.support.annotation.Nullable;

import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineContract;

import java.lang.ref.WeakReference;

import rx.Subscription;

/**
 * Created by hunt on 16-5-23.
 */
public class HomeDiscoveryPresenterImpl implements HomeMineContract.Presenter {

    private WeakReference<HomeMineContract.View> viewWeakReference;

    private Subscription onRefreshSubscription;

    public HomeDiscoveryPresenterImpl(HomeMineContract.View view) {
        viewWeakReference = new WeakReference<>(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
    }

    @Nullable
    private HomeMineContract.View getView() {
        return viewWeakReference != null ? viewWeakReference.get() : null;
    }

}
