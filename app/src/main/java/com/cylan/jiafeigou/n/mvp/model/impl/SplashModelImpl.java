package com.cylan.jiafeigou.n.mvp.model.impl;

import android.os.Handler;

import com.cylan.jiafeigou.n.mvp.model.contract.ModelContract;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;

/**
 * Created by chen on 5/25/16.
 */
public class SplashModelImpl implements ModelContract.SplashModelOps {

    private SplashContract.PresenterRequiredOps mPresenter;
    private Handler mHandler;

    public SplashModelImpl(SplashContract.PresenterRequiredOps splashPresenter) {
        mPresenter = splashPresenter;
    }

    @Override
    public void splashTimeda() {
        if (mHandler == null)
            mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPresenter.onTimeSplashed();
            }
        }, 900);
    }

    @Override
    public void finishAppDalayda() {
        if (mHandler == null)
            mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPresenter.onFinishDelayed();
            }
        }, 1000);
    }


}
