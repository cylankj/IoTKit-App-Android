package com.cylan.jiafeigou.model.compl;

import android.os.Handler;

import com.cylan.jiafeigou.model.SplashModelOps;
import com.cylan.jiafeigou.presenter.SplashPresenter;
import com.cylan.jiafeigou.utils.CacheUtil;

import support.uil.core.ImageLoader;

/**
 * Created by chen on 5/12/16.
 */
public class SplashModelCompl implements SplashModelOps {

    private SplashPresenter.RequiredOps mPresenter;


    public SplashModelCompl (SplashPresenter.RequiredOps mPresenter) {
        this.mPresenter = mPresenter;
    }

    @Override
    public void showTimeda() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPresenter.onTimeShowed();
            }
        }, 3000);

    }

    @Override
    public void initCacheda() {
        ImageLoader.getInstance().getMemoryCache().clear();
        ImageLoader.getInstance().getDiskCache().clear();
        CacheUtil.clear();
        mPresenter.onCacheInited();
    }
}
