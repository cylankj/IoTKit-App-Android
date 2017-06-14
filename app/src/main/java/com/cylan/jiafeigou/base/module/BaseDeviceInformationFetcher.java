package com.cylan.jiafeigou.base.module;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.injector.lifecycle.ContextLife;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.dp.DpUtils.unpackData;

/**
 * Created by yanzhendong on 2017/5/11.
 */
@Singleton
public class BaseDeviceInformationFetcher extends BroadcastReceiver {
    public static BaseDeviceInformationFetcher INFORMATION_FETCHER;
    private volatile DeviceInformation deviceInformation;
    private volatile boolean isFetching = false;

    public static BaseDeviceInformationFetcher getInstance() {
        return INFORMATION_FETCHER;
    }

    public DeviceInformation getDeviceInformation() {
        return deviceInformation;
    }

    @Inject
    public BaseDeviceInformationFetcher(@ContextLife Context context) {
        INFORMATION_FETCHER = this;
        //这是全局的,所以不需要反注册,本来要写在 manifest 里的但 Android 7.0 写在 manifest 里失效了
        context.registerReceiver(this, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        monitorDeviceInformationSuggestion();
    }

    private boolean resolveDeviceInformation(RxEvent.LocalUdpMsg udpMsg) {
        try {
            AppLogger.d("正在解析 UDP 消息:" + new Gson().toJson(udpMsg));
            JfgUdpMsg.UdpSecondaryHeard udpHeader = unpackData(udpMsg.data, JfgUdpMsg.UdpSecondaryHeard.class);
            if (udpHeader == null) return false;
            if (!TextUtils.equals(udpHeader.cid, deviceInformation.uuid)) {//说明不是我们需要的消息
                return false;
            }
            deviceInformation.mac = udpHeader.mac;
            if (TextUtils.equals(udpHeader.cmd, UdpConstant.F_PING_ACK)) {
                JfgUdpMsg.FPingAck pingAck = unpackData(udpMsg.data, JfgUdpMsg.FPingAck.class);

                if (pingAck != null && TextUtils.equals(pingAck.cid, deviceInformation.uuid)) {
                    deviceInformation.ip = udpMsg.ip;
                    deviceInformation.port = udpMsg.port;
                    AppLogger.d("当前设备的局域网 IP 地址为:http://" + udpMsg.ip);
                    return true;
                }
            }

        } catch (Exception e) {
            AppLogger.e("err: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            //网络状态发生了变化,这里我们判断当前连接的是否是设备AP,如果是设备AP 则主动请求设备信息,这样就做到了全局处理逻辑.
            AppLogger.d("网络状态发生了变化");
            if (!isFetching) {
                RxBus.getCacheInstance().postSticky(RxEvent.FetchDeviceInformation.STARTED);
            }
        }
        //改变背景或者 处理网络的全局变量
        Schedulers.immediate().createWorker().schedule(() -> {
            ConnectivityManager connectivityManager = (ConnectivityManager) BaseApplication.getAppComponent().getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            boolean available = (mobNetInfo != null && mobNetInfo.isConnected()) || (wifiNetInfo != null && wifiNetInfo.isConnected());
            RxEvent.NetConnectionEvent connectionEvent = new RxEvent.NetConnectionEvent(available);
            connectionEvent.mobile = mobNetInfo;
            connectionEvent.wifi = wifiNetInfo;
            connectionEvent.isOnLine = BaseApplication.isOnline();
            boolean publicNetwork = NetUtils.isPublicNetwork();
            BaseApplication.getAppComponent().getSourceManager().setOnline(publicNetwork);
            RxBus.getCacheInstance().postSticky(connectionEvent);
        });

    }

    public void init(String uuid) {
        deviceInformation = new DeviceInformation(uuid);
        RxBus.getCacheInstance().postSticky(RxEvent.FetchDeviceInformation.STARTED);
    }

    public void sleep() {
        deviceInformation = null;
    }


    private void monitorDeviceInformationSuggestion() {
        RxBus.getCacheInstance().toObservable(RxEvent.FetchDeviceInformation.class)
                .observeOn(Schedulers.io())
                .filter(event -> !isFetching && !event.success && deviceInformation != null)
                .map(event -> {
                    deviceInformation.ip = null;
                    deviceInformation.port = 0;
                    isFetching = true;
                    ConnectivityManager connectivityManager = (ConnectivityManager) ContextUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        try {
                            AppLogger.d("fetchDeviceInformation");
                            BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                            BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                            BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                            BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                        } catch (JfgException e) {
                            AppLogger.e(e.getMessage());
                        }
                        return true;
                    } else {
                        isFetching = false;
                        RxBus.getCacheInstance().postSticky(RxEvent.FetchDeviceInformation.SUCCESS);
                        return false;
                    }
                })
                .filter(send -> send)
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                        .first(this::resolveDeviceInformation)
                        .timeout(5, TimeUnit.SECONDS, Observable.just(null)))
                .retry()
                .subscribe(ret -> {
                    isFetching = false;
                    RxBus.getCacheInstance().postSticky(RxEvent.FetchDeviceInformation.SUCCESS);
                }, e -> {
                    isFetching = false;
                    AppLogger.e(e.toString() + ":" + e.getMessage());
                    Schedulers.io().createWorker().schedule(() -> {
                        if (NetUtils.isPublicNetwork()) {
                            RxBus.getCacheInstance().postSticky(RxEvent.FetchDeviceInformation.SUCCESS);
                        }
                    });
                });
    }
}
