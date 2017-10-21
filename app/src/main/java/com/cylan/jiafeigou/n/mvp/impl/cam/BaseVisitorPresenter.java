package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.jiafeigou.misc.VisitorLoader;
import com.cylan.jiafeigou.n.mvp.contract.cam.VisitorListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractFragmentPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 17-10-20.
 */

public class BaseVisitorPresenter extends AbstractFragmentPresenter<VisitorListContract.View>
        implements VisitorListContract.Presenter {


    public BaseVisitorPresenter(VisitorListContract.View view) {
        super(view);
    }

    @Override
    public void fetchVisitorList() {
        Subscription subscription = VisitorLoader.loadAllVisitorList(getUuid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(r -> mView != null)
                .subscribe(visitorList -> mView.onVisitorListReady(visitorList), AppLogger::e);
        addSubscription(subscription, "fetchVisitorList");
    }

    @Override
    public void start() {
        super.start();
        fetchVisitorList();
    }
}
