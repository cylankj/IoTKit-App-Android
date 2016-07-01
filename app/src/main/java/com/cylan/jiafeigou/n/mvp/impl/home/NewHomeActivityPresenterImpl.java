package com.cylan.jiafeigou.n.mvp.impl.home;

import com.cylan.jiafeigou.n.mvp.contract.home.NewHomeActivityContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

import rx.Subscription;

/**
 * Created by hunt on 16-5-23.
 */
public class NewHomeActivityPresenterImpl extends AbstractPresenter<NewHomeActivityContract.View> implements NewHomeActivityContract.Presenter {


    private Subscription onRefreshSubscription;

    public NewHomeActivityPresenterImpl(NewHomeActivityContract.View view) {
        super(view);
        view.setPresenter(this);
        view.initView();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
    }


}
