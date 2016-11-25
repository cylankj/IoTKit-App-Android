package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.misc.ScanResultListFilter;
import com.cylan.jiafeigou.misc.bind.AFullBind;
import com.cylan.jiafeigou.misc.bind.IBindResult;
import com.cylan.jiafeigou.misc.bind.SimpleBindFlow;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.mvp.contract.bind.ConfigApContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
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


    //发送wifi配置
    private void setWifiInfo(UdpConstant.UdpDevicePortrait udpDevicePortrait,
                             String ssid, String pwd, int type) {
        JfgUdpMsg.DoSetWifi setWifi = new JfgUdpMsg.DoSetWifi(udpDevicePortrait.cid,
                udpDevicePortrait.mac,
                ssid, pwd);
        setWifi.security = type;
        //发送wifi配置
        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP,
                UdpConstant.PORT,
                setWifi.toBytes());
        AppLogger.i("ConfigApPresenterImpl:" + new Gson().toJson(setWifi));
    }


    @Override
    public void start() {

    }

    @Override
    public void stop() {
    }

    /**
     * wifi列表
     */
    private void updateWifiResults(List<ScanResult> scanResults) {
        Observable.just(scanResults)
                //别那么频繁
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .filter(new Func1<List<ScanResult>, Boolean>() {
                    @Override
                    public Boolean call(List<ScanResult> scanResults) {
                        //非空返回,如果空,下面的map是不会有结果.
                        return getView() != null;
                    }
                })
                .map(new Func1<List<ScanResult>, List<ScanResult>>() {
                    @Override
                    public List<ScanResult> call(List<ScanResult> scanResults) {
                        return ScanResultListFilter.extractPretty(scanResults, false);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ScanResult>>() {
                    @Override
                    public void call(List<ScanResult> scanResults) {
                        getView().onWiFiResult(scanResults);
                    }
                }, new RxHelper.EmptyException("resultList call"));
    }

    /**
     * 连接状态变化
     */
    private void updateConnectivityStatus(int network) {
        Observable.just(network)
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return getView() != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        getView().onNetStateChanged(integer);
                    }
                });
    }

    /**
     * 可能连上其他非 'DOG-xxx'
     *
     * @param info
     */
    private void updateConnectInfo(NetworkInfo info) {
        Observable.just(info)
                .filter(new Func1<NetworkInfo, Boolean>() {
                    @Override
                    public Boolean call(NetworkInfo info) {
                        //连上其他ap
                        final String ssid = info.getExtraInfo().replace("\"", "");
                        return getView() != null
                                && info.getState() == NetworkInfo.State.CONNECTED
                                && !JFGRules.isCylanDevice(ssid);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<NetworkInfo>() {
                    @Override
                    public void call(NetworkInfo info) {
                        getView().lossDogConnection();
                    }
                });
    }

    @Override
    public void pingFPingFailed() {
        Observable.just(null)
                .filter(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object o) {
                        return getView() != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        getView().lossDogConnection();
                    }
                });
    }

    @Override
    public void isMobileNet() {
        //马上跳转
        Observable.just(null)
                .filter(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object o) {
                        return getView() != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        getView().onSetWifiFinished(aFullBind.getDevicePortrait());
                    }
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
                .filter(new Func1<ConfigApContract.View, Boolean>() {
                    @Override
                    public Boolean call(ConfigApContract.View view) {
                        return view != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ConfigApContract.View>() {
                    @Override
                    public void call(ConfigApContract.View view) {
                        view.upgradeDogState(0);
                    }
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
