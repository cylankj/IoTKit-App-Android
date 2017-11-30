package com.cylan.jiafeigou.n.engine;

import android.text.TextUtils;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.push.BellPuller;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.Gson;

import org.msgpack.MessagePack;

import java.io.IOException;

import rx.Subscription;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-10.
 * 作为一个app生命周期的消息接受处理器
 */

public class GlobalUdpDataSource {
    private Subscription subscription;

    private GlobalUdpDataSource() {
    }

    public void register() {
        unregister();
        subscription = RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                .subscribeOn(Schedulers.immediate())
                .filter(localUdpMsg -> {
                    if (localUdpMsg == null || localUdpMsg.ip == null || localUdpMsg.data == null) {
                        AppLogger.i("err happened: " + localUdpMsg);
                        return false;
                    }
                    return true;
                })
                .map(localUdpMsg -> {
                    final long time = System.currentTimeMillis();
                    MessagePack msgPack = new MessagePack();
                    try {
                        JfgUdpMsg.UdpHeader header = msgPack.read(localUdpMsg.data, JfgUdpMsg.UdpHeader.class);
                        final String headTag = header.cmd;
                        AppLogger.i("headerTag: " + headTag);
                        if (TextUtils.equals(headTag, UdpConstant.PING_ACK)) {
                            JfgUdpMsg.PingAck pingAck = msgPack.read(localUdpMsg.data, JfgUdpMsg.PingAck.class);
                            //保存ping_ack
                            RxBus.getCacheInstance().post(pingAck);
//                                UdpConstant.udpObjectMap.put(UdpConstant.PingAckT.class, new UdpConstant.PingAckT(System.currentTimeMillis(), pingAck));
                            AppLogger.i(new Gson().toJson(pingAck));
                        } else if (TextUtils.equals(headTag, UdpConstant.F_PING_ACK)) {
                            JfgUdpMsg.FPingAck f_pingAck = msgPack.read(localUdpMsg.data, JfgUdpMsg.FPingAck.class);
                            RxBus.getCacheInstance().post(f_pingAck);
//                                UdpConstant.udpObjectMap.put(UdpConstant.PingAckT.class, new UdpConstant.FPingAckT(System.currentTimeMillis(), f_pingAck));
                            AppLogger.i(new Gson().toJson(f_pingAck));
                        } else if (TextUtils.equals(headTag, UdpConstant.DOORBELL_RING)) {
                            AppLogger.d("收到局域网呼叫");
                            JfgUdpMsg.UdpRecvHeard recvHeard = msgPack.read(localUdpMsg.data, JfgUdpMsg.UdpRecvHeard.class);
                            Device device = DataSourceManager.getInstance().getDevice(recvHeard.cid);
                            if (device != null && TextUtils.equals(device.uuid, recvHeard.cid)) {//说明当前账号有这个设备
                                AppLogger.d("当前保存的 NTP 时间为:" + PreferencesUtils.getInt(JConstant.KEY_NTP_INTERVAL));
//                                JFGDoorBellCaller caller = new JFGDoorBellCaller();
//                                caller.time = System.currentTimeMillis() / 1000L - PreferencesUtils.getInt(JConstant.KEY_NTP_INTERVAL);
//                                caller.cid = recvHeard.cid;
//                                RxEvent.BellCallEvent callEvent = new RxEvent.BellCallEvent(caller);
//                                callEvent.isFromLocal = true;
//                                RxBus.getCacheInstance().post(callEvent);
                                BellPuller.getInstance().launchBellLive(recvHeard.cid, null, System.currentTimeMillis() / 1000L);
                            }
                            AppLogger.i(new Gson().toJson(recvHeard));
                        } else if (TextUtils.equals(headTag, "do_set_wifi_ack")) {
                            RxBus.getCacheInstance().post(new RxEvent.SetWifiAck(DpUtils.unpackData(localUdpMsg.data, JfgUdpMsg.DoSetWifiAck.class)));
                        }
                    } catch (IOException e) {
                        AppLogger.i("unpack msgpack failed:" + e.getLocalizedMessage());
                    }
                    AppLogger.i("udp performance: " + (System.currentTimeMillis() - time));
                    return null;
                })
                .retry(exceptionFun)
                .subscribe(ret -> {
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }

    public void unregister() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    /**
     * 异常情况下，返回true,将继续订阅
     */
    private Func2<Integer, Throwable, Boolean> exceptionFun = new Func2<Integer, Throwable, Boolean>() {
        @Override
        public Boolean call(Integer integer, Throwable throwable) {
            //此处return true:表示继续订阅，
            AppLogger.d("GlobalUdpDataSource: " + throwable.getLocalizedMessage());
            return true;
        }
    };
}