package com.cylan.jiafeigou.n.mvp.impl.home;


import android.support.annotation.Nullable;

import com.cylan.jiafeigou.n.model.MediaBean;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;
import com.cylan.utils.RandomUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by hunt on 16-5-23.
 */
public class HomeWonderfulPresenterImpl implements HomeWonderfulContract.Presenter {

    private WeakReference<HomeWonderfulContract.View> viewWeakReference;

    private Subscription onRefreshSubscription;

    public HomeWonderfulPresenterImpl(HomeWonderfulContract.View view) {
        viewWeakReference = new WeakReference<>(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        unRegisterSubscription(onRefreshSubscription);
    }

    @Nullable
    private HomeWonderfulContract.View getView() {
        return viewWeakReference != null ? viewWeakReference.get() : null;
    }

    /**
     * 反注册
     *
     * @param subscriptions
     */
    private void unRegisterSubscription(Subscription... subscriptions) {
        if (subscriptions != null)
            for (Subscription subscription : subscriptions) {
                if (subscription != null)
                    subscription.unsubscribe();
            }
    }

    /**
     * 计算过程.
     *
     * @return
     */
    private List<MediaBean> requestList() {
        int count = 4;
        List<MediaBean> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MediaBean baseBean = new MediaBean();
            baseBean.idImage = i;
            baseBean.curTime = "周四: " + i;
            baseBean.srcFrom = "家";
            baseBean.mediaType = RandomUtils.getRandom(4);
            list.add(baseBean);
        }
        return list;
    }


    @Override
    public void startRefresh() {

        final int testDelay = RandomUtils.getRandom(3);
        onRefreshSubscription = Observable.just("")
                .subscribeOn(Schedulers.newThread())
                .delay(testDelay * 1000L, TimeUnit.MILLISECONDS)
                .map(new Func1<String, List<MediaBean>>() {
                    @Override
                    public List<MediaBean> call(String s) {
                        return requestList();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MediaBean>>() {
                    @Override
                    public void call(List<MediaBean> list) {
                        if (getView() != null) getView().onDeviceListRsp(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });

    }


}

