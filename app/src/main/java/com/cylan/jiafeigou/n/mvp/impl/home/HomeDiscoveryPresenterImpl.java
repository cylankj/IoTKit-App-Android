package com.cylan.jiafeigou.n.mvp.impl.home;

import android.support.annotation.Nullable;

import com.cylan.jiafeigou.n.mvp.contract.home.HomeDiscoveryContract;

import java.lang.ref.WeakReference;

import rx.Subscription;

/**
 * Created by hunt on 16-5-23.
 */
public class HomeDiscoveryPresenterImpl implements HomeDiscoveryContract.Presenter {

    private WeakReference<HomeDiscoveryContract.View> viewWeakReference;

    private Subscription onRefreshSubscription;

    public HomeDiscoveryPresenterImpl(HomeDiscoveryContract.View view) {
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
    private HomeDiscoveryContract.View getView() {
        return viewWeakReference != null ? viewWeakReference.get() : null;
    }

}
