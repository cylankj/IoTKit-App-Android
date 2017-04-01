package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.text.TextUtils;

import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.ScanResultListFilter;
import com.cylan.jiafeigou.misc.bind.AFullBind;
import com.cylan.jiafeigou.misc.bind.IBindResult;
import com.cylan.jiafeigou.misc.bind.SimpleBindFlow;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.mvp.contract.bind.ConfigApContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ShareUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.R.attr.id;
import static android.R.id.list;
import static android.R.string.ok;
import static com.tencent.bugly.crashreport.inner.InnerAPI.context;

/**
 * Created by cylan-hunt on 16-7-8.
 */
public class ConfigApPresenterImpl extends AbstractPresenter<ConfigApContract.View>
        implements ConfigApContract.Presenter, IBindResult {

//    private Network network;

    private AFullBind aFullBind;
    private boolean onLocalFlowFinish = false;

    public ConfigApPresenterImpl(ConfigApContract.View view) {
        super(view);
        view.setPresenter(this);
        aFullBind = new SimpleBindFlow(this);
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{
                WifiManager.RSSI_CHANGED_ACTION,
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION,
                WifiManager.NETWORK_STATE_CHANGED_ACTION,
                ConnectivityManager.CONNECTIVITY_ACTION
        };
    }

    @Override
    public void sendWifiInfo(final String ssid, final String pwd, final int type) {
        //1.先发送ping,等待ping_ack
        //2.发送fping,等待fping_ack
        //3.发送setServer,setLanguage
        //4.发送sendWifi
        String shortCid = getCurrentBindCidInShort();
        if (TextUtils.isEmpty(shortCid)) {
            getView().lossDogConnection();
            return;
        }
        Subscription subscription = aFullBind.getBindObservable(false, shortCid)
                .subscribeOn(Schedulers.newThread())
                .filter(udpDevicePortrait -> udpDevicePortrait != null && udpDevicePortrait.net != 3)
                .subscribe((UdpConstant.UdpDevicePortrait udpDevicePortrait) -> {
                    AppLogger.d(UdpConstant.BIND_TAG + "last state");
                    if (aFullBind != null) {
                        aFullBind.setServerLanguage(udpDevicePortrait);
                        aFullBind.sendWifiInfo(ssid, pwd, type);
                    }
                }, throwable -> {
                    AppLogger.e("err: " + throwable.getLocalizedMessage());
                });
        aFullBind.startPingFPing(shortCid);
        addSubscription(subscription, "startPingFPing");
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
    public void check3GDogCase() {
        String shortCid = getCurrentBindCidInShort();
        if (TextUtils.isEmpty(shortCid)) {
            getView().lossDogConnection();
            return;
        }
        Subscription subscription = aFullBind.getBindObservable(false, shortCid)
                .subscribeOn(Schedulers.newThread())
                //网络为3
                .filter(udpDevicePortrait -> udpDevicePortrait != null && udpDevicePortrait.net == 3)
                .subscribe((UdpConstant.UdpDevicePortrait udpDevicePortrait) -> {
                    AppLogger.d(UdpConstant.BIND_TAG + "start bind 3g last state");
                    if (aFullBind != null) {
                        aFullBind.setServerLanguage(udpDevicePortrait);
                        aFullBind.sendWifiInfo("", "", 0);
                    }
                }, throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
        aFullBind.startPingFPing(shortCid);
        addSubscription(subscription, "getBindObservable");
    }

    @Override
    public void clearConnection() {
    }

    private String getCurrentBindCidInShort() {
        WifiManager wifiManager = (WifiManager) ContextUtils.getContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && JFGRules.isCylanDevice(wifiInfo.getSSID()))
            return BindUtils.filterCylanDeviceShortCid(wifiInfo.getSSID());
        else return "";
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

    /**
     * wifi列表
     */
    private void updateWifiResults() {
        WifiManager wifiManager = (WifiManager) ContextUtils.getContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanResults = wifiManager.getScanResults();
        Subscription subscription = Observable.just(scanResults)
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
        addSubscription(subscription, "updateWifiResults");
    }

    /**
     * 连接状态变化
     */
    private void updateConnectivityStatus(int network) {
        Subscription subscription = Observable.just(network)
                .filter((Integer integer) -> getView() != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Integer integer) -> {
                    getView().onNetStateChanged(integer);
                });
        addSubscription(subscription, "updateConnectivityStatus");
    }

    /**
     * 可能连上其他非 'DOG-xxx'
     *
     * @param networkInfo
     */
    private void updateConnectInfo(NetworkInfo networkInfo) {
        Subscription subscription = Observable.just(networkInfo)
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
        addSubscription(subscription, "updateConnectInfo");
    }

    @Override
    public void pingFPingFailed() {
        Subscription subscription = Observable.just(null)
                .filter((Object o) -> {
                    return getView() != null;
                })
                .delay(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Object o) -> {
                    getView().pingFailed();
                });
        addSubscription(subscription, "pingFPingFailed");
    }


    @Override
    public void needToUpgrade() {
        Subscription subscription = Observable.just(getView())
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
        addSubscription(subscription, "needToUpgrade");
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
        Subscription subscription = Observable.just(null)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map((Object o) -> {
                    onLocalFlowFinish = true;
                    WifiManager wifiManager = (WifiManager) ContextUtils.getContext().getSystemService(Context.WIFI_SERVICE);
                    List<WifiConfiguration> list =
                            wifiManager.getConfiguredNetworks();
                    WifiInfo info = wifiManager.getConnectionInfo();
                    AppLogger.d("当前连接的网络:" + info.getSSID() + ":" + info.getNetworkId());
                    boolean disconnect = wifiManager.disconnect();
                    AppLogger.d("断开网络是否成功:" + disconnect);
                    boolean disableNetwork = wifiManager.disableNetwork(info.getNetworkId());
                    AppLogger.d("禁用网络是否成功:" + disableNetwork);
                    if (list != null) {
                        int highPriority = -1;
                        int index = -1;
                        for (int i = 0; i < list.size(); i++) {
                            String ssid = list.get(i).SSID;
                            if (!JFGRules.isCylanDevice(ssid)) {
                                //恢复之前连接过的wifi
                                if (highPriority < list.get(i).priority) {
                                    highPriority = list.get(i).priority;
                                    index = i;
                                }
                            }
                        }
                        if (index != -1) {
                            boolean enableNetwork = wifiManager.enableNetwork(list.get(index).networkId, false);
                            AppLogger.d("re enable ssid: " + list.get(index).SSID + "success:" + enableNetwork);
                            boolean reconnect = wifiManager.reconnect();
                            AppLogger.d("re connect :" + reconnect);
                        }
                    }
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(s -> {
                    AppLogger.d("onLocalFlowFinish");
                    getView().onSetWifiFinished(aFullBind.getDevicePortrait());
                    return s;
                })
                .subscribe();
        addSubscription(subscription, "onLocalFlowFinish");
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals(action, WifiManager.RSSI_CHANGED_ACTION)
                || TextUtils.equals(action, WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            if (!onLocalFlowFinish)
                updateWifiResults();
        } else if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (!onLocalFlowFinish) {
                ConnectivityStatus status = ReactiveNetwork.getConnectivityStatus(context);
                updateConnectivityStatus(status.state);
            }
        } else if (TextUtils.equals(action, WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (!onLocalFlowFinish)
                updateConnectInfo(info);
        }
    }

}
