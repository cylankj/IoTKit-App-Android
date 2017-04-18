package com.cylan.jiafeigou.base.module;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDoorBellCaller;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.Gson;

import org.msgpack.MessagePack;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/4/14.
 */
@Singleton
public class BaseGlobalUdpParser {

    @Inject
    public BaseGlobalUdpParser() {

    }

    public Subscription initSubscription() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                .subscribeOn(Schedulers.io())
                .retry((integer, throwable) -> true)
                .subscribe(this::parserUdpMessage, AppLogger::e);
    }

    private void parserUdpMessage(RxEvent.LocalUdpMsg localUdpMsg) {
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
                AppLogger.i(new Gson().toJson(pingAck));
            } else if (TextUtils.equals(headTag, UdpConstant.F_PING_ACK)) {
                JfgUdpMsg.FPingAck f_pingAck = msgPack.read(localUdpMsg.data, JfgUdpMsg.FPingAck.class);
                RxBus.getCacheInstance().post(f_pingAck);
//                                UdpConstant.udpObjectMap.put(UdpConstant.PingAckT.class, new UdpConstant.FPingAckT(System.currentTimeMillis(), f_pingAck));
                AppLogger.i(new Gson().toJson(f_pingAck));
            } else if (TextUtils.equals(headTag, UdpConstant.DOORBELL_RING)) {
                AppLogger.d("收到局域网呼叫");
                JfgUdpMsg.UdpRecvHeard recvHeard = msgPack.read(localUdpMsg.data, JfgUdpMsg.UdpRecvHeard.class);
                Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(recvHeard.cid);
                if (device != null && TextUtils.equals(device.uuid, recvHeard.cid)) {//说明当前账号有这个设备
                    AppLogger.d("当前保存的 NTP 时间为:" + PreferencesUtils.getInt(JConstant.KEY_NTP_INTERVAL));
                    JFGDoorBellCaller caller = new JFGDoorBellCaller();
                    caller.time = System.currentTimeMillis() / 1000L - PreferencesUtils.getInt(JConstant.KEY_NTP_INTERVAL);
                    caller.cid = recvHeard.cid;
                    RxEvent.BellCallEvent callEvent = new RxEvent.BellCallEvent(caller);
                    callEvent.isFromLocal = true;
                    RxBus.getCacheInstance().post(callEvent);
                }
                AppLogger.i(new Gson().toJson(recvHeard));
            }
        } catch (IOException e) {
            AppLogger.i("unpack msgpack failed:" + e.getLocalizedMessage());
        }
        AppLogger.i("udp performance: " + (System.currentTimeMillis() - time));
    }
}
