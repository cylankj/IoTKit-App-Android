package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.widget.wheel.DataProviderImpl;
import com.cylan.jiafeigou.widget.wheel.SDataStack;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLivePresenterImpl extends AbstractPresenter<CamLiveContract.View> implements CamLiveContract.Presenter {

    private Subscription subscription;

    public CamLivePresenterImpl(CamLiveContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void fetchHistoryData() {
        subscription = Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Object, SDataStack>() {
                    @Override
                    public SDataStack call(Object o) {
                        DataProviderImpl im = new DataProviderImpl();
                        im.setHistoryTimeSet(getHistorySet());
                        return im.initTimeLine();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SDataStack>() {
                    @Override
                    public void call(SDataStack dataStack) {
                        getView().onHistoryDataRsp(dataStack);
                    }
                });
    }

    private long[] getHistorySet() {
        final int maxCount = 100;

        long[] timeSet = new long[maxCount];

        final long startTime = System.currentTimeMillis() - 1000 * 60 * 60 * 100;

        final long currentTime = System.currentTimeMillis();
        final int interval = (int) ((currentTime - startTime) / maxCount);
        for (int i = 0; i < maxCount; i++) {
            timeSet[i] = startTime + i * interval;
        }
        return timeSet;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        unSubscribe(subscription);
    }
}
