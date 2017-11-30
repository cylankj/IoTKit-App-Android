package com.cylan.jiafeigou.base.module;

import android.util.Log;

import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.push.BellPuller;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.Gson;

import org.msgpack.MessagePack;
import org.msgpack.type.Value;

import java.io.IOException;

import static com.cylan.jiafeigou.dp.DpUtils.unpackData;

/**
 * Created by yanzhendong on 2017/4/14.
 */
public class BaseUdpMsgParser {
    private static BaseUdpMsgParser instance;

    public static BaseUdpMsgParser getInstance() {
        if (instance == null) {
            synchronized (BaseUdpMsgParser.class) {
                if (instance == null) {
                    instance = new BaseUdpMsgParser();
                }
            }
        }
        return instance;
    }

    public BaseUdpMsgParser() {
    }


    public static final String TAG = "LOCAL_UDP:";

    public void parserUdpMessage(RxEvent.LocalUdpMsg localUdpMsg) {
        JfgUdpMsg.UdpRecvHeard header = null;
        try {
            header = unpackData(localUdpMsg.data, JfgUdpMsg.UdpRecvHeard.class);
            if (header == null) {
                return;
            }
            MessagePack pack = new MessagePack();
            Value read = pack.read(localUdpMsg.data);
            Log.i(TAG, read.toString());
            switch (header.cmd) {
                case UdpConstant.PING_ACK: {
                    JfgUdpMsg.PingAck pingAck = unpackData(localUdpMsg.data, JfgUdpMsg.PingAck.class);
                    RxBus.getCacheInstance().post(pingAck);
                    break;
                }
                case UdpConstant.F_PING_ACK: {
                    JfgUdpMsg.FPingAck f_pingAck = unpackData(localUdpMsg.data, JfgUdpMsg.FPingAck.class);
                    RxBus.getCacheInstance().post(f_pingAck);
                    break;
                }
                case UdpConstant.DOORBELL_RING: {
                    Device device = DataSourceManager.getInstance().getDevice(header.cid);
                    if (device != null && device.available()) {//说明当前账号有这个设备
//                        JFGDoorBellCaller caller = new JFGDoorBellCaller();
//                        caller.cid = header.cid;
//                        RxEvent.BellCallEvent callEvent = new RxEvent.BellCallEvent(caller);
//                        callEvent.isFromLocal = true;
//                        RxBus.getCacheInstance().post(callEvent);
                        BellPuller.getInstance().launchBellLive(header.cid, null, System.currentTimeMillis() / 1000);
                    }
                    break;
                }
                case UdpConstant.REPORT_MSG: {
                    PanoramaEvent.ReportMsg reportMsg = unpackData(localUdpMsg.data, PanoramaEvent.ReportMsg.class);
                    if (reportMsg == null) {
                        return;
                    }
                    PanoramaEvent.MsgForward msgForward = unpackData(reportMsg.bytes, PanoramaEvent.MsgForward.class);
                    if (msgForward != null) {
                        BaseForwardHelper.getInstance().dispatcherForward(msgForward);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Log.i(TAG, "解析局域网消息失败了:" + new Gson().toJson(localUdpMsg));
                MessagePack pack = new MessagePack();
                Value read = pack.read(localUdpMsg.data);
                Log.i(TAG, "message:" + read.toString());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
