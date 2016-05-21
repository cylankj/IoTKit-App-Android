package com.cylan.jiafeigou.n.presenter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.widget.Toast;

import com.cylan.jiafeigou.n.view.BaseView;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import support.network.ConnectivityStatus;
import support.network.ReactiveNetwork;
import support.network.WifiSignalLevel;

/**
 * Created by hunt on 16-5-20.
 */
public class TestPresenter extends AbstractPresenter {
    private Context context;
    BaseView baseView;
    private ReactiveNetwork reactiveNetwork;
    private Subscription wifiSubscription;
    private Subscription networkConnectivitySubscription;
    private Subscription internetConnectivitySubscription;
    private Subscription signalLevelSubscription;

    public TestPresenter(BaseView baseView) {
        this.baseView = baseView;
        this.context = baseView.getContext();
    }

    private static final String TAG = "TestPresenter";

    @Override
    public void start() {
        Toast.makeText(baseView.getContext(), "start: ", Toast.LENGTH_LONG).show();
        Task<String> task = new TestTask();
        submitCallbackUI(task, Schedulers.newThread());
        initNetwork();
    }

    private void initNetwork() {
        reactiveNetwork = new ReactiveNetwork();
        networkConnectivitySubscription =
                reactiveNetwork.observeNetworkConnectivity(context)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ConnectivityStatus>() {
                            @Override
                            public void call(final ConnectivityStatus status) {
                                Log.d(TAG, status.toString());

                                final boolean isOffline = status == ConnectivityStatus.OFFLINE;
                                final boolean isMobileConnected = status == ConnectivityStatus.MOBILE_CONNECTED;

                                if (isOffline || isMobileConnected) {
                                    final String description = WifiSignalLevel.NO_SIGNAL.description;
                                    Log.d(TAG, "description: " + description);
                                }
                            }
                        });

        internetConnectivitySubscription = reactiveNetwork.observeInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean isConnectedToInternet) {
                        Log.d(TAG, "isConnectedToInternet: " + isConnectedToInternet);
                    }
                });

        signalLevelSubscription = reactiveNetwork.observeWifiSignalLevel(context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<WifiSignalLevel>() {
                    @Override
                    public void call(final WifiSignalLevel level) {
                        Log.d(TAG, level.toString());
                        final String description = level.description;
                        Log.d(TAG, "signalLevelSubscription: " + description);
                    }
                });

        wifiSubscription = reactiveNetwork.observeWifiAccessPoints(context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ScanResult>>() {
                    @Override
                    public void call(final List<ScanResult> scanResults) {
                        Log.d(TAG, "scanResults: " + scanResults);
                    }
                });
    }

    @Override
    public void stop() {
        safelyUnSubscribe(networkConnectivitySubscription, internetConnectivitySubscription
                , signalLevelSubscription, wifiSubscription);
    }

    /**
     * @param subscriptions :取消订阅
     */
    private void safelyUnSubscribe(Subscription... subscriptions) {
        for (Subscription subscription : subscriptions) {
            if (subscription != null && !subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
    }

    private class TestTask implements Task<String> {

        @Override
        public String taskStart() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "ahdhdh");
            return "ni hao";
        }

        @Override
        public void taskFinish(String task) {
            Toast.makeText(baseView.getContext(), "result: " + task, Toast.LENGTH_LONG).show();
        }

        @Override
        public void taskFailed(Throwable throwable) {
            Log.d(TAG, ": " + throwable.toString());
        }
    }
}
