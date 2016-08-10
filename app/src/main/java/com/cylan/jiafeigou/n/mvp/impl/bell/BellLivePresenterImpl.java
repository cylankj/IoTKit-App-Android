package com.cylan.jiafeigou.n.mvp.impl.bell;

import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * Created by cylan-hunt on 16-8-10.
 */
public class BellLivePresenterImpl extends AbstractPresenter<BellLiveContract.View> implements
        BellLiveContract.Presenter {

    public BellLivePresenterImpl(BellLiveContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void onPickup() {

    }

    @Override
    public void onDismiss() {

    }

    @Override
    public void onMike(int on) {

    }

    @Override
    public void onCapture() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
