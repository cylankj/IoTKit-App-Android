package com.cylan.jiafeigou.n.presenter.home;

import android.support.annotation.UiThread;

import com.cylan.jiafeigou.n.view.home.view.HomePageView;

/**
 * Created by hunt on 16-5-14.
 */
public class HomePageListPresenterImpl implements HomePageListPresenter {
    HomePageView homePageListView;

    @UiThread
    public HomePageListPresenterImpl(HomePageView homePageListView) {
        this.homePageListView = homePageListView;
        homePageListView.initView();
        start();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
