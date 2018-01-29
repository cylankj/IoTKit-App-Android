package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.ScanResultListFilter;
import com.cylan.jiafeigou.misc.bind.AFullBind;
import com.cylan.jiafeigou.misc.bind.IBindResult;
import com.cylan.jiafeigou.misc.bind.SimpleBindFlow;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.mvp.contract.bind.ConfigApContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.BindHelper;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import permissions.dispatcher.PermissionUtils;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.bind.UdpConstant.BIND_TAG;
import static com.cylan.jiafeigou.utils.BindUtils.TAG_UDP_FLOW;

/**
 * Created by cylan-hunt on 16-7-8.
 */
public class ConfigApPresenterImpl extends AbstractPresenter<ConfigApContract.View>
        implements ConfigApContract.Presenter, IBindResult {

//    private Network network;

    private AFullBind aFullBind;
    private boolean onLocalFlowFinish = false;

    private BindHelper.BindContext bindContext;

    public ConfigApPresenterImpl(ConfigApContract.View view) {
        super(view);
        bindContext = new BindHelper.BindContext(uuid);
        aFullBind = new SimpleBindFlow(this);
        monitorLocalUdpMessage();
    }

    private void monitorLocalUdpMessage() {
        RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                .observeOn(Schedulers.io())
                .retry()
                .subscribe(localUdpMsg -> BindHelper.considerUsefulLocalMessage(bindContext, localUdpMsg), new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
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
        PerformanceUtils.startTrace(TAG_UDP_FLOW);
        String shortCid = getCurrentBindCidInShort();
        if (TextUtils.isEmpty(shortCid)) {
            getView().check3gFinish();
//            return;
        }
        Subscription subscription = aFullBind.getBindObservable(false, shortCid)
                .subscribeOn(Schedulers.io())
                .filter(udpDevicePortrait -> udpDevicePortrait != null && udpDevicePortrait.net != 3)
                .subscribe((UdpConstant.UdpDevicePortrait udpDevicePortrait) -> {
                    AppLogger.w(BIND_TAG + "last state");
                    Device device = DataSourceManager.getInstance().getDevice(udpDevicePortrait.uuid);
                    if (device.available()) {
                        device.setValue(201, new DpMsgDefine.DPNet());//先清空防止过早绑定成功
                    }
                    if (aFullBind != null) {
                        AppLogger.w("setServerLanguage");
                        aFullBind.setServerLanguage(udpDevicePortrait);
                        AppLogger.w("sendWifiInfo");
                        aFullBind.sendWifiInfo(ssid, pwd, type);
                    }
                }, throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
        addSubscription(subscription, "startPingFPing");
    }

    @Override
    public void sendWifiInfo(String uuid, String ssid, String pwd, int type) {
        aFullBind.getBindObservable(false, /*"290300000012"*/uuid)
                .subscribeOn(Schedulers.io())
                .delay(500, TimeUnit.MILLISECONDS)
                .map(s -> {
                    Device device = DataSourceManager.getInstance().getDevice(uuid);
                    String mac = device.$(202, "");
                    aFullBind.sendWifiInfo(uuid, mac, ssid, pwd, type)
                            .subscribe(ret -> {
                                AppLogger.w("already send info");
                                UdpConstant.UdpDevicePortrait devicePortrait = aFullBind.getDevicePortrait();
                                getView().onSetWifiFinished(devicePortrait == null ? "" : devicePortrait.uuid);
                                //需要恢复网络.
                                MiscUtils.recoveryWiFi();
                            }, throwable -> AppLogger.e("err" + throwable.getLocalizedMessage()));
                    return s;
                })
                .flatMap(s -> RxBus.getCacheInstance().toObservable(RxEvent.SetWifiAck.class)
                        .filter(ret -> ret != null && ret.data != null)
                        .filter(ret -> TextUtils.equals(uuid, ret.data.cid))
                        .timeout(2, TimeUnit.SECONDS))
                .subscribeOn(AndroidSchedulers.mainThread())
                .timeout(5, TimeUnit.SECONDS)
                .subscribe(s -> {
                    getView().onSetWifiFinished(null);
                    AppLogger.w("发送配置成功");
                }, throwable -> {
                    AppLogger.e("err:" + throwable.getLocalizedMessage());
                    if (throwable instanceof TimeoutException) {
                        getView().sendWifiInfoFailed();
                        AppLogger.e("发送配置失败");
                    }
                });


//
//        Device device = DataSourceManager.getInstance().getDevice(uuid);
//        String mac = device.$(202, "");
//        BindHelper.sendWiFiConfig(uuid, mac, ssid, pwd, type)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<JfgUdpMsg.DoSetWifiAck>() {
//                    @Override
//                    public void call(JfgUdpMsg.DoSetWifiAck doSetWifiAck) {
//                        getView().onSetWifiFinished(uuid);
//                        MiscUtils.recoveryWiFi();
//                    }
//                }, new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        if (throwable instanceof TimeoutException) {
//                            getView().sendWifiInfoFailed();
//                            AppLogger.e("发送配置失败");
//                        }
//                    }
//                });
    }

    @Override
    public void checkDeviceState() {
    }

    @Override
    public void refreshWifiList() {
        Observable.just("scan")
                .subscribeOn(Schedulers.io())
//                .delay(500, TimeUnit.MILLISECONDS)
                .subscribe(ret -> {
                    WifiManager wifiManager = (WifiManager) ContextUtils.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifiManager.startScan();
                }, AppLogger::e);
    }

    @Override
    public void check3GDogCase() {
        String shortCid = getCurrentBindCidInShort();
        if (TextUtils.isEmpty(shortCid)) {
            getView().check3gFinish();
            return;
        }
        Subscription subscription = aFullBind.getBindObservable(false, shortCid)
                .subscribeOn(Schedulers.io())
                .delay(1, TimeUnit.SECONDS)
                //网络为3
                .filter(udpDevicePortrait -> {
                    AppLogger.w(UdpConstant.BIND_TAG + new Gson().toJson(udpDevicePortrait));
                    return udpDevicePortrait != null && udpDevicePortrait.net > 1;
                })
                .map(udpDevicePortrait -> {
                    AppLogger.w(UdpConstant.BIND_TAG + "initSubscription bind 3g last state");
                    if (aFullBind != null) {
                        aFullBind.setServerLanguage(udpDevicePortrait);
                        aFullBind.sendWifiInfo("", "", 0);
                    }
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null)
                .doOnCompleted(() -> {
                    AppLogger.w("取消loading");
                    mView.check3gFinish();
                })
                .subscribe(result -> {
                }, throwable -> {
                    mView.check3gFinish();
                    AppLogger.e("err: " + throwable.getLocalizedMessage());
                });
        addSubscription(subscription, "getBindObservable");
    }

    @Override
    public void clearConnection() {
    }

    private String getCurrentBindCidInShort() {
        WifiManager wifiManager = (WifiManager) ContextUtils.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && JFGRules.isCylanDevice(wifiInfo.getSSID())) {
            return BindUtils.filterCylanDeviceShortCid(wifiInfo.getSSID());
        } else {
            Device device = DataSourceManager.getInstance().getDevice(uuid);
            DpMsgDefine.DPNet dpNet = device.$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
            if (TextUtils.equals(dpNet.ssid, wifiInfo == null ? "" : wifiInfo.getSSID().replace("\"", ""))) {
                return device.uuid.length() >= 6 ? device.uuid.substring(device.uuid.length() - 6, device.uuid.length()) : device.uuid;
            }
            return "";
        }
    }

    @Override
    public boolean isConnectDog() {
        return aFullBind != null && aFullBind.getDevicePortrait() != null;
    }

    @Override
    public void finish() {
        stop();
        if (aFullBind != null) {
            aFullBind.clean();
        }
    }

    @Override
    public void performSendWiFiConfig(String ssid, String password, int type) {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                bindContext.onUpdateWiFiConfig(ssid, password, type);
                BindHelper.checkParamsForEventType(bindContext, BindHelper.EVENT_TYPE_WIFI_CONFIG);
                boolean noError = BindHelper.isNoError(bindContext);
                if (!noError) {
                    noError = BindHelper.performRepairAction(bindContext, bindContext.getErrorCode());
                }
                if (!noError) {
                    subscriber.onError(new IllegalStateException("非法的绑定环境:" + bindContext.getErrorCode() + ",错误信息为:" + bindContext.getErrorMessage()));
                    return;
                }

                BindHelper.performPingAction(bindContext);
                mView.onSendWiFiConfigPrepared();
            }
        })
                .flatMap(cmd -> RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class))
                .first(localUdpMsg -> {
                    BindHelper.considerUsefulLocalMessage(bindContext, localUdpMsg);
                    BindHelper.checkParamsForEventType(bindContext, BindHelper.EVENT_TYPE_WIFI_CONFIG);
                    return BindHelper.isNoError(bindContext);
                })
                .map(localUdpMsg -> {
                    BindHelper.sendWiFiConfig(bindContext);
                    return "";
                });
    }

    /**
     * wifi列表
     */
    private void updateWifiResults() {
        boolean hasSelfPermissions = PermissionUtils.hasSelfPermissions(getView().getContext(), Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasSelfPermissions) {
            WifiManager wifiManager = (WifiManager) ContextUtils.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> scanResults = wifiManager.getScanResults();
            Subscription subscription = Observable.just(scanResults)
                    //别那么频繁
                    .throttleFirst(200, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .filter((List<ScanResult> s) -> {
                        //非空返回,如果空,下面的map是不会有结果.
                        return getView() != null;
                    })
                    .map((List<ScanResult> s) -> ScanResultListFilter.extractPretty(scanResults, false))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((List<ScanResult> s) -> getView().onWiFiResult(s), new RxHelper.EmptyException("resultList call"));
            addSubscription(subscription, "updateWifiResults");
        } else {
            AppLogger.d("当前无法获取 WiFi 列表,请先开启位置权限再试");
            AndroidSchedulers.mainThread().createWorker().schedule(() -> getView().onAccessLocationPermissionRejected());
        }
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
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
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
                    getView().check3gFinish();
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
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
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
        addSubscription(subscription, "pingFPingFailed");
    }


    @Override
    public void needToUpgrade() {
        Subscription subscription = Observable.just(getView())
                .flatMap(new Func1<ConfigApContract.View, Observable<ConfigApContract.View>>() {
                    @Override
                    public Observable<ConfigApContract.View> call(ConfigApContract.View view) {
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
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
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
                    MiscUtils.recoveryWiFi();
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(s -> {
                    AppLogger.d("onLocalFlowFinish");
                    UdpConstant.UdpDevicePortrait devicePortrait = aFullBind.getDevicePortrait();
                    if (devicePortrait != null) {
                        getView().onSetWifiFinished(devicePortrait.uuid);
                    }
                    return s;
                })
                .subscribe(ret -> {
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
        addSubscription(subscription, "onLocalFlowFinish");
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals(action, WifiManager.RSSI_CHANGED_ACTION)
                || TextUtils.equals(action, WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            if (!onLocalFlowFinish) {
                updateWifiResults();
            }
        } else if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (!onLocalFlowFinish) {
                ConnectivityStatus status = ReactiveNetwork.getConnectivityStatus(context);
                updateConnectivityStatus(status.state);
            }
        } else if (TextUtils.equals(action, WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (!onLocalFlowFinish) {
                updateConnectInfo(info);
            }
        }
    }

}
