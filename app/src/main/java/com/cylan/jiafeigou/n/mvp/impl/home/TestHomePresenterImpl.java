package com.cylan.jiafeigou.n.mvp.impl.home;

import android.support.annotation.NonNull;

import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.n.mvp.contract.home.NewHomeActivityContract;

import java.lang.ref.WeakReference;

/**
 * Created by hunt on 16-5-23.
 */
public class TestHomePresenterImpl implements NewHomeActivityContract.Presenter {

    private WeakReference<NewHomeActivityContract.View> viewWeakReference;


    public TestHomePresenterImpl(NewHomeActivityContract.View view) {
        viewWeakReference = new WeakReference<>(view);
        view.setPresenter(this);
    }

    @NonNull
    @Override
    public Device getDevice() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void startUpdate() {

    }
}
