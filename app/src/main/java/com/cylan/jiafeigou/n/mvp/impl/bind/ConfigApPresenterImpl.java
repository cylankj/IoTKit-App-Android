package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;

import com.cylan.jiafeigou.cache.SimpleCache;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.misc.ScanResultListFilter;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.mvp.contract.bind.ConfigApContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.cylan.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-8.
 */
public class ConfigApPresenterImpl extends AbstractPresenter<ConfigApContract.View> implements ConfigApContract.Presenter {

//    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private Subscription accessPeriod;
    private Subscription getAccessPointsSub;
    private Subscription connectSub;

    public ConfigApPresenterImpl(ConfigApContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    /**
     * 热点列表
     *
     * @return
     */
    private Subscription accessPointsPeriod() {
        //实时刷新,2s钟一次
//        return Observable.interval(1000, 2000, TimeUnit.MILLISECONDS)
//                .subscribeOn(Schedulers.newThread())
//                .subscribe(new Action1<Object>() {
//                    @Override
//                    public void call(Object o) {
//                        AppLogger.i("refresh wifi list");
        return refreshAccessPoints();
//                    }
//                });
    }

    /**
     * 刷新列表
     */
    private Subscription refreshAccessPoints() {
        return new ReactiveNetwork()
                .observeWifiAccessPoints(ContextUtils.getContext(), true)
                .subscribeOn(Schedulers.io())
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .flatMap(new Func1<List<ScanResult>, Observable<List<ScanResult>>>() {
                    @Override
                    public Observable<List<ScanResult>> call(List<ScanResult> resultList) {
                        AppLogger.i("refreshAccessPoints");
                        List<ScanResult> newList = new ArrayList<>(ScanResultListFilter.extractPretty(resultList));
                        SimpleCache.getInstance().setWeakScanResult(newList);
                        RxBus.getDefault().postSticky(new RxEvent.ScanResultList(newList));
                        return null;
                    }
                })
                .subscribe();
    }

    /**
     * 更新到ui
     *
     * @return
     */
    private Subscription getAccessPointsSubscription() {
        return RxBus.getDefault().toObservableSticky(RxEvent.ScanResultList.class)
                .subscribeOn(Schedulers.computation())
                .filter(new Func1<RxEvent.ScanResultList, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.ScanResultList scanResultList) {
                        //非空返回,如果空,下面的map是不会有结果.
                        return getView() != null
                                && scanResultList != null
                                && !ListUtils.isEmpty(scanResultList.scanResultList);
                    }
                })
                .map(new Func1<RxEvent.ScanResultList, List<ScanResult>>() {
                    @Override
                    public List<ScanResult> call(RxEvent.ScanResultList scanResultList) {
                        //相当于实时刷新,不用合并.
                        return scanResultList.scanResultList;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<List<ScanResult>, Object>() {
                    @Override
                    public Object call(List<ScanResult> scanResults) {
                        getView().onWiFiResult(scanResults);
                        return null;
                    }
                })
                .retry(exceptionFun)
                .subscribe();
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
        AppLogger.i("refresh registerWiFiBroadcast");
        unSubscribe(accessPeriod, getAccessPointsSub, connectSub);
        accessPeriod = accessPointsPeriod();
        getAccessPointsSub = getAccessPointsSubscription();
        connectSub = connectivitySubscription();
    }

    @Override
    public void sendWifiInfo(String ssid, String pwd, int type) {
        //1.先发送ping,等待ping_ack
        //2.发送fping,等待fping_ack
        //3.发送setServer,setLanguage
        //4.发送sendWifi
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP,
                                UdpConstant.PORT,
                                new JfgUdpMsg.Ping().toBytes());
                    }
                });
    }

    @Override
    public void start() {

    }

    /**
     * 异常情况下，返回true,将继续订阅
     */
    private Func2<Integer, Throwable, Boolean> exceptionFun = new Func2<Integer, Throwable, Boolean>() {
        @Override
        public Boolean call(Integer integer, Throwable throwable) {
            //此处return true:表示继续订阅，
            AppLogger.e("ConfigApPresenterImpl: " + throwable.getLocalizedMessage());
            return true;
        }
    };

    @Override
    public void stop() {
        unSubscribe(accessPeriod, getAccessPointsSub, connectSub);
    }
}
