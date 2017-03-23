package com.cylan.jiafeigou.n.engine;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDoorBellCaller;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.Gson;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.IOException;

import rx.Subscription;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-10.
 * 作为一个app生命周期的消息接受处理器
 */

public class GlobalUdpDataSource {
    private Subscription subscription;
    public static GlobalUdpDataSource instance;

    public static GlobalUdpDataSource getInstance() {
        if (instance == null)
            instance = new GlobalUdpDataSource();
        return instance;
    }

    private GlobalUdpDataSource() {
    }

    @Message
    public static class BellRing {
        @Index(1)
        public String cid;
    }

    public void register() {
        unregister();
        subscription = RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                .subscribeOn(Schedulers.immediate())
                .filter(new Func1<RxEvent.LocalUdpMsg, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.LocalUdpMsg localUdpMsg) {
                        if (localUdpMsg == null
                                || localUdpMsg.ip == null
                                || localUdpMsg.data == null) {
                            AppLogger.i("err happened: " + localUdpMsg);
                            return false;
                        }
                        return true;
                    }
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
                            if (DataSourceManager.getInstance().getJFGDevice(recvHeard.cid) != null) {//说明当前账号有这个设备
                                AppLogger.d("当前保存的 NTP 时间为:" + PreferencesUtils.getInt(JConstant.KEY_NTP_INTERVAL));
                                JFGDoorBellCaller caller = new JFGDoorBellCaller();
                                caller.time = System.currentTimeMillis() / 1000L;
                                caller.cid = recvHeard.cid;
                                RxBus.getCacheInstance().post(new RxEvent.BellCallEvent(caller));
                            }
                            AppLogger.i(new Gson().toJson(recvHeard));
                        }
                    } catch (IOException e) {
                        AppLogger.i("unpack msgpack failed:" + e.getLocalizedMessage());
                    }
                    AppLogger.i("udp performance: " + (System.currentTimeMillis() - time));
                    return null;
                })
                .retry(exceptionFun)
                .subscribe();
    }

    public void unregister() {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }

    /**
     * 异常情况下，返回true,将继续订阅
     */
    private Func2<Integer, Throwable, Boolean> exceptionFun = new Func2<Integer, Throwable, Boolean>() {
        @Override
        public Boolean call(Integer integer, Throwable throwable) {
            //此处return true:表示继续订阅，
            AppLogger.e("GlobalUdpDataSource: " + throwable.getLocalizedMessage());
            return true;
        }
    };
}