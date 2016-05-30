package com.cylan.jiafeigou.n.mvp.impl.splash;


import com.cylan.jiafeigou.n.model.contract.ModelContract;
import com.cylan.jiafeigou.n.model.impl.SplashModelImpl;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;

import java.lang.ref.WeakReference;

/**
 * Created by hunt on 16-5-14.
 */
public class SplashPresenterImpl implements SplashContract.PresenterOps, SplashContract.PresenterRequiredOps {

    private WeakReference<SplashContract.ViewRequiredOps> mView;
    private ModelContract.SplashModelOps mModel;

    private SplashContract.ViewRequiredOps curView;

    public SplashPresenterImpl(SplashContract.ViewRequiredOps splashView) {
        this.mView = new WeakReference<SplashContract.ViewRequiredOps>(splashView);
        this.mModel = new SplashModelImpl(this);
    }


    @Override
    public void splashTime() {
        //闪屏时间
        mModel.splashTimeda();
    }

    @Override
    public void finishAppDelay() {
        mModel.finishAppDalayda();
    }


    @Override
    public void onTimeSplashed() {
        curView = mView.get();
        if (curView != null) curView.timeSplashed();
    }

    @Override
    public void onfinishDelayed() {
        curView = mView.get();
        if (curView != null) curView.finishDelayed();
    }
}

