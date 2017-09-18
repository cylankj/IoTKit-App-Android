package com.cylan.jiafeigou.misc;

import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.udpMsgPack.JfgUdpMsg;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 17-9-8.
 */

public class UdpDevice {
    private static final String TAG = "UdpDevice";

    /**
     * @param fullCid 12位cid
     * @return
     */
    public static Observable<String> pingDevice(final String ip, final String fullCid) {
        rx.Observable<String> result = RxBus.getCacheInstance().toObservable(JfgUdpMsg.PingAck.class)
                .subscribeOn(Schedulers.newThread())
                .timeout(2, TimeUnit.SECONDS)
                .filter(pingAck -> TextUtils.equals(pingAck.cid, fullCid))
                .first()
                .flatMap(pingAck -> rx.Observable.just(pingAck.cid));
        Observable.just("udp")
                .subscribeOn(Schedulers.newThread())
                .subscribe(s -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(ip,
                                UdpConstant.PORT, new JfgUdpMsg.Ping().toBytes());
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(ip,
                                UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);

        return result;
    }


    public static Observable<UdpConstant.UdpDevicePortrait>
    getDevicePortrait(final String summaryCid, final short port, final String host) {
        //需要fping
        return getPingAck(summaryCid, port, host)
                .zipWith(getFPingAck(summaryCid, port, host), (pingAck, fPingAck) -> {
                    UdpConstant.UdpDevicePortrait portrait = new UdpConstant.UdpDevicePortrait();
                    portrait.mac = fPingAck.mac;
                    portrait.version = fPingAck.version;
                    portrait.uuid = fPingAck.cid;
                    portrait.net = pingAck.net;
                    return portrait;
                });
    }


    public static Observable<JfgUdpMsg.PingAck> getPingAck(final String summaryCid, final short port, final String host) {
        Observable<JfgUdpMsg.PingAck> result = RxBus.getCacheInstance()
                .toObservable(JfgUdpMsg.PingAck.class)
                .subscribeOn(Schedulers.newThread())
                .timeout(2, TimeUnit.SECONDS, Observable.just("pingFailed")
                        .map(s -> {
                            throw new RxEvent.HelperBreaker(s);
                        }))
                .filter(pingAck -> pingAck.cid.endsWith(summaryCid))
                .first();
        Observable.just("udp")
                .subscribeOn(Schedulers.newThread())
                .subscribe(s -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(host, port, new JfgUdpMsg.Ping().toBytes());
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(host, port, new JfgUdpMsg.Ping().toBytes());
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    AppLogger.d(TAG + ":ping: " + summaryCid);
                }, AppLogger::e);
        return result;
    }

    public static Observable<JfgUdpMsg.FPingAck> getFPingAck(final String summaryCid, final short port, final String host) {
        Observable<JfgUdpMsg.FPingAck> result = RxBus.getCacheInstance()
                .toObservable(JfgUdpMsg.FPingAck.class)
                .subscribeOn(Schedulers.newThread())
                .timeout(2, TimeUnit.SECONDS, Observable.just("fPingFailed")
                        .map(s -> {
                            throw new RxEvent.HelperBreaker(s);
                        }))
                .filter(fPingAck -> fPingAck.cid.endsWith(summaryCid))
                .first();
        Observable.just("udp")
                .subscribeOn(Schedulers.newThread())
                .subscribe(s -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(host, port, new JfgUdpMsg.FPing().toBytes());
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(host, port, new JfgUdpMsg.FPing().toBytes());
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    AppLogger.d(TAG + ":fping: " + summaryCid);
                }, AppLogger::e);
        return result;
    }

    public static Observable<JfgUdpMsg.DoSetWifiAck> sendWifiInfo(UdpConstant.UdpDevicePortrait portrait,
                                                                  final String ssid,
                                                                  final String pwd, final int type,
                                                                  final String host, final short port) {
//        Observable<JfgUdpMsg.DoSetWifiAck> result = RxBus.getCacheInstance()
//                .toObservable(JfgUdpMsg.DoSetWifiAck.class)
//                .subscribeOn(Schedulers.newThread())
//                .timeout(2, TimeUnit.SECONDS, Observable.just("sendWifiInfoFailed")
//                        .map(s -> {
//                            throw new RxEvent.HelperBreaker(s);
//                        }))
//                .filter(setWifiAck -> setWifiAck.cid.endsWith(portrait.uuid))
//                .first();
        return Observable.just(new JfgUdpMsg.DoSetWifiAck())
                .subscribeOn(Schedulers.newThread())
                .flatMap(s -> {
                    try {
                        JfgUdpMsg.DoSetWifi setWifi = new JfgUdpMsg.DoSetWifi(portrait.uuid, portrait.mac, ssid, pwd);
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(host, port, setWifi.toBytes());
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(host, port, setWifi.toBytes());
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(host, port, setWifi.toBytes());
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    AppLogger.d(TAG + ":sendWifiInfo: " + portrait);
                    return Observable.just(s);
                });
    }


}
