package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.cache.SimpleCache;
import com.cylan.jiafeigou.misc.ScanResultListFilter;
import com.cylan.jiafeigou.n.mvp.contract.bind.ConfigApContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;

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
 * Created by cylan-hunt on 16-7-8.
 */
public class ConfigApPresenterImpl extends AbstractPresenter<ConfigApContract.View> implements ConfigApContract.Presenter {

    private Subscription connectivitySubscription;
    private Subscription accessPointsSubscription;
    private ScanResultListFilter scanResultListFilter;

    public ConfigApPresenterImpl(ConfigApContract.View view) {
        super(view);
        view.setPresenter(this);
        scanResultListFilter = new ScanResultListFilter();
    }

    /**
     * 热点列表
     *
     * @return
     */
    private Subscription accessPointsSubscription() {
        return new ReactiveNetwork()
                .observeWifiAccessPoints(getView().getContext().getApplicationContext(), false)
                .subscribeOn(Schedulers.io())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .flatMap(new Func1<List<ScanResult>, Observable<List<ScanResult>>>() {
                    @Override
                    public Observable<List<ScanResult>> call(List<ScanResult> resultList) {
                        List<ScanResult> newList = new ArrayList<>(scanResultListFilter.extractPretty(resultList));
                        SimpleCache.getInstance().setWeakScanResult(newList);
                        return Observable.just(newList);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ScanResult>>() {
                    @Override
                    public void call(List<ScanResult> resultList) {
                        if (resultList == null
                                || resultList.size() == 0
                                || getView() == null)
                            return;
                        getView().onWiFiResult(resultList);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "err: " + throwable.getLocalizedMessage());
                    }
                });
    }

    private Subscription connectivitySubscription() {
        return new ReactiveNetwork().observeNetworkConnectivity(getView().getContext().getApplicationContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ConnectivityStatus>() {
                    @Override
                    public void call(ConnectivityStatus connectivityStatus) {
                        getView().onWifiStateChanged(connectivityStatus.state);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "err: " + throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public void registerWiFiBroadcast(Context context) {
        if (connectivitySubscription != null && !connectivitySubscription.isUnsubscribed())
            connectivitySubscription.unsubscribe();
        connectivitySubscription = connectivitySubscription();
        if (accessPointsSubscription != null && !connectivitySubscription.isUnsubscribed())
            accessPointsSubscription.unsubscribe();
        accessPointsSubscription = accessPointsSubscription();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        unSubscribe(connectivitySubscription, accessPointsSubscription);
    }
}
