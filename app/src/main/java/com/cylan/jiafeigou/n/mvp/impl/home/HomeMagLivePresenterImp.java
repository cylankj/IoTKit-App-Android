package com.cylan.jiafeigou.n.mvp.impl.home;

import com.cylan.jiafeigou.n.mvp.contract.home.HomeMagLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/9/19
 * 描述：
 */
public class HomeMagLivePresenterImp extends AbstractPresenter<HomeMagLiveContract.View> implements HomeMagLiveContract.Presenter {

    public HomeMagLivePresenterImp(HomeMagLiveContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void clearOpenAndCloseRecord() {

    }
}
