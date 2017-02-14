package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.ScanResultListFilter;
import com.cylan.jiafeigou.misc.bind.AFullBind;
import com.cylan.jiafeigou.misc.bind.IBindResult;
import com.cylan.jiafeigou.misc.bind.SimpleBindFlow;
import com.cylan.jiafeigou.n.mvp.contract.bind.ConfigApContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-8.
 */
public class ConfigApPresenterImpl extends AbstractPresenter<ConfigApContract.View>
        implements ConfigApContract.Presenter, IBindResult {

    private Network network;

    private AFullBind aFullBind;

    public ConfigApPresenterImpl(ConfigApContract.View view) {
        super(view);
        view.setPresenter(this);
        aFullBind = new SimpleBindFlow(this);
    }

    @Override
    public void registerNetworkMonitor() {
        try {
            if (network == null) {
                network = new Network();
                final IntentFilter filter = new IntentFilter();
                filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
                filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                ContextUtils.getContext().registerReceiver(network, filter);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void unregisterNetworkMonitor() {
        if (network != null) {
            ContextUtils.getContext().unregisterReceiver(network);
            network = null;
        }
    }

    @Override
    public void sendWifiInfo(final String ssid, final String pwd, final int type) {
        //1.先发送ping,等待ping_ack
        //2.发送fping,等待fping_ack
        //3.发送setServer,setLanguage
        //4.发送sendWifi
        AppLogger.i("sendWifiInfo:start:");
        WifiManager wifiManager = (WifiManager) ContextUtils.getContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null || !JFGRules.isCylanDevice(wifiInfo.getSSID())) {
            //something happened
            AppLogger.i("ConfigApPresenterImpl 连接错误");
            getView().lossDogConnection();
            return;
        }
        final String ssidInDigits = BindUtils.getDigitsString(wifiInfo.getSSID());
        if (!TextUtils.isDigitsOnly(ssidInDigits)) {

        }
        if (aFullBind != null)
            aFullBind.sendWifiInfo(ssid, pwd, type);
        AppLogger.i("sendWifiInfo: " + (aFullBind != null));
    }

    @Override
    public void checkDeviceState() {
    }

    @Override
    public void refreshWifiList() {
        WifiManager wifiManager = (WifiManager) ContextUtils.getContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
    }

    @Override
    public void clearConnection() {
    }

    @Override
    public void startPingFlow() {
        if (aFullBind != null) {
            WifiManager wifiManager = (WifiManager) ContextUtils.getContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo == null || !JFGRules.isCylanDevice(wifiInfo.getSSID())) {
                //something happened
                AppLogger.i("ConfigApPresenterImpl 连接错误");
                getView().lossDogConnection();
                return;
            }
            //纯数字
            final String ssidInDigits = BindUtils.getDigitsString(wifiInfo.getSSID());
            aFullBind.startPingFPing(ssidInDigits);
        }


    }

    @Override
    public boolean isConnectDog() {
        return aFullBind != null && aFullBind.getDevicePortrait() != null;
    }

    @Override
    public void finish() {
        stop();
        if (aFullBind != null)
            aFullBind.clean();
    }


    @Override
    public void start() {
        super.start();
        registerNetworkMonitor();
    }

    @Override
    public void stop() {
        super.stop();
        unregisterNetworkMonitor();
    }


    /**
     * wifi列表
     */
    private void updateWifiResults(List<ScanResult> scanResults) {
        Observable.just(scanResults)
                //别那么频繁
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .filter((List<ScanResult> s) -> {
                    //非空返回,如果空,下面的map是不会有结果.
                    return getView() != null;
                })
                .map((List<ScanResult> s) -> ScanResultListFilter.extractPretty(scanResults, false))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((List<ScanResult> s) -> {
                    getView().onWiFiResult(s);
                }, new RxHelper.EmptyException("resultList call"));
    }

    /**
     * 连接状态变化
     */
    private void updateConnectivityStatus(int network) {
        Observable.just(network)
                .filter((Integer integer) -> getView() != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Integer integer) -> {
                    getView().onNetStateChanged(integer);
                });
    }

    /**
     * 可能连上其他非 'DOG-xxx'
     *
     * @param networkInfo
     */
    private void updateConnectInfo(NetworkInfo networkInfo) {
        Observable.just(networkInfo)
                .filter((NetworkInfo info) -> {
                    //连上其他ap
                    final String ssid = info.getExtraInfo().replace("\"", "");
                    return getView() != null
                            && info.getState() == NetworkInfo.State.CONNECTED
                            && !JFGRules.isCylanDevice(ssid);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((NetworkInfo info) -> {
                    getView().lossDogConnection();
                });
    }

    @Override
    public void pingFPingFailed() {
        Observable.just(null)
                .filter((Object o) -> {
                    return getView() != null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Object o) -> {
                    getView().lossDogConnection();
                });
    }

    @Override
    public void isMobileNet() {
        //马上跳转
        Observable.just(null)
                .filter((Object o) -> {
                    return getView() != null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Object o) -> {
                    getView().onSetWifiFinished(aFullBind.getDevicePortrait());
                });
    }


    @Override
    public void needToUpgrade() {
        Observable.just(getView())
                .flatMap(new Func1<ConfigApContract.View, Observable<ConfigApContract.View>>() {
                    @Override
                    public Observable<ConfigApContract.View> call(ConfigApContract.View view) {
                        //
                        aFullBind.startUpgrade();
                        return Observable.just(view);
                    }
                })
                .filter((ConfigApContract.View view) -> {
                    return view != null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((ConfigApContract.View view) -> {
                    view.upgradeDogState(0);
                });
    }

    @Override
    public void updateState(int state) {

    }

    @Override
    public void bindFailed() {

    }

    @Override
    public void bindSuccess() {

    }

    @Override
    public void onLocalFlowFinish() {
        getView().onSetWifiFinished(aFullBind.getDevicePortrait());
        Observable.just(null)
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .map((Object o) -> {
                    WifiManager wifiManager = (WifiManager) ContextUtils.getContext().getSystemService(Context.WIFI_SERVICE);
                    List<WifiConfiguration> list =
                            wifiManager.getConfiguredNetworks();
                    if (list != null) {
                        int highPriority = -1;
                        int index = -1;
                        for (int i = 0; i < list.size(); i++) {
                            String ssid = list.get(i).SSID.replace("\"", "");
                            if (JFGRules.isCylanDevice(ssid)) {
                                //找到这个狗,清空他的信息
                                wifiManager.removeNetwork(list.get(i).networkId);
                                AppLogger.i(TAG + "clean dog like ssid: " + ssid);
                            } else {
                                //恢复之前连接过的wifi
                                if (highPriority < list.get(i).priority) {
                                    highPriority = list.get(i).priority;
                                    index = i;
                                }
                            }
                        }
                        if (index != -1) {
                            AppLogger.i("re enable ssid: " + list.get(index).SSID);
                            wifiManager.enableNetwork(list.get(index).networkId, false);
                        }
                    }
                    return null;
                }).subscribe();
    }

    private class Network extends BroadcastReceiver {

        private WifiManager wifiManager;

        public Network() {
            wifiManager = (WifiManager) ContextUtils.getContext().getSystemService(Context.WIFI_SERVICE);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (TextUtils.equals(action, WifiManager.RSSI_CHANGED_ACTION)
                    || TextUtils.equals(action, WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                updateWifiResults(wifiManager.getScanResults());
            } else if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityStatus status = ReactiveNetwork.getConnectivityStatus(context);
                updateConnectivityStatus(status.state);
            } else if (TextUtils.equals(action, WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                updateConnectInfo(info);
            }
        }
    }
}
