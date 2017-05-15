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
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.Gson;

import java.io.IOException;
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
    private DeviceInformation deviceInformation = new DeviceInformation(null);

    public static BaseDeviceInformationFetcher getInstance() {
        return INFORMATION_FETCHER;
    }

    public DeviceInformation getDeviceInformation() {
        if (TextUtils.isEmpty(deviceInformation.ip)) {
            fetchDeviceInformationSuggestion();
        }
        return deviceInformation;
    }

    @Inject
    public BaseDeviceInformationFetcher(@ContextLife Context context) {
        INFORMATION_FETCHER = this;
        //这是全局的,所以不需要反注册,本来要写在 manifest 里的但 Android 7.0 写在 manifest 里失效了
        context.registerReceiver(this, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void resolveDeviceInformation(RxEvent.LocalUdpMsg udpMsg) {
        try {
            AppLogger.d("正在解析 UDP 消息:" + new Gson().toJson(udpMsg));
            JfgUdpMsg.UdpSecondaryHeard udpHeader = unpackData(udpMsg.data, JfgUdpMsg.UdpSecondaryHeard.class);
            assert udpHeader != null;
            if (!TextUtils.equals(udpHeader.cid, deviceInformation.uuid)) {//说明不是我们需要的消息
                return;
            }
            deviceInformation.mac = udpHeader.mac;
            if (TextUtils.equals(udpHeader.cmd, UdpConstant.F_PING_ACK)) {
                JfgUdpMsg.FPingAck pingAck = unpackData(udpMsg.data, JfgUdpMsg.FPingAck.class);
                assert pingAck != null;
                if (TextUtils.equals(pingAck.cid, deviceInformation.uuid)) {
                    deviceInformation.ip = udpMsg.ip;
                    deviceInformation.port = udpMsg.port;
                    AppLogger.d("当前设备的局域网 IP 地址为:http://" + udpMsg.ip);
                }
            }

        } catch (Exception e) {
            AppLogger.e("err: " + e.getMessage());
        }
    }

    private boolean resolveFinished() {
        boolean hasInformation = true;
        if (deviceInformation == null) {
            hasInformation = false;
        }
        if (deviceInformation.uuid == null) {
            hasInformation = false;
        }
        if (deviceInformation.ip == null) {
            hasInformation = false;
        }
        if (deviceInformation.port == -10000) {
            hasInformation = false;
        }
        return hasInformation;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())
                && !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
            //网络状态发生了变化,这里我们判断当前连接的是否是设备AP,如果是设备AP 则主动请求设备信息,这样就做到了全局处理逻辑.
            fetchDeviceInformationSuggestion();
        }
    }

    public void init(String uuid) {
        deviceInformation = new DeviceInformation(uuid);
        fetchDeviceInformationSuggestion();
    }

    private void fetchDeviceInformationSuggestion() {
        Observable.create(subscriber -> {
            ConnectivityManager connectivityManager = (ConnectivityManager) ContextUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && TextUtils.isEmpty(deviceInformation.ip)) {
                try {
                    AppLogger.d("fetchDeviceInformation");
                    BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                    BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                    BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                    BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                    RxBus.getCacheInstance().postSticky(RxEvent.FetchDeviceInformation.STARTED);//当前连接的是 WiFi 网络则尝试读取所有能读取的设备信息
                    subscriber.onNext("fetch");
                    subscriber.onCompleted();
                } catch (JfgException e) {
                    subscriber.onError(e);
                }
            } else {
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class))
                .filter(this::accept)
                .timeout(10, TimeUnit.SECONDS)
                .takeUntil(udpMsg -> resolveFinished())
                .subscribe(this::resolveDeviceInformation, e -> {
                    AppLogger.e(e.toString() + ":" + e.getMessage());
                    if (BaseApplication.isOnline()) {
                        RxBus.getCacheInstance().postSticky(RxEvent.FetchDeviceInformation.SUCCESS);
                    }
                }, () -> {
                    RxBus.getCacheInstance().postSticky(deviceInformation);
                    RxBus.getCacheInstance().postSticky(RxEvent.FetchDeviceInformation.SUCCESS);
                });
    }

    private boolean accept(RxEvent.LocalUdpMsg udpMsg) {
        JfgUdpMsg.UdpSecondaryHeard udpHeader = null;
        try {
            udpHeader = unpackData(udpMsg.data, JfgUdpMsg.UdpSecondaryHeard.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return udpHeader != null && TextUtils.equals(udpHeader.cid, deviceInformation.uuid);
    }
}
