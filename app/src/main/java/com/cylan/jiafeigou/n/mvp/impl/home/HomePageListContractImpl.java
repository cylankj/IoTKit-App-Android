package com.cylan.jiafeigou.n.mvp.impl.home;

import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;

import java.lang.ref.WeakReference;

/**
 * Created by hunt on 16-5-23.
 */
public class HomePageListContractImpl implements HomePageListContract.Presenter {

    private WeakReference<HomePageListContract.View> viewWeakReference;


    public HomePageListContractImpl(HomePageListContract.View view) {
        viewWeakReference = new WeakReference<>(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void startRefresh() {

    }
}
