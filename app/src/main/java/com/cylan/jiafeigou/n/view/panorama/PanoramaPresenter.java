package com.cylan.jiafeigou.n.view.panorama;

import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.JFGCameraDevice;
import com.cylan.jiafeigou.base.module.PanoramaEvent;
import com.cylan.jiafeigou.base.wrapper.BaseViewablePresenter;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.RandomUtils;
import com.cylan.socket.JfgSocket;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/8.
 */

public class PanoramaPresenter extends BaseViewablePresenter<PanoramaCameraContact.View> implements PanoramaCameraContact.Presenter, JfgSocket.JfgSocketCallBack {

    private boolean hasConnected = false;//0:未连接,1:连接中,2:已连接

    @Override
    public void onStart() {
        super.onStart();
        JFGCameraDevice device = mSourceManager.getJFGDevice(mUUID);
        if (device != null) {
            mView.onShowProperty(device);
        }
        connectSocket(true);
    }

    protected void connectSocket(boolean init) {
        if (init) {
            JfgSocket.InitSocket(this);
        }
        try {
            AppLogger.d("正在发送 FPing 消息");
            JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
            JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
        } catch (JfgException e) {
            e.printStackTrace();
            AppLogger.d("连接 socket 出现错误");
        }
    }

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getNetWorkChangedSub());
        registerSubscription(getConnectLocalDeviceSub());
    }

    private Subscription getConnectLocalDeviceSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(msg -> {
                    try {
                        JfgUdpMsg.UdpHeader header = DpUtils.unpackData(msg.data, JfgUdpMsg.UdpHeader.class);
                        if (header != null && TextUtils.equals(header.cmd, "f_ping_ack")) {
                            //得到 fping响应
                            JfgUdpMsg.FPingAck pingAck = DpUtils.unpackData(msg.data, JfgUdpMsg.FPingAck.class);
                            if (pingAck != null && TextUtils.equals(pingAck.cid, mUUID)) {
                                synchronized (this) {
                                    if (!hasConnected) {
                                        hasConnected = true;
                                        AppLogger.d("获取到设备 IP 地址:" + msg.ip + ",port:" + msg.port);
                                        JfgSocket.Connect(msg.ip, msg.port, true);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                });
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
        Subscription subscribe = RxBus.getCacheInstance().toObservable(PanoramaEvent.RawRspMsg.class)
                .mergeWith(
                        verifySDCard(mSourceManager.getJFGDevice(mUUID))
                                .observeOn(Schedulers.io())
                                .map(dev -> {
                                    PanoramaEvent.RawReqMsg msg = new PanoramaEvent.RawReqMsg();
                                    byte[] reqBytes = DpUtils.pack(new PanoramaEvent.MSG_TYPE_TAKE_PICTURE_REQ());
                                    byte[] rawBytes = fill(msg, PanoramaEvent.MIDRobotForwardDataV2, PanoramaEvent.TYPE_TAKE_PICTURE_REQ, reqBytes);
                                    JfgSocket.SendMsgpackBuff(rawBytes);
                                    AppLogger.d("正在发送拍照请求:" + new Gson().toJson(msg));
                                    return msg.mSeq;
                                })
                                .flatMap(this::makeForwardDataRspResponse))
                .first()
                .map(rsp -> {
                    PanoramaEvent.MSG_TYPE_TAKE_PICTURE_RSP data = null;
                    try {
                        data = DpUtils.unpackData(rsp.msg, PanoramaEvent.MSG_TYPE_TAKE_PICTURE_RSP.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return data;
                })
                .filter(rsp -> {
                    if (rsp == null || rsp.ret == -1) {
                        //失败了
                        AppLogger.d("拍照失败了!");
                        mView.onMakePhotoGraphFailed();
                        return false;
                    }
                    return true;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    AppLogger.d("拍照成功了" + new Gson().toJson(rsp));
                    mView.onMakePhotoGraphPreview();
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                });
        registerSubscription(subscribe);
    }

    protected Observable<PanoramaEvent.RawRspMsg> makeForwardDataRspResponse(long seq) {
        return RxBus.getCacheInstance().toObservable(PanoramaEvent.RawRspMsg.class)
                .filter(rsp -> rsp.mSeq == seq);
    }

    protected RecordProgress calculate(long count) {//这里计算修正量,因为可能之前已经在录了
        return new RecordProgress((count + 1) * 500);
    }

    @Override
    public void startMakeLongVideo() {

    }

    @Override
    public void stopMakeMakeLongVideo() {
        Subscription subscribe = verifySDCard(mSourceManager.getJFGDevice(mUUID))
                .flatMap(this::verifyBattery)
                .flatMap(ret -> startMakeVideo(2))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(second -> {
                    mView.onUpdateRecordTime(second);
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                });
        registerSubscription(subscribe);
    }

    protected Observable<Integer> startMakeVideo(int type) {
        return RxBus.getCacheInstance().toObservable(PanoramaEvent.RawRspMsg.class)
                .mergeWith(
                        Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
                            PanoramaEvent.RawReqMsg msg = new PanoramaEvent.RawReqMsg();
                            byte[] reqBytes = DpUtils.pack(type);
                            byte[] rawBytes = fill(msg, PanoramaEvent.MIDRobotForwardDataV2, PanoramaEvent.TYPE_VIDEO_BEGIN_REQ, reqBytes);
                            JfgSocket.SendMsgpackBuff(rawBytes);
                            AppLogger.d(new Gson().toJson(msg));
                            subscriber.onNext(msg.mSeq);
                            subscriber.onCompleted();
                        })
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .flatMap(this::makeForwardDataRspResponse)
                )
                .first()
                .map(rsp -> {
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
                        mView.onStartMakeVideoFailed();
                        return false;
                    }
                    return true;
                })
                .flatMap(dev -> Observable.interval(500, TimeUnit.MILLISECONDS)
                        .map(time -> {
                            int second = (int) (time + 1) / 2;
                            RxBus.getCacheInstance().post(calculate(second));
                            return second;
                        }));
    }

    @Override
    public void startMakeShortVideo() {
        Subscription subscribe = verifySDCard(mSourceManager.getJFGDevice(mUUID))
                .flatMap(ret -> startMakeVideo(1))
                .observeOn(AndroidSchedulers.mainThread())
                .map(time -> {
                    mView.onShortVideoStarted();
                    return time;
                })
                .takeUntil(RxBus.getCacheInstance().toObservable(RecordFinished.class).first())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sec -> {
                    mView.onUpdateRecordTime(sec);
                    if (sec >= 8) {
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
                .observeOn(AndroidSchedulers.mainThread())
                .filter(progress -> {
                    if (progress.second < 3) {//录制不足3秒
                        mView.onShortVideoCanceled(-1);
                        return false;
                    }
                    return true;
                })
                .observeOn(Schedulers.io())
                .filter(progress -> progress.second < 7)//超过8秒会在 startMakeShortVideo中完成
                .flatMap(progress -> {
                    return Observable.just("完成了");//这里会插入完成录制逻辑,现在还没接口;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    mView.onShortVideoCompleted();
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                }, () -> {
                    RxBus.getCacheInstance().post(new RecordFinished());
                });
        registerSubscription(subscribe);
    }


    @Override
    public void checkAndInitRecord() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(PanoramaEvent.RawRspMsg.class)
                .mergeWith(
                        Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
                            PanoramaEvent.RawReqMsg msg = new PanoramaEvent.RawReqMsg();
                            byte[] reqBytes = DpUtils.pack(new PanoramaEvent.MSG_TYPE_VIDEO_STATUS_REQ());
                            byte[] rawBytes = fill(msg, PanoramaEvent.MIDRobotForwardDataV2, PanoramaEvent.TYPE_VIDEO_STATUS_REQ, reqBytes);
                            JfgSocket.SendMsgpackBuff(rawBytes);
                            subscriber.onNext(msg.mSeq);
                            subscriber.onCompleted();
                        })
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .flatMap(this::makeForwardDataRspResponse))
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                            if (rsp == null || rsp.ret != -1) {//当前没有录制视频

                            } else if (rsp.videoType == 1) {//正在录制8秒短视频

                            } else if (rsp.videoType == 2) {//正在录制长视频

                            }
                        },
                        e -> {
                            AppLogger.e(e.getMessage());
                            e.printStackTrace();
                        }
                );
        registerSubscription(subscribe);
    }

    public void makeVideoProgressUpdate(int offset, int type) {
        Observable.create(subscriber -> {
            if (offset == 0 && type == 1) {//开始录制8秒短视频
                mView.onShortVideoStarted();
            } else if (offset == 0 && type == 2) {//开始录制长视频
            } else if (offset > 0 && type == 1) {//继续上次的录制8秒短视频
                mView.onUpdateRecordTime(offset);
            } else if (offset > 0 && type == 2) {//继续上次的录制长视频
                mView.onUpdateRecordTime(offset);
            }
            subscriber.onNext("VideoProgressStart");
            subscriber.onCompleted();
        }).subscribeOn(AndroidSchedulers.mainThread())
                .flatMap(ret -> Observable.interval(500, TimeUnit.MILLISECONDS))
                .observeOn(Schedulers.io())
                .map(time -> {
                    int second = (int) (time + 1) / 2;
                    RxBus.getCacheInstance().post(new RecordProgress((time + 1 + 2 * offset) * 500));
                    return second;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(sec -> {

                    return sec;
                });

    }

    protected byte[] fill(PanoramaEvent.RawReqMsg rawReqMsg, int msgId, int type, byte[] msg) {
        rawReqMsg.mSeq = RandomUtils.getRandom(Integer.MAX_VALUE);
        rawReqMsg.dst = Collections.singletonList(mUUID);
        rawReqMsg.mCaller = "";
        rawReqMsg.mCallee = "";
        rawReqMsg.mId = msgId;
        rawReqMsg.isAck = 1;
        rawReqMsg.type = type;
        rawReqMsg.msg = msg;
        return DpUtils.pack(rawReqMsg);
    }

    private Observable<JFGCameraDevice> verifySDCard(JFGCameraDevice device) {
        return Observable.just(device)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(dev -> {
                    if (dev.sdcard_storage == null || !dev.sdcard_storage.hasSdcard) {
                        //没有存储设备
                        mView.onSDCardUnMounted();
                        return true;
                    }
                    return true;
                })
                .filter(dev -> {
                    if (dev != null && dev.sdcard_storage.total - dev.sdcard_storage.used < 1000) {
                        //存储设备内存不足
                        mView.onSDCardMemoryFull();
                        return true;
                    }
                    return true;
                });
    }

    private Observable<JFGCameraDevice> verifyBattery(JFGCameraDevice device) {
        return Observable.just(device)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(dev -> {
                    if (dev.battery == null || dev.battery.value < 5) {
                        //电量低于5%
                        mView.onDeviceBatteryLow();
                        return false;
                    }
                    return true;
                });
    }


    @Override
    public void OnConnected() {
        AppLogger.d("OnConnected");
    }

    @Override
    public void OnDisconnected() {
        AppLogger.d("OnDisconnected");
    }

    @Override
    public void onStop() {
        super.onStop();
        JfgSocket.Disconnect();
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
