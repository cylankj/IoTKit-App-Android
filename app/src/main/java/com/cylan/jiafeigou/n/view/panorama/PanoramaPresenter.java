package com.cylan.jiafeigou.n.view.panorama;

import android.net.wifi.WifiInfo;
import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.PanoramaEvent;
import com.cylan.jiafeigou.base.wrapper.BaseViewablePresenter;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.RandomUtils;
import com.cylan.socket.JFGSocket;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.base.module.PanoramaEvent.MIDRobotForwardDataV2;
import static com.cylan.jiafeigou.base.module.PanoramaEvent.TYPE_TAKE_PICTURE_REQ;
import static com.cylan.jiafeigou.base.module.PanoramaEvent.TYPE_VIDEO_BEGIN_REQ;
import static com.cylan.jiafeigou.base.module.PanoramaEvent.TYPE_VIDEO_END_REQ;
import static com.cylan.jiafeigou.base.module.PanoramaEvent.TYPE_VIDEO_STATUS_REQ;
import static com.cylan.jiafeigou.dp.DpUtils.pack;

/**
 * Created by yanzhendong on 2017/3/8.
 */

public class PanoramaPresenter extends BaseViewablePresenter<PanoramaCameraContact.View> implements PanoramaCameraContact.Presenter, JFGSocket.JFGSocketCallBack {
    private boolean localUDPConnected = false;
    private boolean remoteServerConnected = false;
    private long socketHandler = -1;

    @Override
    public void onStart() {
        super.onStart();
        Device device = mSourceManager.getJFGDevice(mUUID);
        if (device != null) {
            mView.onShowProperty(device);
        }
//        checkAndInitConnection();
    }

    protected void checkAndInitConnection() {
        Observable.just(NetUtils.ping())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(remote -> {
                    remoteServerConnected = remote;
                    return remote;
                })
                .filter(ret -> NetUtils.isWiFiConnected(mView.getAppContext()));//没有连接 WiFi 就没必要初始化 local udp连接了

    }

    protected Observable getLocalConnectionSub() {
        return null;
    }

    protected Observable getRemoteConnectionSub() {
        return Observable.just(NetUtils.ping())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .filter(connect -> remoteServerConnected = connect);
    }

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getNetWorkChangedSub());
        registerSubscription(makeTCPBridge());
    }

    private Observable<Boolean> checkConnection() {
        return Observable.just("checkConnection")
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    final WifiInfo info = NetUtils.getWifiManager(ContextUtils.getContext()).getConnectionInfo();
                    if (info == null || !JFGRules.isCylanDevice(info.getSSID())) {
                        AppLogger.i("checkConnection: " + info);
                        return false;
                    }
                    return true;
                });
    }

    private Subscription makeTCPBridge() {
        return checkConnection()
                .filter(aBoolean -> aBoolean)
                .map(s -> {
                    try {
                        if (socketHandler == -1) {
                            socketHandler = JFGSocket.InitSocket(this);
                        }
                        AppLogger.d("正在发送 FPing 消息");
                        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.d("连接 socket 出现错误");
                    }
                    return null;
                })
                .zipWith(RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .map(new Func1<RxEvent.LocalUdpMsg, Boolean>() {
                            @Override
                            public Boolean call(RxEvent.LocalUdpMsg msg) {
                                try {
                                    JfgUdpMsg.UdpHeader header = DpUtils.unpackData(msg.data, JfgUdpMsg.UdpHeader.class);
                                    AppLogger.d("header: " + new Gson().toJson(header));
                                    if (header == null || !TextUtils.equals(header.cmd, "f_ping_ack"))
                                        return false;
                                    JfgUdpMsg.FPingAck pingAck = DpUtils.unpackData(msg.data, JfgUdpMsg.FPingAck.class);
                                    AppLogger.d("pingAck: " + new Gson().toJson(pingAck));
                                    if (pingAck == null || !TextUtils.equals(pingAck.cid, mUUID))
                                        return false;
                                    //得到 fping响应
                                    synchronized (this) {
                                        if (!localUDPConnected) {
                                            localUDPConnected = true;
                                            AppLogger.d("获取到设备 IP 地址:" + msg.ip + ",port:" + msg.port);
                                            JFGSocket.Connect(socketHandler, msg.ip, msg.port, true);
                                            return true;
                                        }
                                    }
                                } catch (Exception e) {
                                    AppLogger.e("err: " + e.getLocalizedMessage());
                                    return false;
                                }
                                return false;
                            }
                        }), (Object o, Boolean aBoolean) -> aBoolean)
                .filter(aBoolean -> aBoolean)
                .doOnError(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        AppLogger.e("f_ping timeout :");
                    }
                })
                .subscribe(o -> AppLogger.d("good"),
                        throwable -> AppLogger.e("f_ping err :" + throwable.getLocalizedMessage()),
                        () -> AppLogger.d("make tcp finished"));
    }

    protected Observable<RxEvent.PanoramaConnection> getActiveConnection() {
        return RxBus.getCacheInstance().toObservable(RxEvent.PanoramaConnection.class)
                .mergeWith(Observable.just(localUDPConnected)
                        .observeOn(Schedulers.io())
                        .filter(connect -> connect)
                        .map(connected -> new RxEvent.PanoramaConnection()))
                .first();
    }

    private Subscription getNetWorkChangedSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.NetConnectionEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event.mobile != null && event.mobile.isConnected()) {
                        //移动网络,提醒用户注意流量
                        mView.onNetWorkChangedToMobile();
                    } else if (event.wifi != null && event.wifi.isConnected()) {
                        //wifi 网络,关闭流量提醒
                        mView.onNetWorkChangedToWiFi();
                    }
                });
    }

    @Override
    public void makePhotograph() {
        Subscription subscribe = checkSDCard(mSourceManager.getJFGDevice(mUUID))
                .observeOn(Schedulers.io())
                .map(dev -> fillRawMsg(new PanoramaEvent.RawReqMsg(), MIDRobotForwardDataV2, TYPE_TAKE_PICTURE_REQ, pack(new PanoramaEvent.MSG_TYPE_TAKE_PICTURE_REQ())))
                .flatMap(this::sendAndReceiveRawMsg)
                .timeout(15, TimeUnit.SECONDS, Observable.just(null))//15秒的超时时间
                .map(rsp -> {
                    if (rsp == null)
                        return null;//为 null 说明获取响应超时了,这时不应该一直等下去了
                    PanoramaEvent.MSG_TYPE_TAKE_PICTURE_RSP data = null;
                    try {
                        data = DpUtils.unpackData(rsp.msg, PanoramaEvent.MSG_TYPE_TAKE_PICTURE_RSP.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return data;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(rsp -> {
                    if (rsp == null || rsp.ret == -1) {
                        //失败了
                        AppLogger.d("拍照失败了!");
                        if (mView != null) {
                            mView.onMakePhotoGraphFailed();
                        }
                        return false;
                    }
                    return true;
                })
                .subscribe(rsp -> {
                    AppLogger.d("拍照成功了" + new Gson().toJson(rsp));
                    if (mView != null) {
                        mView.onMakePhotoGraphPreview();
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                    if (mView != null) {
                        mView.onMakePhotoGraphFailed();
                    }
                });
        registerSubscription(subscribe);
    }

    protected Observable<PanoramaEvent.RawRspMsg> sendAndReceiveRawMsg(PanoramaEvent.RawReqMsg msg) {
        return Observable.just(RandomUtils.getRandom(Integer.MAX_VALUE))
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(PanoramaEvent.RawRspMsg.class)
                        .mergeWith(Observable.just("sendAndReceiveRawMsg")
                                .map(go -> {
                                    msg.mSeq = seq;
                                    JFGSocket.SendMsgpackBuff(socketHandler, pack(msg));
                                    AppLogger.d("正在发送局域网消息:" + new Gson().toJson(msg));
                                    return msg.mSeq;
                                })
                                .flatMap(ret -> RxBus.getCacheInstance().toObservable(PanoramaEvent.RawRspMsg.class)))
                        .filter(rsp -> rsp.mSeq == seq)
                        .first());
    }

    protected RecordProgress calculate(long count) {//这里计算修正量,因为可能之前已经在录了
        return new RecordProgress((count + 1) * 500);
    }

    @Override
    public void startMakeLongVideo() {
        Subscription subscribe = checkSDCard(mSourceManager.getJFGDevice(mUUID))
                .observeOn(Schedulers.io())
                .flatMap(this::checkBattery)
                .observeOn(Schedulers.io())
                .flatMap(ret -> startMakeVideo(2))
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(ret -> makeVideoProgressUpdate(0, 2))
                .subscribe(second -> {
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                    if (mView != null) {
                        mView.onStartLongVideoFailed();
                    }
                });
        registerSubscription(subscribe);
    }

    @Override
    public void stopMakeLongVideo() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RecordProgress.class)
                .first()
                .map(sec -> {
                    RxBus.getCacheInstance().post(new RecordFinished());
                    return sec;
                })
                .flatMap(progress -> stopMakeVideo(2)
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(rsp -> {
                            if (rsp == null || rsp.ret == -1) {
                                AppLogger.d("结束录制长视频失败");
                                mView.onStopMakeVideoFailed();
                            } else {
                                AppLogger.d("结束录制长视频成功");
                                mView.onLongVideoCompleted();
                            }
                            return rsp;
                        })
                )
                .subscribe(ret -> {
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                    if (mView != null) {
                        mView.onStopMakeVideoFailed();
                    }
                });
        registerSubscription(subscribe);
    }

    protected Observable<Integer> startMakeVideo(int type) {
        return getActiveConnection()
                .observeOn(Schedulers.io())
                .map(connection -> fillRawMsg(new PanoramaEvent.RawReqMsg(), MIDRobotForwardDataV2, TYPE_VIDEO_BEGIN_REQ, pack(type)))
                .flatMap(this::sendAndReceiveRawMsg)
                .first()
                .timeout(15, TimeUnit.SECONDS, Observable.just(null))
                .map(rsp -> {
                    if (rsp == null) return null;
                    Integer data = null;
                    try {
                        data = DpUtils.unpackData(rsp.msg, Integer.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return data;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(rsp -> {
                    if (rsp == null || rsp == -1) {
                        //开始录像失败了
                        AppLogger.d("开始录像失败了!!");
                        if (type == 1) {
                            mView.onStartShortVideoFailed();
                        } else if (type == 2) {
                            mView.onStartLongVideoFailed();
                        }
                        return false;
                    }
                    return true;
                });
    }

    protected Observable<PanoramaEvent.MSG_TYPE_VIDEO_END_RSP> stopMakeVideo(int type) {
        return getActiveConnection()
                .observeOn(Schedulers.io())
                .map(connection -> fillRawMsg(new PanoramaEvent.RawReqMsg(), MIDRobotForwardDataV2, TYPE_VIDEO_END_REQ, pack(type)))
                .flatMap(this::sendAndReceiveRawMsg)
//                .timeout(30, TimeUnit.SECONDS, Observable.just(null))//因为结束录制视频等待时间会很长,所有不用 timeout
                .map(rsp -> {
                    if (rsp == null) return null;//说明请求数据超时了
                    PanoramaEvent.MSG_TYPE_VIDEO_END_RSP data = null;
                    try {
                        data = DpUtils.unpackData(rsp.msg, PanoramaEvent.MSG_TYPE_VIDEO_END_RSP.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return data;
                });
    }

    @Override
    public void startMakeShortVideo() {
        Subscription subscribe = checkSDCard(mSourceManager.getJFGDevice(mUUID))
                .observeOn(Schedulers.io())
                .flatMap(ret -> startMakeVideo(1))
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(ret -> makeVideoProgressUpdate(0, 1))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sec -> {
                    if (sec >= 8) {//大于八秒设备自动停止录像,不需要我们再去发送消息,我们只需要更新界面就行了
                        AppLogger.d("录制完成了");
                        mView.onShortVideoCompleted();
                        RxBus.getCacheInstance().post(new RecordFinished());
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                });
        registerSubscription(subscribe);
    }

    @Override
    public void stopMakeShortVideo() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RecordProgress.class)
                .first()
                .map(sec -> {
                    RxBus.getCacheInstance().post(new RecordFinished());
                    return sec;
                })
                .filter(progress -> progress.second < 8)//超过8秒会在 startMakeShortVideo中完成
                .flatMap(progress -> stopMakeVideo(1)
                        .observeOn(AndroidSchedulers.mainThread())
                        .filter(rsp -> {
                                    if (progress.second < 3) {
                                        mView.onShortVideoCanceled(-1);
                                        return false;
                                    }
                                    return true;
                                }
                        ))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    AppLogger.d("结束录制短视频结果为:" + new Gson().toJson(rsp));
                    if (rsp == null || rsp.ret == -1) {
                        mView.onStopMakeVideoFailed();
                    } else {
                        mView.onShortVideoCompleted();
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                });
        registerSubscription(subscribe);
    }


    @Override
    public void checkAndInitRecord() {
        Subscription subscribe = getActiveConnection()
                .observeOn(Schedulers.io())
                .map(connection -> fillRawMsg(new PanoramaEvent.RawReqMsg(), MIDRobotForwardDataV2, TYPE_VIDEO_STATUS_REQ, pack(new PanoramaEvent.MSG_TYPE_VIDEO_STATUS_REQ())))
                .observeOn(Schedulers.io())
                .flatMap(this::sendAndReceiveRawMsg)
                .first()
                .map(rsp -> {
                    PanoramaEvent.MSG_TYPE_VIDEO_STATUS_RSP data = null;
                    try {
                        data = DpUtils.unpackData(rsp.msg, PanoramaEvent.MSG_TYPE_VIDEO_STATUS_RSP.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return data;
                })
                .filter(rsp -> rsp != null && rsp.ret != -1)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(rsp -> makeVideoProgressUpdate(rsp.secends, rsp.videoType))
                .subscribe(rsp -> {
                        },
                        e -> {
                            AppLogger.e(e.getMessage());
                            e.printStackTrace();
                        }
                );
        registerSubscription(subscribe);
    }

    public Observable<Integer> makeVideoProgressUpdate(int offset, int type) {
        return Observable.create(subscriber -> {
            if (offset == 0 && type == 1) {//开始录制8秒短视频
                mView.onShortVideoStarted();
            } else if (offset == 0 && type == 2) {//开始录制长视频
                mView.onLongVideoStarted();
            } else if (offset > 0 && type == 1) {//继续上次的录制8秒短视频
                mView.onShortVideoStarted();
                mView.onUpdateRecordTime(offset, type);
            } else if (offset > 0 && type == 2) {//继续上次的录制长视频
                mView.onLongVideoStarted();
                mView.onUpdateRecordTime(offset, type);
            }
            subscriber.onNext("VideoProgressStart");
            subscriber.onCompleted();
        }).subscribeOn(AndroidSchedulers.mainThread())
                .flatMap(ret -> Observable.interval(0, 500, TimeUnit.MILLISECONDS))
                .takeUntil(RxBus.getCacheInstance().toObservable(RecordFinished.class).first())
                .observeOn(Schedulers.io())
                .map(time -> {
                    int second = (int) (time / 2) + offset;
                    RxBus.getCacheInstance().post(new RecordProgress(second));
                    return second;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(() -> mView.onUpdateRecordTime(-1, -1))
                .map(sec -> {
                    mView.onUpdateRecordTime(sec, type);
                    return sec;
                });
    }

    protected PanoramaEvent.RawReqMsg fillRawMsg(PanoramaEvent.RawReqMsg rawReqMsg, int msgId, int type, byte[] msg) {
        rawReqMsg.dst = Collections.singletonList(mUUID);
        rawReqMsg.mCaller = "";
        rawReqMsg.mCallee = "";
        rawReqMsg.mId = msgId;
        rawReqMsg.isAck = 1;
        rawReqMsg.type = type;
        rawReqMsg.msg = msg;
        return rawReqMsg;
    }

    private Observable<Device> checkSDCard(Device device) {
        return Observable.just(device)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(dev -> {
                    DpMsgDefine.DPSdStatus status = dev.$(DpMsgMap.ID_204_SDCARD_STORAGE, null);
                    if (status == null || !status.hasSdcard) {
                        //没有存储设备
//                        mView.onSDCardUnMounted();
                        return true;
                    }
                    return true;
                })
                .filter(dev -> {
                    DpMsgDefine.DPSdStatus status = dev.$(DpMsgMap.ID_204_SDCARD_STORAGE, null);
                    if (status != null && status.total - status.used < 1000) {
                        //存储设备内存不足
//                        mView.onSDCardMemoryFull();
                        return true;
                    }
                    return true;
                });
    }

    private Observable<Device> checkBattery(Device device) {
        return Observable.just(device)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(dev -> {
                    if (dev.$(DpMsgMap.ID_206_BATTERY, 0) < 5) {
                        //电量低于5%
//                        mView.onDeviceBatteryLow();
//                        return false;
                    }
                    return true;
                });
    }


    @Override
    public void OnConnected() {
        AppLogger.d("OnConnected");
        localUDPConnected = true;
        RxBus.getCacheInstance().post(new RxEvent.PanoramaConnection());
    }

    @Override
    public void OnDisconnected() {
        AppLogger.d("OnDisconnected");
        localUDPConnected = false;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (socketHandler != -1) {
            JFGSocket.Disconnect(socketHandler);
            JFGSocket.Release(socketHandler);
            socketHandler = -1;
        }
    }

    @Override
    public void OnMsgpackBuff(byte[] bytes) {
        try {
            PanoramaEvent.RawRspMsg rawRspMsg = DpUtils.unpackData(bytes, PanoramaEvent.RawRspMsg.class);
            RxBus.getCacheInstance().post(rawRspMsg);
            AppLogger.d("收到局域网数据:" + new Gson().toJson(rawRspMsg));
        } catch (IOException e) {
            e.printStackTrace();
            AppLogger.e("解析局域网消息失败!!!");
        }
    }
}
