package com.cylan.jiafeigou.presenter.compl;

import com.cylan.jiafeigou.model.SplashModelOps;
import com.cylan.jiafeigou.model.compl.SplashModelCompl;
import com.cylan.jiafeigou.presenter.SplashPresenter;
import com.cylan.jiafeigou.view.SplashViewRequiredOps;

import java.lang.ref.WeakReference;

/**
 * Created by chen on 5/12/16.
 */
public class SplashPresenterCompl implements SplashPresenter.Ops,SplashPresenter.RequiredOps {

    private final WeakReference<SplashViewRequiredOps> mView;
    SplashModelOps mModel;
    public SplashPresenterCompl(SplashViewRequiredOps mView) {
        this.mView = new WeakReference<SplashViewRequiredOps>(mView);
        this.mModel = new SplashModelCompl(this);
    }


    @Override
    public void initCache() {
        mModel.initCacheda();
    }

    @Override
    public void showTime() {
        mModel.showTimeda();
    }

    @Override
    public void onTimeShowed() {
        mView.get().timeShowed();
    }

    @Override
    public void onCacheInited() {
        mView.get().cacheInited();
    }
}
