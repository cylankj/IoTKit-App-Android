package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.jiafeigou.misc.VisitorLoader;
import com.cylan.jiafeigou.n.mvp.contract.cam.FaceStrangerContract;
import com.cylan.jiafeigou.n.mvp.contract.cam.VisitorListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractFragmentPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 17-10-20.
 */

public class VisitorStrangerPresenter extends AbstractFragmentPresenter<FaceStrangerContract.View>
        implements FaceStrangerContract.Presenter {


    public VisitorStrangerPresenter(FaceStrangerContract.View view) {
        super(view);
    }

    @Override
    public void fetchVisitorList() {
        Subscription subscription = VisitorLoader.loadAllStrangerList(getUuid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(r -> mView != null)
                .subscribe(visitorList -> mView.onStrangerVisitorListReady(visitorList), AppLogger::e);
        addSubscription(subscription, "fetchVisitorList");
    }
}
