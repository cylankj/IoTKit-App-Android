package com.cylan.jiafeigou.misc.ver;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;

import org.msgpack.MessagePack;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.cylan.jiafeigou.misc.bind.UdpConstant.F_ACK;
import static com.cylan.jiafeigou.misc.bind.UdpConstant.F_PING_ACK;

/**
 * Created by hds on 17-6-3.
 */

public class NormalFUUpdate extends BaseFUUpdate {

    private String fileName;

    public NormalFUUpdate(String uuid, String fileName) {
        this.uuid = uuid;
        this.fileName = fileName;
        if (TextUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
    }

    /**
     * {@link JConstant.U#FAILED_90S}
     * {@link JConstant.U#FAILED_120S}
     * {@link JConstant.U#IDLE}
     * {@link JConstant.U#UPDATING}
     * {@link JConstant.U#SUCCESS}
     */
    private int updateState;

    @Override
    public int getUpdateState() {
        return updateState;
    }

    @Override
    public int getSimulatePercent() {
        if (simulatePercent == null) {
            return 0;
        }
        return simulatePercent.getProgress();
    }

    @Override
    public void setListener(FUpgradingListener listener) {
        this.listener = listener;
    }

    @Override
    public void call(String s) {
        prepareNetMonitor();
        //1.发送一个fping,等待fpingRsp,从中读取ip,port.
        RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                .subscribeOn(Schedulers.io())
                .timeout(10, TimeUnit.SECONDS)//设备无响应
                .flatMap(localUdpMsg -> {
                    MessagePack msgPack = new MessagePack();
                    try {
                        JfgUdpMsg.UdpHeader header = msgPack.read(localUdpMsg.data, JfgUdpMsg.UdpHeader.class);
                        Log.d(TAG, "cmd:" + header.cmd + ",");
                        if (TextUtils.equals(F_PING_ACK, header.cmd)) {
                            //得到fping结果
                            JfgUdpMsg.UdpRecvHeard recvHeard = msgPack.read(localUdpMsg.data, JfgUdpMsg.UdpRecvHeard.class);
                            if (TextUtils.equals(recvHeard.cid, uuid)) {
                                throw new RxEvent.HelperBreaker().setValue(localUdpMsg);
                            } else {
                                Log.d(TAG, "不是同一个设备:" + uuid + ",cid:" + recvHeard.cid);
                            }
                        }
                        return Observable.just(null);
                    } catch (IOException e) {
                        return Observable.just(null);
                    }
                })
                .filter(ret -> ret != null)
                .subscribe(ret -> AppLogger.d("got your rsp : " + uuid + " "),
                        //err发生,整个订阅链就结束
                        throwable -> {
                            if (throwable instanceof RxEvent.HelperBreaker) {
                                AppLogger.d("got your rsp : " + uuid + " " + ((RxEvent.HelperBreaker) throwable).localUdpMsg);
                                prepareSending(((RxEvent.HelperBreaker) throwable).localUdpMsg.ip, ((RxEvent.HelperBreaker) throwable).localUdpMsg.port);
                            } else if (throwable instanceof TimeoutException) {
                                updateState = JConstant.U.FAILED_FPING_ERR;
                                handleTimeout(JConstant.U.FAILED_FPING_ERR);
                            }
                        });
        try {
            Command.getInstance().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.Ping().toBytes());
            Command.getInstance().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
            AppLogger.d("send fping :" + UdpConstant.IP);
        } catch (JfgException e) {
            e.printStackTrace();
        }
        makeSimulatePercent();
    }

    private void prepareSending(String remoteIp, int port) {
        String localIp = NetUtils.getReadableIp();
        //需要说明,http_server映射的路径是 /data/data/com.cylan.jiafeigou/files/.200000000086
        String localUrl = "http://" + localIp + ":8765/" + fileName;
        AppLogger.d("ip:" + localIp + ",localUrl" + localUrl);
        if (listener != null) {
            listener.upgradeStart();
        }
        resetRspRecv(true);
        makeUpdateRspRecv(90);//90s
        makeUpdateRspRecv(120);//120s
        try {
            Command.getInstance().sendLocalMessage(remoteIp, (short) port, new UdpConstant.UdpFirmwareUpdate(localUrl, uuid, remoteIp, 8765).toBytes());
            Command.getInstance().sendLocalMessage(remoteIp, (short) port, new UdpConstant.UdpFirmwareUpdate(localUrl, uuid, remoteIp, 8765).toBytes());
        } catch (JfgException e) {
            AppLogger.e("发送升级包失败?" + MiscUtils.getErr(e));
        }
        //1.ping 设备,得到pingAck
    }

    private void makeSimulatePercent() {
        AppLogger.d("开始模拟升级进度");
        if (simulatePercent == null) {
            simulatePercent = new SimulatePercent();
            simulatePercent.setOnAction(this);
        }
        simulatePercent.stop();
        simulatePercent.start();
    }


    private void resetRspRecv(boolean needInit) {
        compositeSubscription.unsubscribe();
        if (needInit) {
            compositeSubscription = new CompositeSubscription();
        }
    }

    private void addSub(Subscription subscription) {
        if (compositeSubscription != null) {
            compositeSubscription.add(subscription);
        }
    }

    /**
     * @param timeout :超时
     */
    private void makeUpdateRspRecv(int timeout) {
        Subscription subscription = RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                .subscribeOn(Schedulers.io())
                .timeout(timeout, TimeUnit.SECONDS)//设备无响应
                .filter(ret -> !TextUtils.isEmpty(ret.ip) && ret.port != 0)
                .flatMap(localUdpMsg -> {
                    MessagePack msgPack = new MessagePack();
                    try {
                        JfgUdpMsg.UdpHeader header = msgPack.read(localUdpMsg.data, JfgUdpMsg.UdpHeader.class);
                        if (TextUtils.equals(F_ACK, header.cmd)) {
                            //得到fping结果
                            JfgUdpMsg.UdpRecvHeard recvHeard = msgPack.read(localUdpMsg.data, JfgUdpMsg.UdpRecvHeard.class);
                            if (TextUtils.equals(recvHeard.cid, uuid)) {
                                throw new RxEvent.HelperBreaker().setValue(localUdpMsg);
                            } else {
                                Log.d(TAG, "不是同一个设备:" + uuid + ",cid:" + recvHeard.cid);
                            }
                        }
                        return Observable.just(null);
                    } catch (IOException e) {
                        return Observable.just(null);
                    }
                })
                .filter(ret -> ret != null)
                .subscribe(ret -> {
                }, throwable -> {
                    if (throwable instanceof RxEvent.HelperBreaker) {
                        //此处发生之后,表明整个订阅链结束了,不会再发生60s超时的回调
                        handleResult(uuid, timeout, ((RxEvent.HelperBreaker) throwable).localUdpMsg.data);
                        Log.d(TAG, "Client: " + ((RxEvent.HelperBreaker) throwable).localUdpMsg);
                    } else if (throwable instanceof TimeoutException) {
                        int err = timeout == 90 ? JConstant.U.FAILED_90S : JConstant.U.FAILED_120S;
                        updateState = err;
                        handleTimeout(err);
                    }
                    resetRspRecv(false);
                });
        addSub(subscription);
    }

    private void handleTimeout(int code) {
        if (listener != null) {
            listener.upgradeErr(code);
        }
        if (simulatePercent != null) {
            simulatePercent.stop();
        }
        AppLogger.d("fping timeout : " + uuid + ",code:" + code + " " + listener);
    }

    private void handleResult(String uuid, int tag, byte[] data) {
        try {
            UdpConstant.FAck fAck = DpUtils.unpackData(data, UdpConstant.FAck.class);
            if (fAck != null && fAck.ret != 0) {
                this.updateState = JConstant.U.FAILED_DEVICE_FAILED;
                if (listener != null) {
                    listener.upgradeErr(this.updateState);
                }
                if (simulatePercent != null) {
                    simulatePercent.stop();
                }
            } else if (fAck != null) {//相应,成功了.
                this.updateState = JConstant.U.SUCCESS;
                if (simulatePercent != null) {
                    simulatePercent.boost();
                }
                AppLogger.d("升级成功,清空配置:" + uuid);
                PreferencesUtils.remove(JConstant.KEY_FIRMWARE_CONTENT + uuid);
            }
            AppLogger.d(String.format(Locale.getDefault(), "got %s firmware rsp :,uuid,%s,data:%s ", tag, uuid, fAck));
        } catch (IOException e) {
            AppLogger.e("err:" + MiscUtils.getErr(e));
        }

    }

    @Override
    public void actionDone() {
        updateState = JConstant.U.SUCCESS;
        if (listener != null) {
            listener.upgradeSuccess();
        }

    }

    @Override
    public void actionPercent(int percent) {
        updateState = JConstant.U.UPDATING;
        if (listener != null) {
            listener.upgradeProgress(percent);
        }
    }

    private void prepareNetMonitor() {
        AppLogger.d("注册网络 广播");
    }
}
