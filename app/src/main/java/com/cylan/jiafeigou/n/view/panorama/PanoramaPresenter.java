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

import static com.cylan.jiafeigou.base.module.PanoramaEvent.TYPE_VIDEO_END_REQ;

/**
 * Created by yanzhendong on 2017/3/8.
 */

public class PanoramaPresenter extends BaseViewablePresenter<PanoramaCameraContact.View> implements PanoramaCameraContact.Presenter, JfgSocket.JfgSocketCallBack {

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
                .filter(msg -> {
                    JfgUdpMsg.UdpHeader header = null;
                    try {
                        header = DpUtils.unpackData(msg.data, JfgUdpMsg.UdpHeader.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return header != null && TextUtils.equals(header.cmd, "f_ping_ack");
                })
                .filter(msg -> {
                    JfgUdpMsg.FPingAck pingAck = null;
                    try {
                        pingAck = DpUtils.unpackData(msg.data, JfgUdpMsg.FPingAck.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return pingAck != null && TextUtils.equals(pingAck.cid, mUUID);
                })
                .first()
                .subscribe(msg -> {
                    AppLogger.d("获取到设备 IP 地址:" + msg.ip + ",port:" + msg.port);
                    JfgSocket.Connect(msg.ip, msg.port, true);
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
                                .flatMap(this::makeLocalDataRspResponse))
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
                .observeOn(AndroidSchedulers.mainThread())
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

    protected Observable<PanoramaEvent.RawRspMsg> makeLocalDataRspResponse(long seq) {
        return RxBus.getCacheInstance().toObservable(PanoramaEvent.RawRspMsg.class)
                .filter(rsp -> rsp.mSeq == seq);
    }

    protected RecordProgress calculate(long count) {//这里计算修正量,因为可能之前已经在录了
        return new RecordProgress((count + 1) * 500);
    }

    @Override
    public void startMakeLongVideo() {
        Subscription subscribe = verifySDCard(mSourceManager.getJFGDevice(mUUID))
                .flatMap(this::verifyBattery)
                .flatMap(ret -> startMakeVideo(2))
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(ret -> makeVideoProgressUpdate(0, 2))
                .subscribe(second -> {
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
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
                            AppLogger.d("正在发送录像请求:" + new Gson().toJson(msg));
                            subscriber.onNext(msg.mSeq);
                            subscriber.onCompleted();
                        })
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .flatMap(this::makeLocalDataRspResponse)
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
                });
    }

    protected Observable<PanoramaEvent.MSG_TYPE_VIDEO_END_RSP> stopMakeVideo(int type) {
        return RxBus.getCacheInstance().toObservable(PanoramaEvent.RawRspMsg.class)
                .mergeWith(
                        Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
                            PanoramaEvent.RawReqMsg msg = new PanoramaEvent.RawReqMsg();
                            byte[] rawBytes = fill(msg, PanoramaEvent.MIDRobotForwardDataV2, TYPE_VIDEO_END_REQ, DpUtils.pack(type));
                            JfgSocket.SendMsgpackBuff(rawBytes);
                            subscriber.onNext(msg.mSeq);
                            subscriber.onCompleted();
                        })
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .flatMap(this::makeLocalDataRspResponse))
                .first()
                .map(rsp -> {
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
        Subscription subscribe = verifySDCard(mSourceManager.getJFGDevice(mUUID))
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
                .filter(progress -> progress.second < 7)//超过8秒会在 startMakeShortVideo中完成
                .flatMap(progress -> stopMakeVideo(1)
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(rsp -> {
                            if (rsp == null || rsp.ret == -1) {
                                mView.onStopMakeVideoFailed();
                            } else if (progress.second < 3) {
                                mView.onShortVideoCanceled(-1);
                            } else {
                                mView.onShortVideoCompleted();
                            }
                            return rsp;
                        })

                )
                .subscribe(ret -> {
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
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
                                .flatMap(this::makeLocalDataRspResponse))
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

    public Observable<Integer> makeVideoProgressUpdate(int offset, int type) {
        return Observable.create(subscriber -> {
            if (offset == 0 && type == 1) {//开始录制8秒短视频
                mView.onShortVideoStarted();
            } else if (offset == 0 && type == 2) {//开始录制长视频
                mView.onLongVideoStarted();
            } else if (offset > 0 && type == 1) {//继续上次的录制8秒短视频
                mView.onUpdateRecordTime(offset, type);
            } else if (offset > 0 && type == 2) {//继续上次的录制长视频
                mView.onUpdateRecordTime(offset, type);
            }
            subscriber.onNext("VideoProgressStart");
            subscriber.onCompleted();
        }).subscribeOn(AndroidSchedulers.mainThread())
                .flatMap(ret -> Observable.interval(500, TimeUnit.MILLISECONDS))
                .takeUntil(RxBus.getCacheInstance().toObservable(RecordFinished.class).first())
                .observeOn(Schedulers.io())
                .map(time -> {
                    int second = (int) ((time + 1 + 2 * offset) * 500);
                    RxBus.getCacheInstance().post(new RecordProgress(second));
                    return second;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(sec -> {
                    mView.onUpdateRecordTime(sec, type);
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
//                        return false;
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
