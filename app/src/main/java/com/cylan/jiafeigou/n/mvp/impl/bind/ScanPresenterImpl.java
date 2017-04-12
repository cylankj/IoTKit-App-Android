package com.cylan.jiafeigou.n.mvp.impl.bind;

import com.cylan.jiafeigou.n.mvp.contract.bind.ScanContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

import rx.Subscription;

/**
 * Created by cylan-hunt on 16-7-7.
 */
public class ScanPresenterImpl extends AbstractPresenter<ScanContract.View> implements ScanContract.Presenter {

    private Subscription subscription;

    public ScanPresenterImpl(ScanContract.View v) {
        super(v);
        v.setPresenter(this);
    }


    @Override
    public void stop() {
        super.stop();
        unSubscribe(subscription);
    }
}
