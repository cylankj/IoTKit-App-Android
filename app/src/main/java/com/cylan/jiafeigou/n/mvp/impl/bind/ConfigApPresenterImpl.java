package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;

import com.cylan.jiafeigou.cache.SimpleCache;
import com.cylan.jiafeigou.misc.ScanResultListFilter;
import com.cylan.jiafeigou.n.mvp.contract.bind.ConfigApContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-7-8.
 */
public class ConfigApPresenterImpl extends AbstractPresenter<ConfigApContract.View> implements ConfigApContract.Presenter {

    //    private Subscription connectivitySubscription;
//    private Subscription accessPointsSubscription;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public ConfigApPresenterImpl(ConfigApContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    /**
     * 热点列表
     *
     * @return
     */
    private Subscription accessPointsSubscription() {
        return new ReactiveNetwork()
                .observeWifiAccessPoints(ContextUtils.getContext(), true)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<List<ScanResult>, Observable<List<ScanResult>>>() {
                    @Override
                    public Observable<List<ScanResult>> call(List<ScanResult> resultList) {
                        List<ScanResult> newList = new ArrayList<>(ScanResultListFilter.extractPretty(resultList));
                        SimpleCache.getInstance().setWeakScanResult(newList);
                        return Observable.just(newList);
                    }
                })
                .filter(new Func1<List<ScanResult>, Boolean>() {
                    @Override
                    public Boolean call(List<ScanResult> scanResults) {
                        AppLogger.i("scanResult: " + (scanResults != null && scanResults.size() > 0) + " " + (getView() != null));
                        return !ListUtils.isEmpty(scanResults) && getView() != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ScanResult>>() {
                    @Override
                    public void call(List<ScanResult> resultList) {
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
        return new ReactiveNetwork().observeNetworkConnectivity(ContextUtils.getContext())
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
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed())
            compositeSubscription.unsubscribe();
        compositeSubscription.add(connectivitySubscription());
        compositeSubscription.add(accessPointsSubscription());
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
    }
}
