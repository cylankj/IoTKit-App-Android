package com.cylan.jiafeigou.n.mvp.impl.splash;


import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.contract.ModelContract;
import com.cylan.jiafeigou.n.mvp.model.impl.SplashModelImpl;

/**
 * Created by hunt on 16-5-14.
 */
public class SplashPresenterImpl extends AbstractPresenter<SplashContract.View> implements SplashContract.Presenter, SplashContract.PresenterRequiredOps {

    private ModelContract.SplashModelOps mModel;

    public SplashPresenterImpl(SplashContract.View splashView) {
        super(splashView);
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
        final SplashContract.View mViewRef = getView();
        if (mViewRef != null) mViewRef.timeSplashed();
    }

    @Override
    public void onFinishDelayed() {
        final SplashContract.View mViewRef = getView();
        if (mViewRef != null) mViewRef.finishDelayed();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}

