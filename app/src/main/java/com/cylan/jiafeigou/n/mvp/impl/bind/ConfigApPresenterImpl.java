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
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.RxHelper;
import com.cylan.jiafeigou.misc.ScanResultListFilter;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.engine.task.OfflineTaskQueue;
import com.cylan.jiafeigou.n.mvp.contract.bind.ConfigApContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.cylan.utils.PackageUtils;
import com.google.gson.Gson;

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
public class ConfigApPresenterImpl extends AbstractPresenter<ConfigApContract.View>
        implements ConfigApContract.Presenter {
    private Subscription pingFlowSub;
    private Network network;

    public ConfigApPresenterImpl(ConfigApContract.View view) {
        super(view);
        view.setPresenter(this);
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
        unSubscribe(pingFlowSub);
        //zip用法,合并
        pingFlowSub = Observable.zip(pingObservable(ssidInDigits),
                fPingObservable(ssidInDigits),
                new Func2<JfgUdpMsg.PingAck, JfgUdpMsg.FPingAck, UdpConstant.UdpDevicePortrait>() {
                    @Override
                    public UdpConstant.UdpDevicePortrait call(JfgUdpMsg.PingAck pingAck, JfgUdpMsg.FPingAck fPingAck) {
                        //此处完成了第1和第2步.
                        UdpConstant.UdpDevicePortrait devicePortrait = new UdpConstant.UdpDevicePortrait();
                        devicePortrait.cid = pingAck.cid;
                        devicePortrait.mac = fPingAck.mac;
                        devicePortrait.version = fPingAck.version;
                        devicePortrait.net = pingAck.net;
                        return devicePortrait;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<UdpConstant.UdpDevicePortrait, UdpConstant.UdpDevicePortrait>() {
                    @Override
                    public UdpConstant.UdpDevicePortrait call(final UdpConstant.UdpDevicePortrait udpDevicePortrait) {
                        setServer_Language(udpDevicePortrait);
                        setWifiInfo(udpDevicePortrait, ssid, pwd, type);
                        //此时,设备还没恢复连接,需要加入队列
                        int key = ("JfgCmdInsurance.getCmd().bindDevice" + udpDevicePortrait.cid).hashCode();
                        OfflineTaskQueue.getInstance().enqueue(key, new Runnable() {
                            @Override
                            public void run() {
                                AppLogger.i("bind cid: " + udpDevicePortrait.cid);
                                JfgCmdInsurance.getCmd().bindDevice(udpDevicePortrait.cid, "fxx");
                            }
                        });
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UdpConstant.UdpDevicePortrait>() {
                    @Override
                    public void call(UdpConstant.UdpDevicePortrait o) {
                        if (getView() != null) {
                            getView().onSetWifiFinished(o);
                        }
                    }
                }, new RxHelper.EmptyException("binding flow"));
        sendPing_FPing();
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
        Observable.just(null)
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        WifiManager wifiManager = (WifiManager) ContextUtils.getContext().getSystemService(Context.WIFI_SERVICE);
                        List<WifiConfiguration> list =
                                wifiManager.getConfiguredNetworks();
                        if (list != null) {
                            for (int i = 0; i < list.size(); i++) {
                                String ssid = list.get(i).SSID.replace("\"", "");
                                if (JFGRules.isCylanDevice(ssid)) {
                                    //找到这个狗,清空他的信息
                                    wifiManager.removeNetwork(list.get(i).networkId);
                                    AppLogger.i("clean dog like ssid: " + ssid);
                                }
                            }
                        }
                        return null;
                    }
                }).subscribe();
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

    /**
     * 发送服务器信息,发送timeZone信息
     *
     * @param udpDevicePortrait
     */
    private void setServer_Language(UdpConstant.UdpDevicePortrait udpDevicePortrait) {
        AppLogger.i("ConfigApPresenterImpl: " + udpDevicePortrait);
        final String[] serverDetails = PackageUtils.getMetaString(ContextUtils.getContext(),
                "ServerAddress").split(":");
        String address = serverDetails[0];
        int port = Integer.parseInt(serverDetails[1]);
        //设置语言
        JfgUdpMsg.SetLanguage setLanguage = new JfgUdpMsg.SetLanguage(
                udpDevicePortrait.cid,
                udpDevicePortrait.mac,
                JFGRules.getLanguageType(ContextUtils.getContext()));
        //设置服务器
        JfgUdpMsg.SetServer setServer =
                new JfgUdpMsg.SetServer(udpDevicePortrait.cid,
                        udpDevicePortrait.mac,
                        address,
                        port,
                        80);
        AppLogger.i("setServer: " + new Gson().toJson(setServer));
        AppLogger.i("setLanguage: " + new Gson().toJson(setLanguage));
        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP,
                UdpConstant.PORT,
                setServer.toBytes());
        //
        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP,
                UdpConstant.PORT,
                setLanguage.toBytes());
    }

    /**
     * 此处的消息来源非常关键{@link com.cylan.jiafeigou.n.engine.GlobalUdpDataSource}
     * emit出来 {@link JfgUdpMsg.PingAck},两者都是异步.
     *
     * @param ssidInDigits
     * @return
     */
    private Observable<JfgUdpMsg.PingAck> pingObservable(final String ssidInDigits) {
        return RxBus.getDefault().toObservable(JfgUdpMsg.PingAck.class)
                .filter(new Func1<JfgUdpMsg.PingAck, Boolean>() {
                    @Override
                    public Boolean call(JfgUdpMsg.PingAck pingAck) {
                        //注意条件
                        return !TextUtils.isEmpty(pingAck.cid)
                                && pingAck.cid.endsWith(ssidInDigits);
                    }
                })
                .throttleFirst(100, TimeUnit.MILLISECONDS);
    }

    /**
     * 此处的消息来源非常关键{@link com.cylan.jiafeigou.n.engine.GlobalUdpDataSource}
     * emit出来 {@link JfgUdpMsg.FPingAck}
     *
     * @param ssidInDigits f_ping_ack 消息只取100ms内的第一条
     * @return
     */
    private Observable<JfgUdpMsg.FPingAck> fPingObservable(final String ssidInDigits) {
        return RxBus.getDefault().toObservable(JfgUdpMsg.FPingAck.class)
                .filter(new Func1<JfgUdpMsg.FPingAck, Boolean>() {
                    @Override
                    public Boolean call(JfgUdpMsg.FPingAck pingAck) {
                        //注意条件
                        return !TextUtils.isEmpty(pingAck.cid)
                                && pingAck.cid.endsWith(ssidInDigits);
                    }
                })
                .throttleFirst(100, TimeUnit.MILLISECONDS);
    }

    /**
     * 发送,三次
     */
    private void sendPing_FPing() {
        Observable.just(1, 2)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP,
                                UdpConstant.PORT,
                                new JfgUdpMsg.Ping().toBytes());
                        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP,
                                UdpConstant.PORT,
                                new JfgUdpMsg.FPing().toBytes());
                        AppLogger.i("send ping n fping: " + integer);
                    }
                });
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        unSubscribe(pingFlowSub);
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
