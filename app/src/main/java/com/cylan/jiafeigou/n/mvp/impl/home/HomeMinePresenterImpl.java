package com.cylan.jiafeigou.n.mvp.impl.home;

import android.support.annotation.Nullable;
import android.util.Log;

import com.cylan.jiafeigou.n.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.utils.RandomUtils;

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
public class HomeMinePresenterImpl implements HomePageListContract.Presenter {

    private WeakReference<HomePageListContract.View> viewWeakReference;

    private Subscription onRefreshSubscription;

    public HomeMinePresenterImpl(HomePageListContract.View view) {
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
    private HomePageListContract.View getView() {
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
    private List<DeviceBean> requestList() {
        int count = RandomUtils.getRandom(20);
        List<DeviceBean> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            DeviceBean baseBean = new DeviceBean();
            baseBean.id = i;
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
                .map(new Func1<String, List<DeviceBean>>() {
                    @Override
                    public List<DeviceBean> call(String s) {
                        return requestList();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<DeviceBean>>() {
                    @Override
                    public void call(List<DeviceBean> list) {
                        if (getView() != null) getView().onDeviceListRsp(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    @Override
    public void onDeleteItem(DeviceBean deviceBean) {
        Log.d("hunt", "hunt....delete item: " + deviceBean);
    }
}
