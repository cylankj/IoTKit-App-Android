package com.cylan.jiafeigou.n.mvp.impl.bell;

import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.utils.RandomUtils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class DBellHomePresenterImpl extends AbstractPresenter<DoorBellHomeContract.View>
        implements DoorBellHomeContract.Presenter {
    public DBellHomePresenterImpl(DoorBellHomeContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    private CompositeSubscription compositeSubscription = new CompositeSubscription();


    @Override
    public void start() {
        compositeSubscription.add(onBellCallListSubscription());
        compositeSubscription.add(onLogStateSubscription());
        getView().onLoginState(RandomUtils.getRandom(1));
    }

    private Subscription onBellCallListSubscription() {
        return Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Object, ArrayList<BellCallRecordBean>>() {
                    @Override
                    public ArrayList<BellCallRecordBean> call(Object o) {
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<BellCallRecordBean>>() {
                    @Override
                    public void call(ArrayList<BellCallRecordBean> bellInfoBeen) {
                        if (getView() != null) {
                            getView().onRecordsListRsp(bellInfoBeen);
                        }
                    }
                });
    }

    /**
     * 查询登陆状态
     *
     * @return
     */
    private Subscription onLogStateSubscription() {
        return RxBus.getInstance().toObservable()

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (o != null && o instanceof RxEvent.LoginRsp) {
                            if (getView() != null) getView().onLoginState(JFGRules.LOGIN);
                        }
                    }
                });
    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
    }

    @Override
    public void fetchBellRecordsList() {

    }
}
