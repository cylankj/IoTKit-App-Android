package com.cylan.jiafeigou.n.mvp.impl.home;

import android.support.annotation.Nullable;

import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;

import java.lang.ref.WeakReference;

import rx.Subscription;

/**
 * Created by hunt on 16-5-23.
 */
public class HomeWonderfulPresenterImpl implements HomeWonderfulContract.Presenter {

    private WeakReference<HomeWonderfulContract.View> viewWeakReference;

    private Subscription onRefreshSubscription;

    public HomeWonderfulPresenterImpl(HomeWonderfulContract.View view) {
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
    private HomeWonderfulContract.View getView() {
        return viewWeakReference != null ? viewWeakReference.get() : null;
    }

}
