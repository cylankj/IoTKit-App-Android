package com.cylan.jiafeigou.n.mvp.impl.splash;


import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;

import java.lang.ref.WeakReference;

/**
 * Created by hunt on 16-5-14.
 */
class SplashPresenterImpl implements SplashContract.Presenter {

    WeakReference<SplashContract.View> splashView;

    public SplashPresenterImpl(SplashContract.View splashView) {
        this.splashView = new WeakReference<SplashContract.View>(splashView);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}

