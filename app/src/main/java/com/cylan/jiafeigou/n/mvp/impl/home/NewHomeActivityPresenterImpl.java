package com.cylan.jiafeigou.n.mvp.impl.home;

import android.support.annotation.Nullable;

import com.cylan.jiafeigou.n.mvp.contract.home.NewHomeActivityContract;

import java.lang.ref.WeakReference;

import rx.Subscription;

/**
 * Created by hunt on 16-5-23.
 */
public class NewHomeActivityPresenterImpl implements NewHomeActivityContract.Presenter {

    private WeakReference<NewHomeActivityContract.View> viewWeakReference;

    private Subscription onRefreshSubscription;

    public NewHomeActivityPresenterImpl(NewHomeActivityContract.View view) {
        viewWeakReference = new WeakReference<>(view);
        view.setPresenter(this);
        view.initView();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
    }

    @Nullable
    private NewHomeActivityContract.View getView() {
        return viewWeakReference != null ? viewWeakReference.get() : null;
    }


}
