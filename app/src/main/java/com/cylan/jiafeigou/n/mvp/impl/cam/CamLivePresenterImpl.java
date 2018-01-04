package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGHistoryVideoErrorInfo;
import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.ex.JfgException;
import com.cylan.jfgapp.interfases.CallBack;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.live.IFeedRtcp;
import com.cylan.jiafeigou.misc.live.LiveFrameRateMonitor;
import com.cylan.jiafeigou.misc.ver.AbstractVersion;
import com.cylan.jiafeigou.misc.ver.PanDeviceVersionChecker;
import com.cylan.jiafeigou.module.CameraLiveActionHelper;
import com.cylan.jiafeigou.module.CameraLiveHelper;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.module.DPTimeZone;
import com.cylan.jiafeigou.module.DoorLockHelper;
import com.cylan.jiafeigou.module.HistoryManager;
import com.cylan.jiafeigou.module.SubscriptionSupervisor;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractFragmentPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.wheel.ex.DataExt;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import permissions.dispatcher.PermissionUtils;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PREPARE;
import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_HISTORY;
import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_LIVE;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLivePresenterImpl extends AbstractFragmentPresenter<CamLiveContract.View>
        implements CamLiveContract.Presenter, IFeedRtcp.MonitorListener {

    /**
     * 只有从Idle->playing,err->playing才会设置.
     */
    private int resolutionH, resolutionW;
    /**
     * 保存当前播放的方式,eg:从播放历史视频切换到设置页面,回来之后,需要继续播放历史视频.
     */
    private CamLiveContract.LiveStream liveStream;
    /**
     * 帧率记录
     */
    private IFeedRtcp feedRtcp = new LiveFrameRateMonitor();

    private CameraLiveActionHelper liveActionHelper;


    public CamLivePresenterImpl(CamLiveContract.View view) {
        super(view);
        liveActionHelper = new CameraLiveActionHelper(uuid);
        feedRtcp.setMonitorListener(this);
        //清了吧.不需要缓存.
        HistoryManager.getInstance().clearHistory(uuid);
        monitorVideoDisconnect();
        monitorVideoRtcp();
        monitorHistoryVideoError();
        monitorVideoResolution();
    }


    @Override
    public void start() {
        super.start();
        DataSourceManager.getInstance().syncAllProperty(uuid, 204, 222);
        monitorBattery();
        monitorRobotDataSync();
        monitorDeviceUnbind();
        monitorCheckNewVersionRsp();
        monitorSdcardFormatSub();
    }

    private void monitorVideoDisconnect() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGMsgVideoDisconn>() {
                    @Override
                    public void call(JFGMsgVideoDisconn jfgMsgVideoDisconn) {
                        liveActionHelper.onVideoDisconnected(jfgMsgVideoDisconn);
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                            performReportPlayError(playError);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e(throwable);
                        throwable.printStackTrace();
                    }
                });
        addDestroySubscription(subscribe);
    }

    private void monitorVideoRtcp() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGMsgVideoRtcp>() {
                    @Override
                    public void call(JFGMsgVideoRtcp jfgMsgVideoRtcp) {

                        feedRtcp.feed(jfgMsgVideoRtcp);
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                            performReportPlayError(playError);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        AppLogger.e(throwable);
                    }
                });
        addDestroySubscription(subscribe);
    }

    private void monitorHistoryVideoError() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(JFGHistoryVideoErrorInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGHistoryVideoErrorInfo>() {
                    @Override
                    public void call(JFGHistoryVideoErrorInfo jfgHistoryVideoErrorInfo) {

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        AppLogger.e(throwable);
                    }
                });
        addDestroySubscription(subscribe);
    }

    private void monitorVideoResolution() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGMsgVideoResolution>() {
                    @Override
                    public void call(JFGMsgVideoResolution jfgMsgVideoResolution) {

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        AppLogger.e(throwable);
                    }
                });
        addDestroySubscription(subscribe);
    }

    private void performReportPlayError(int playError) {
        Subscription schedule = AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                switch (playError) {
                    case CameraLiveHelper.PLAY_ERROR_STANDBY: {
                        mView.onPlayErrorStandBy();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_FIRST_SIGHT: {
                        mView.onPlayErrorFirstSight();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_NO_NETWORK: {
                        mView.onPlayErrorNoNetwork();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_DEVICE_OFF_LINE: {
                        mView.onPlayErrorDeviceOffLine();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_JFG_EXCEPTION_THROW: {
                        mView.onPlayErrorException();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED: {
                        mView.onPlayErrorWaitForPlayCompleted();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_LOW_FRAME_RATE: {
                        mView.onPlayErrorLowFrameRate();
                        performUpdateBottomMenuEnable();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_BAD_FRAME_RATE: {
                        mView.onPlayErrorBadFrameRate();
                        performUpdateBottomMenuEnable();
                        performStopVideoAction(CameraLiveHelper.isLive(liveActionHelper));
                    }
                    break;
                }
            }
        });
        addStopSubscription(schedule);
    }

    public void performUpdateBottomMenuEnable() {
        boolean microphoneEnable = CameraLiveHelper.checkMicrophoneEnable(liveActionHelper);
        boolean speakerEnable = CameraLiveHelper.checkSpeakerEnable(liveActionHelper);
        boolean doorLockEnable = CameraLiveHelper.checkDoorLockEnable(liveActionHelper);
        boolean captureEnable = CameraLiveHelper.checkCaptureEnable(liveActionHelper);
        mView.onUpdateBottomMenuEnable(microphoneEnable, speakerEnable, doorLockEnable, captureEnable);
    }

    @Override
    public void performPlayVideoAction(boolean live, long timestamp) {
        Subscription subscribe = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    //当前情况下不能播放
                    subscriber.onError(new RxEvent.HelperBreaker(playError));
                    return;
                }

                liveActionHelper.onUpdateVideoPlayType(live);

                boolean shouldDisconnectFirst = CameraLiveHelper.shouldDisconnectFirst(liveActionHelper);
                if (shouldDisconnectFirst) {
                    //播放前需要先断开
                    performStopVideoAction(live);
                }

                int playCode;
                try {
                    if (live) {
                        playCode = Command.getInstance().playVideo(uuid);
                    } else {
                        playCode = Command.getInstance().playHistoryVideo(uuid, timestamp);
                    }
                } catch (Exception e) {
                    playCode = CameraLiveHelper.PLAY_ERROR_JFG_EXCEPTION_THROW;
                    e.printStackTrace();
                    AppLogger.e(e);
                }

                liveActionHelper.onUpdateVideoPlayCode(playCode);

                playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    //开始播放历史视频或直播出现了错误
                    subscriber.onError(new RxEvent.HelperBreaker(playError));
                    return;
                }

                liveActionHelper.onVideoPlayStarted(live);
                Subscription schedule = Schedulers.io().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                            subscriber.onError(new RxEvent.HelperBreaker(playError));
                        } else {
                            subscriber.onNext("应该是播放成功了啊...");
                            subscriber.onCompleted();
                        }
                    }
                }, 30, TimeUnit.SECONDS);
                subscriber.add(schedule);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                            performReportPlayError(playError);
                        }
                    }
                });
        addStopSubscription(subscribe);
    }

    @Override
    public void performStopVideoAction(boolean live) {
        Subscription subscribe = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                int playError = CameraLiveHelper.checkPlayError(liveActionHelper);

                if (playError == CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    performLiveThumbSaveAction(true);
                }

                int playCode;
                try {
                    playCode = Command.getInstance().stopPlay(uuid);
                } catch (Exception e) {
                    playCode = CameraLiveHelper.PLAY_ERROR_JFG_EXCEPTION_THROW;
                    e.printStackTrace();
                    AppLogger.e(e);
                }
                liveActionHelper.onUpdateVideoPlayCode(playCode);

                playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    subscriber.onError(new RxEvent.HelperBreaker(playError));
                    return;
                }

                liveActionHelper.onUpdateVideoPlayType(live);
                subscriber.onNext("");
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        addStopSubscription(subscribe);
    }

    @Override
    public void performStopVideoAction() {
        performStopVideoAction(CameraLiveHelper.isLive(liveActionHelper));
    }

    @Override
    public void performLiveThumbSaveAction(boolean sync) {
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            if (CameraLiveHelper.isVideoPlaying(liveActionHelper)) {
                Command.getInstance().screenshot(false, new CallBack<Bitmap>() {
                    @Override
                    public void onSucceed(Bitmap bitmap) {
                        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                        PerformanceUtils.startTrace("takeCapture");
                        if (bitmap == null) {
                            AppLogger.e("performLiveThumbSaveAction bitmap is null ..");
                            return;
                        }
                        final String fileName = uuid + System.currentTimeMillis() + ".png";
                        final String cover = JConstant.MEDIA_PATH + File.separator + uuid + "_cover.png";
                        final String filePath = JConstant.MEDIA_PATH + File.separator + fileName;
                        //需要删除之前的一条记录.
                        BitmapUtils.saveBitmap2file(bitmap, cover);
                        PreferencesUtils.putString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, System.currentTimeMillis() + "");
                        PreferencesUtils.putString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_FILE + uuid, cover);
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onFailure(String s) {
                        countDownLatch.countDown();
                    }
                });
            } else {
                countDownLatch.countDown();
            }
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void performDeviceInfoChangedAction(ArrayList<JFGDPMsg> dpList) {
        for (JFGDPMsg msg : dpList) {

            try {
                mView.onDeviceInfoChanged(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }

            switch ((int) msg.id) {
                case DpMsgMap.ID_222_SDCARD_SUMMARY: {
                    DpMsgDefine.DPSdcardSummary sdStatus = DpUtils.unpackDataWithoutThrow(msg.packValue, DpMsgDefine.DPSdcardSummary.class, null);
                    boolean hasSdcard = JFGRules.hasSdcard(sdStatus);
                    liveActionHelper.onUpdateSDCard(hasSdcard);
                    if (!hasSdcard) {
                        AppLogger.d("sdcard 被拔出");
                        updateLiveStream(TYPE_LIVE, 0, -1);
                        mView.onDeviceSDCardOut();
                    }
                }
                break;
                case DpMsgMap.ID_206_BATTERY: {
                    if (JFGRules.popPowerDrainOut(getDevice().pid)) {
                        Integer battery = DpUtils.unpackDataWithoutThrow(msg.packValue, Integer.class, 0);
                        if (battery != null && battery <= 20 && getDevice().$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet()).net > 0) {
                            mView.onBatteryDrainOut();
                        }
                    }
                }
                break;
                case DpMsgMap.ID_508_CAMERA_STANDBY_FLAG: {
                    DpMsgDefine.DPStandby standby = DpUtils.unpackDataWithoutThrow(msg.packValue, DpMsgDefine.DPStandby.class, null);
                    boolean standBy = JFGRules.isStandBy(standby);
                    liveActionHelper.onUpdateStandBy(standBy);
                    if (standBy) {
                        mView.onPlayErrorStandBy();
                        performStopVideoAction(CameraLiveHelper.isLive(liveActionHelper));
                    } else if (CameraLiveHelper.canPlayVideoNow(uuid)) {
                        boolean live = CameraLiveHelper.isLive(liveActionHelper);
                        performPlayVideoAction(live, CameraLiveHelper.getLastPlayTime(live, liveActionHelper));
                    }
                }
                break;
                case DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD: {
                    mView.onDeviceSDCardFormat();
                }
                break;
                case DpMsgMap.ID_509_CAMERA_MOUNT_MODE: {
                    String _509 = DpUtils.unpackDataWithoutThrow(msg.packValue, String.class, "1");
                    Device device = DataSourceManager.getInstance().getDevice(uuid);
                    if (device.pid == 39 || device.pid == 49) {
                        _509 = "0";
                    }
                    mView.onUpdateLiveViewMode(_509);
                }
                break;
                case DpMsgMap.ID_201_NET: {

                }
                break;
                case DpMsgMap.ID_214_DEVICE_TIME_ZONE: {
                    DPTimeZone dpTimeZone = DpUtils.unpackDataWithoutThrow(msg.packValue, DPTimeZone.class, null);
                    if (dpTimeZone != null) {
                        mView.onDeviceTimeZoneChanged(dpTimeZone);
                    }
                }
                break;
                case DpMsgMap.ID_510_CAMERA_COORDINATE: {
                    DpMsgDefine.DpCoordinate dpCoordinate = DpUtils.unpackDataWithoutThrow(msg.packValue, DpMsgDefine.DpCoordinate.class, null);
                    if (dpCoordinate != null) {
                        mView.onUpdateCameraCoordinate(dpCoordinate);
                    }
                }
                break;
            }
        }
    }

    @Override
    public void pause() {
        super.pause();
        //历史视频播放的时候，如果来了门铃。然而等待onStop，unSubscribe就非常晚。会导致这里的
        //rtcp还会继续接受数据。继续更新时间轴。
        unSubscribe("RTCPNotifySub");
    }

    private void monitorDeviceUnbind() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.DeviceUnBindedEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(event -> TextUtils.equals(event.uuid, uuid))
                .subscribe(event -> {
                    if (mView != null) {
                        mView.onDeviceUnBind();
                    }
                }, e -> AppLogger.d(e.getMessage()));
        addStopSubscription(subscribe);
    }

    /**
     * 1.需要获取设备电量并且,一天一次提醒弹窗.
     * 2.
     *
     * @return
     */
    private void monitorBattery() {
        //按照
        if (JFGRules.popPowerDrainOut(getDevice().pid)) {
            Subscription subscribe = Observable.just("monitorBattery")
                    .subscribeOn(Schedulers.io())
                    .filter(ret -> NetUtils.getJfgNetType() > 0)//在线
                    .filter(ret -> !JFGRules.isShareDevice(uuid))//非分享
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                        Device device = getDevice();
                        Integer battery = device.$(DpMsgMap.ID_206_BATTERY, 0);
                        DpMsgDefine.DPNet net = device.$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
                        if (battery <= 20 && net.net > 0) {//电量低
                            DBOption.DeviceOption option = device.option(DBOption.DeviceOption.class);
                            if (option != null && option.lastLowBatteryTime < TimeUtils.getTodayStartTime()) {//新的一天
                                option.lastLowBatteryTime = System.currentTimeMillis();
                                device.setOption(option);
                                DataSourceManager.getInstance().updateDevice(device);
                                mView.onBatteryDrainOut();
                            }
//                            mView.onBatteryDrainOut();
                        }
                    }, AppLogger::e);
            addStopSubscription(subscribe);
        }
    }

    private void monitorCheckNewVersionRsp() {
        Subscription subscription = RxBus.getCacheInstance().toObservable(AbstractVersion.BinVersion.class)
                .filter(ret -> mView != null && mView.isAdded() && TextUtils.equals(ret.getCid(), uuid))
                .retry()
                .subscribe(version -> {
                    version.setLastShowTime(System.currentTimeMillis());
                    PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + uuid, new Gson().toJson(version));
                    mView.showFirmwareDialog();
                }, AppLogger::e);
        PanDeviceVersionChecker version = new PanDeviceVersionChecker();
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        version.setPortrait(new AbstractVersion.Portrait().setCid(uuid).setPid(device.pid));
        version.setShowCondition(() -> {
            DpMsgDefine.DPNet dpNet = getDevice().$(201, new DpMsgDefine.DPNet());
            //设备离线就不需要弹出来
            if (!JFGRules.isDeviceOnline(dpNet)) {
                return false;
            }
            //局域网弹出
            if (!MiscUtils.isDeviceInWLAN(uuid)) {
                return false;
            }
            return true;
        });
        version.startCheck();
        addStopSubscription(subscription);
    }

//    /**
//     * 视频断开连接
//     * 只需要开始播放后注册
//     *
//     * @return
//     */
//    private Subscription videoDisconnectSub() {
//        return RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
//                .subscribeOn(Schedulers.io())
//                .filter(ret -> mView != null)
//                .map(ret -> {
//                    AppLogger.i("stop for reason: " + ret.code);
//                    stopPlayVideo(ret.code);
//                    return ret;
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(ret -> {
//                    BellPuller.getInstance().currentCaller(null);
//                    updateLiveStream(getLiveStream().type, -1, PLAY_STATE_IDLE);
//                    getView().onLiveStop(getLiveStream().type, ret.code);
////                    feedRtcp.stop();
//                    AppLogger.d("reset subscription");
//                }, AppLogger::e);
//    }

    @Override
    public String getThumbnailKey() {
        return PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_FILE + uuid);
    }

    @Override
    public int getPlayState() {
        return getLiveStream().playState;
    }

    @Override
    public int getPlayType() {
        return getLiveStream().type;
    }

    @Override
    public CamLiveContract.LiveStream getLiveStream() {
        if (liveStream == null) {
            this.liveStream = new CamLiveContract.LiveStream();
        }
        return this.liveStream;
    }

    @Override
    public void updateLiveStream(CamLiveContract.LiveStream prePlayType) {
        this.liveStream = prePlayType;
    }


    @Override
    public float getVideoPortHeightRatio() {
        AppLogger.d("获取分辨率?");
        float cache = PreferencesUtils.getFloat(JConstant.KEY_UUID_RESOLUTION + uuid, 0.0f);
        if (cache == 0.0f) {
            cache = JFGRules.getDefaultPortHeightRatio(getDevice().pid);
        }
        return PreferencesUtils.getFloat(JConstant.KEY_UUID_RESOLUTION + uuid, cache);
    }

    @Override
    public void saveHotSeatState() {
        getHotSeatStateMaintainer().saveRestore();
    }

    @Override
    public void restoreHotSeatState() {
        getHotSeatStateMaintainer().restore();
    }

    @Override
    public void fetchHistoryDataListV1(String uuid) {
        Observable<String> observable = Observable.create((Observable.OnSubscribe<String>) subscriber -> {
            HistoryManager.getInstance().fetchHistoryV1(uuid);
            subscriber.onNext(uuid);
            subscriber.onCompleted();
        });
        fetchHistoryDataListCompat(observable);
    }

    @Override
    public void fetchHistoryDataListV2(String uuid, int time, int way, int count) {
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                HistoryManager.getInstance().fetchHistoryV2(uuid, time, way, count);
                subscriber.onNext(uuid);
                subscriber.onCompleted();
            }
        });
        fetchHistoryDataListCompat(observable);
    }

    private void fetchHistoryDataListCompat(Observable<String> observable) {
        if (getLiveStream().playState == PLAY_STATE_PLAYING || getLiveStream().playState == PLAY_STATE_PREPARE) {
//            stopPlayVideo(PLAY_STATE_STOP);
            AppLogger.d("获取历史录像,先断开直播,或者历史录像");
        }
        boolean videoPlaying = CameraLiveHelper.isVideoPlaying(liveActionHelper);
        if (videoPlaying) {
            performStopVideoAction(CameraLiveHelper.isLive(liveActionHelper));
            AppLogger.d("获取历史录像,先断开直播,或者历史录像");
        }

        Subscription subscribe = observable.subscribeOn(Schedulers.io())
                .flatMap(cid -> RxBus.getCacheInstance().toObservable(JFGHistoryVideo.class))
                .first()
                .delay(1, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .map(rsp -> {
                    if (HistoryManager.getInstance().hasHistory(uuid)) {
                        return HistoryManager.getInstance().getHistory(uuid);
                    }
                    return null;
                })
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<TreeSet<JFGVideo>>() {
                    @Override
                    public void call(TreeSet<JFGVideo> jfgVideos) {
                        if (jfgVideos == null) {
                            //没有历史视频
                            mView.onHistoryEmpty();
                        } else {
                            //有历史视频
                            mView.onHistoryReady(jfgVideos);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        AppLogger.e(throwable);
                        mView.onLoadHistoryFailed();
                    }
                });
        addStopSubscription(subscribe);
    }

    @Override
    public void openDoorLock(String password) {
        if (liveStream.playState != PLAY_STATE_PLAYING) {
            //还没有开始直播,则需要开始直播
//            startPlay();
            performPlayVideoAction(true, 0);
        }
        Subscription subscribe = DoorLockHelper.INSTANCE.openDoor(uuid, password)
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> LoadingDialog.showLoading(mView.activity(), mView.getContext().getString(R.string.DOOR_OPENING), false))
                .doOnTerminate(LoadingDialog::dismissLoading)
                .subscribe(success -> {
                    if (success == null) {
                        mView.onOpenDoorError();
                    } else if (success == 0) {
                        mView.onOpenDoorSuccess();
                    } else if (success == 2) {
                        mView.onOpenDoorPasswordError();
                    } else {
                        mView.onOpenDoorError();
                    }
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e);
                });
        addSubscription(method(), subscribe);
    }

    @Override
    public boolean isHistoryEmpty() {
        return !HistoryManager.getInstance().hasHistory(uuid);
    }

    @Override
    public boolean isShareDevice() {
        return JFGRules.isShareDevice(uuid);
    }

    @Override
    public boolean isDeviceStandby() {
        DpMsgDefine.DPStandby standby = getDevice().$(508, new DpMsgDefine.DPStandby());
        return standby.standby;
    }


//    @Override
//    public void startPlay() {
//        if (mView == null || !mView.isUserVisible()) {
//            return;
//        }
//        if (getLiveStream().playState == PLAY_STATE_PREPARE) {
//            AppLogger.d("已经loading");
//            mView.onLivePrepare(getLiveStream().type);
//            return;
//        }
//        mView.onLivePrepare(getLiveStream().type);
//        boolean sdkOnlineStatus = DataSourceManager.getInstance().isOnline();
//        if (!sdkOnlineStatus) {
//            String routeMac = NetUtils.getRouterMacAddress();
//            String deviceMac = getDevice().$(202, "");
//            boolean AP = !TextUtils.isEmpty(routeMac) && TextUtils.equals(deviceMac, routeMac);
//            AppLogger.d("直连Ap?" + AP);
//        }
//        addSubscription(beforePlayObservable(s -> {
//            try {
//                int ret;
//                boolean switchInterface = false;
//                // 历史播放中，需要停止,不能保证上次是播放的是历史还是直播，所以直接断开。
//                if (getLiveStream().playState == PLAY_STATE_PLAYING) {
//                    Command.getInstance().stopPlay(uuid);  // 先停止播放
//                    switchInterface = true;
//                }
//                // TODO: 2017/9/2 记录开始播放时间,在开始播放的最初几秒内禁止 Rtcp回调
//                getLiveStream().playStartTime = System.currentTimeMillis() / 1000;
////                getLiveStream().playStartTime = System.currentTimeMillis();
//                ret = Command.getInstance().playVideo(uuid);
//
//                AppLogger.d("play video ret :" + ret + "," + switchInterface);
//
//                // TODO: 2017/7/12 判断当前是否需要拦截呼叫事件 针对所有的门铃产品
////                if (JFGRules.isBell(getDevice().pid)) {
//                BellPuller.getInstance().currentCaller(uuid);//查看直播时禁止呼叫
////                }
//                getHotSeatStateMaintainer().saveRestore();
//                AppLogger.i("play video: " + uuid + " " + ret);
//            } catch (JfgException e) {
//                e.printStackTrace();
//            }
//            updateLiveStream(getLiveStream().type, -1, PLAY_STATE_PREPARE);
//            getView().onLivePrepare(TYPE_LIVE);
//            return null;
//        }).subscribe(objectObservable -> {
//            AppLogger.d("播放流程走通 done,不在这个环节获取历史视频");
//        }, AppLogger::e), "beforePlayObservable");
//    }
//
//    /**
//     * 错误码
//     */
//    private Subscription errCodeSub() {
//        return RxBus.getCacheInstance().toObservable(JFGHistoryVideoErrorInfo.class)
//                .subscribeOn(Schedulers.io())
//                .filter(ret -> ret != null && ret.code != 0)
//                .subscribe(result -> {
//                    stopPlayVideo(result.code);
//                    removeTimeoutSub();
//                }, AppLogger::e);
//    }


    /**
     * 一旦有rtcp消息回来,就表明直播通了,需要通知更新UI.
     * 这个逻辑只需要第一个消息,然后就断开
     *
     * @return
     */
    private Subscription getFirstRTCPNotification() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                .filter((JFGMsgVideoRtcp rtcp) -> (getView() != null) && rtcp.frameRate > 0)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    throw new RxEvent.HelperBreaker("Sweat");
                }, throwable -> {
                    if (throwable instanceof RxEvent.HelperBreaker) {
                        AppLogger.d("收到RTCP通知:" + resolutionH);
                        //需要收到发送一个Resolution
                        getHotSeatStateMaintainer().restore();
                        if (resolutionH != 0 && resolutionW != 0) {
                            //因为直播转历史录像,不会再有JFGMsgVideoResolution回调.
                            JFGMsgVideoResolution resolution = new JFGMsgVideoResolution();
                            resolution.peer = uuid;
                            resolution.height = resolutionH;
                            resolution.width = resolutionW;
                            RxBus.getCacheInstance().post(resolution);
                        }
                    }
                });
    }

    /**
     * Rtcp和resolution的回调,
     * 只有resolution回调之后,才能设置{@link JfgAppCmd#enableRenderLocalView(boolean, View)} (View)}
     * 正常播放回调
     * 10s没有视频,直接断开
     *
     * @return
     */
    private Subscription RTCPNotifySub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                .subscribeOn(Schedulers.io())
                //断网就不要feed.
                .filter(rtcp -> getView() != null && NetUtils.getJfgNetType() != 0)
                .map(rtcp -> {
                    removeTimeoutSub();
                    feedRtcp.feed(rtcp);
                    updateLiveStream(getLiveStream().type, rtcp.timestamp, PLAY_STATE_PLAYING);
                    return rtcp;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rtcp -> {
                    try {
                        getView().onRtcp(rtcp);
                    } catch (Exception e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                }, AppLogger::e);
    }
//
//    private Subscription timeoutSub() {
//        return Observable.just("timeout")
//                .subscribeOn(Schedulers.newThread())
//                .delay(30, TimeUnit.SECONDS)
//                .doOnError(ret -> AppLogger.e("30s 超时了"))
//                .subscribe(ret -> {
//                    //需要发送超时
//                    stopPlayVideo(JFGRules.PlayErr.ERR_NOT_FLOW);
//                    BellPuller.getInstance().currentCaller(null);
//                }, AppLogger::e);
//    }

    /**
     * 只有新建立的播放,才有这个回调.
     *
     * @return
     */
    private Subscription resolutionSub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                .timeout(30, TimeUnit.SECONDS)
                .filter(resolution -> TextUtils.equals(resolution.peer, uuid))
                .observeOn(Schedulers.io())
                .map(resolution -> {
                    addSubscription(RTCPNotifySub(), "RTCPNotifySub");
                    removeTimeoutSub();
                    PreferencesUtils.putFloat(JConstant.KEY_UUID_RESOLUTION + uuid, (float) resolution.height / resolution.width);

                    //注册监听耳机
                    registerHeadSetObservable();
                    //正向,抛异常
                    resolutionH = resolution.height;
                    resolutionW = resolution.width;
                    throw new RxEvent.HelperBreaker(resolution);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                }, throwable -> {
                    if (throwable instanceof RxEvent.HelperBreaker) {
                        Object o = ((RxEvent.HelperBreaker) throwable).object;
                        if (o instanceof JFGMsgVideoResolution) {
                            JFGMsgVideoResolution resolution = (JFGMsgVideoResolution) o;
                            AppLogger.i("ResolutionNotifySub: " + new Gson().toJson(resolution) + "," + Thread.currentThread().getName());
                            try {
                                getView().onResolution(resolution);
                                //保存分辨率
                                PreferencesUtils.putFloat(JConstant.KEY_UUID_RESOLUTION + uuid, (float) resolution.height / resolution.width);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                            updateLiveStream(-1, -1, PLAY_STATE_PLAYING);
                            getView().onLiveStarted(getLiveStream().type);
                            getHotSeatStateMaintainer().restore();
                        }
                    }
                });
    }

//    /**
//     * 1.检查网络
//     * 2.开始播放
//     *
//     * @return
//     */
//    private Observable<String> beforePlayObservable(Func1<String, String> func1) {
//        return Observable.just("")
//                .subscribeOn(AndroidSchedulers.mainThread())
//                .filter(o -> {
//                    DpMsgDefine.DPStandby dpStandby = getDevice().$(508, new DpMsgDefine.DPStandby());
//                    if (dpStandby.standby) {
//                        return false;//待机模式
//                    }
//                    if (NetUtils.getJfgNetType() == 0) {
//                        //客户端断网了
//                        stopPlayVideo(ERR_NETWORK);
//                        if (mView != null) {
//                            mView.onLiveStop(getLiveStream().type, ERR_NETWORK);
//                        }
//                        AppLogger.i("stop play  video for err network");
//                        return false;
//                    }
//                    return true;
//                })
//                .map(ret -> {
//                    //加入管理,如果播放失败,收到disconnect
//
//                    feedRtcp.stop();
//                    addSubscription(videoDisconnectSub(), "videoDisconnectSub");
//                    addSubscription(errCodeSub(), "errCodeSub");
//                    //挪到resolutionSub里面
////                    addSubscription(RTCPNotifySub(), "RTCPNotifySub");
//                    addSubscription(resolutionSub(), "resolutionSub");
//                    addSubscription(timeoutSub(), "timeoutSub");
//                    addSubscription(getFirstRTCPNotification(), "getFirstRTCPNotification");
//                    return "";
//                })
//                .observeOn(Schedulers.io())
//                .map(func1);
//    }

    private void removeTimeoutSub() {
        unSubscribe("timeoutSub");
    }

    private void updateLiveStream(int type, long time, int state) {
        if (liveStream == null) {
            liveStream = new CamLiveContract.LiveStream();
        }
        if (type != -1) {
            liveStream.type = type;
        }
        if (time != -1 && time != 0) {
            liveStream.time = time;
        }
        liveStream.playState = state;
        Log.d("updateLiveStream", "updateLiveStream:" + liveStream);
    }

//    @Override
//    public void startPlayHistory(long t) {
//        //保证得到s System.currentTimeMillis() / t == 0 的条件范围可能有点小
//        if (t == 0) {
//            t = 1;
//            if (BuildConfig.DEBUG) {
//                throw new IllegalArgumentException("怎么会有这种情况发生");
//            }
//        }
//        if (t == getLiveStream().playStartTime) {
//            return;//多次调用了,过滤掉即可
//        }
//        final long time = System.currentTimeMillis() / t > 100 ? t : t / 1000;
//        // TODO: 2017/9/2 记录开始播放时间,在开始播放的最初几秒内禁止 Rtcp回调
//        getLiveStream().playStartTime = time;
//        getView().onLivePrepare(TYPE_HISTORY);
//        DpMsgDefine.DPNet net = getDevice().$(201, new DpMsgDefine.DPNet());
//        if (!JFGRules.isDeviceOnline(net)) {
//            updateLiveStream(TYPE_HISTORY, -1, PLAY_STATE_IDLE);
//            mView.onLiveStop(TYPE_HISTORY, JFGRules.PlayErr.ERR_DEVICE_OFFLINE);
//            return;
//        }
//        addSubscription(beforePlayObservable(s -> {
//            try {
//                int ret = 0;
//                getLiveStream().time = time;
//                getHotSeatStateMaintainer().saveRestore();
//                if (getLiveStream().playState != PLAY_STATE_PLAYING) {
////                    Command.getInstance().performPlayVideoAction(uuid);
////                    AppLogger.i(" stop video .first......");
//                }
//                ret = Command.getInstance().playHistoryVideo(uuid, time);
//                //说明现在是在查看历史录像了,泽允许进行门铃呼叫
//                BellPuller.getInstance().currentCaller(null);
//                updateLiveStream(TYPE_HISTORY, time, PLAY_STATE_PREPARE);
//                AppLogger.i("play history video: " + uuid + " time:" + History.parseTime2Date(TimeUtils.wrapToLong(time)) + " ret:" + ret);
//            } catch (Exception e) {
//                AppLogger.e("err:" + e.getLocalizedMessage());
//            }
//            return null;
//        }).subscribe(ret -> {
//        }, AppLogger::e), "playHistory");
//    }

//    @Override
//    public void stopPlayVideo(int reasonOrState) {
//        AppLogger.d("pre play state: " + liveStream);
//        if (liveStream == null || liveStream.playState == PLAY_STATE_IDLE
//                || liveStream.playState == PLAY_STATE_STOP) {
////            return Observable.just(false);
//        }
//
//        resolutionW = resolutionH = 0;
//        Observable.just(uuid)
//                .subscribeOn(Schedulers.io())
//                .flatMap((String s) -> {
//                    try {
//                        Command.getInstance().stopPlay(s);
//                        getHotSeatStateMaintainer().reset();
//                        updateLiveStream(getLiveStream().type, -1, reasonOrState);
//                        BellPuller.getInstance().currentCaller(null);
//                        AppLogger.i("stopPlayVideo:" + s);
//                    } catch (JfgException e) {
//                        AppLogger.e("stop play err: " + e.getLocalizedMessage());
//                    }
//                    AppLogger.d("live stop: " + reasonOrState);
//                    if (getView() != null) {
//                        getView().onLiveStop(getLiveStream().type, reasonOrState);
//                    }
//                    return Observable.just(true);
//                })
//                .doOnError(throwable -> AppLogger.e("stop play err" + throwable.getLocalizedMessage()));
//    }
//
//    @Override
//    public void stopPlayVideo(boolean detach) {
//        stopPlayVideo(PLAY_STATE_STOP);
//    }

    @Override
    public String getUuid() {
        return uuid;
    }

    /**
     * 查看 doc/mic_speaker设置.md
     * localSpeaker 0->1{ }
     *
     * @return
     */
    @Override
    public void switchSpeaker() {
        getHotSeatStateMaintainer().switchSpeaker();

    }

    @Override
    public HotSeatStateMaintainer getHotSeatStateMaintainer() {
        if (hotSeatStateMaintainer == null) {
            hotSeatStateMaintainer = new HotSeatStateMaintainer(mView, this);
        }
        return hotSeatStateMaintainer;
    }

    /**
     * localMic 0-->1{全部打开}
     * localMic 1-->0{}
     * 查看 doc/mic_speaker设置.md
     *
     * @return
     */
    @Override
    public void switchMic() {
        getHotSeatStateMaintainer().switchMic();
    }

    /**
     * 高清,标清,自动模式切换
     *
     * @param mode
     * @return
     */
    @Override
    public Observable<Boolean> switchStreamMode(int mode) {
        return Observable.just(mode)
                .subscribeOn(Schedulers.io())
                .flatMap(integer -> {
                    DpMsgDefine.DPPrimary<Integer> dpPrimary = new DpMsgDefine.DPPrimary<>(integer);
                    try {
                        AppLogger.e("还需要发送局域网消息");
                        return Observable.just(DataSourceManager.getInstance().updateValue(uuid, dpPrimary, 513));
                    } catch (IllegalAccessException e) {
                        return Observable.just(false);
                    }
                });
    }

    @Override
    public void saveAlarmFlag(boolean flag) {
        Log.d("saveAlarmFlag", "saveAlarmFlag: " + flag);
    }

    @Override
    public void saveAndShareBitmap(Bitmap bitmap, boolean forPopWindow, boolean save) {
        AppLogger.i("take shot saveAndShareBitmap");
        Observable.just(bitmap)
                .subscribeOn(Schedulers.io())
                .map(new TakeSnapShootHelper(uuid, forPopWindow, mView, save))
                .observeOn(Schedulers.io())
                .subscribe(pair -> {
                }, AppLogger::e, () -> AppLogger.d("take screen finish"));
    }

    @Override
    public boolean needShowHistoryWheelView() {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        DpMsgDefine.DPNet net = device.$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
        DpMsgDefine.DPSdStatus sdStatus = device.$(DpMsgMap.ID_204_SDCARD_STORAGE, new DpMsgDefine.DPSdStatus());
        boolean show = JFGRules.isDeviceOnline(net)
                && NetUtils.getJfgNetType(getView().getContext()) != 0
                && TextUtils.isEmpty(device.shareAccount)
                && sdStatus.hasSdcard && sdStatus.err == 0
                && DataExt.getInstance().getDataCount() > 0;
        AppLogger.i("show: " + show);
        return show;
    }

    @Override
    public <T extends DataPoint> void updateInfoReq(T value, long id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        DataSourceManager.getInstance().updateValue(uuid, value, (int) id);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }

    /**
     * sd卡被格式化
     *
     * @return
     */
    private void monitorSdcardFormatSub() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class)
                .subscribeOn(Schedulers.io())
                .filter(ret -> mView != null && TextUtils.equals(ret.uuid, uuid))
                .map(ret -> ret.rets)
                .flatMap(Observable::from)
                .filter(msg -> msg.id == 218)
                .map(msg -> {
                    if (msg.ret == 0) {
                        DataExt.getInstance().clean();
                        History.getHistory().clearHistoryFile(uuid);
                        AppLogger.d("清空历史录像");
                    }
                    return msg;
                })
                .subscribe(ret -> {
                }, AppLogger::e);
        addStopSubscription(subscribe);
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private void monitorRobotDataSync() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter((RxEvent.DeviceSyncRsp jfgRobotSyncData) -> (
                        jfgRobotSyncData.dpList != null &&
                                getView() != null && TextUtils.equals(uuid, jfgRobotSyncData.uuid)
                ))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .retry(new RxHelper.RxException<>("monitorRobotDataSync"))
                .subscribe(jfgRobotSyncData -> {
                    if (jfgRobotSyncData.dpList != null) {
                        performDeviceInfoChangedAction(jfgRobotSyncData.dpList);
                    }
                }, throwable -> AppLogger.e(MiscUtils.getErr(throwable)));
        addStopSubscription(subscribe);
    }


    @Override
    public void onFrameFailed() {
        AppLogger.e("is bad net work");
        liveActionHelper.onUpdateVideoFrameFailed();
        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
            performReportPlayError(playError);
        }
        if (playError == CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
            //说明当前是正常播放状态下 FrameFailed
            performStopVideoAction(CameraLiveHelper.isLive(liveActionHelper));
        } else {
            performReportPlayError(playError);
        }
        //暂停播放
//        stopPlayVideo(JFGRules.PlayErr.ERR_LOW_FRAME_RATE);
    }

    @Override
    public void onFrameRate(boolean slow) {
        AppLogger.e("is bad net work show loading?" + slow);
        liveActionHelper.onUpdateVideoSlowState(slow);
        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
            performReportPlayError(playError);
        }

        if (slow) {
            saveHotSeatState();
        } else {
            restoreHotSeatState();
        }
        Observable.just(slow)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(slowFrameRate -> {
                    getView().shouldWaitFor(slow);
                }, throwable -> {
                    AppLogger.e("err: " + throwable.getLocalizedMessage());
                });
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{ConnectivityManager.CONNECTIVITY_ACTION,
                NETWORK_STATE_CHANGED_ACTION};
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        String action = intent.getAction();
        if (mView == null) {
            return;
        }
        if (networkAction == null) {
            networkAction = new NetworkAction(this);
        }
        if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)
                || TextUtils.equals(action, NETWORK_STATE_CHANGED_ACTION)) {
            networkAction.run();
        }
    }

    private NetworkAction networkAction;

    private static class NetworkAction {
        private int preNetType = 0;
        private WeakReference<CamLivePresenterImpl> presenterWeakReference;

        public NetworkAction(CamLivePresenterImpl camLivePresenter) {
            preNetType = NetUtils.getJfgNetType();
            this.presenterWeakReference = new WeakReference<>(camLivePresenter);
        }

        public void run() {
            if (presenterWeakReference != null && presenterWeakReference.get() != null) {
                Observable.just("")
                        .subscribeOn(Schedulers.io())
                        .filter(ret -> presenterWeakReference.get().mView != null)
                        .subscribe(ret -> {
                            int net = NetUtils.getJfgNetType();
                            if (preNetType == net) {
                                return;
                            }
                            preNetType = net;
                            if (net == 0) {
                                AppLogger.i("网络中断");
                                presenterWeakReference.get().mView.onNetworkChanged(false);
                            } else {
                                presenterWeakReference.get().mView.onNetworkChanged(true);
                                AppLogger.d("网络恢复");
                            }
                        }, AppLogger::e);
            }
        }
    }

    private static class TakeSnapShootHelper implements Func1<Object, Pair<Bitmap, String>> {
        WeakReference<CamLiveContract.View> weakReference;
        boolean forPopWindow;
        String uuid;
        boolean save;

        TakeSnapShootHelper(String uuid, boolean forPopWindow, CamLiveContract.View v, boolean save) {
            this.uuid = uuid;
            this.forPopWindow = forPopWindow;
            this.weakReference = new WeakReference<>(v);
            this.save = save;
        }

        @Override
        public Pair<Bitmap, String> call(Object o) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            PerformanceUtils.startTrace("takeCapture");
            Bitmap bitmap = (Bitmap) o;
            if (bitmap == null) {
                AppLogger.e("takesnapshot bitmap is null ..");
                return null;
            }
            final String fileName = uuid + System.currentTimeMillis() + ".png";
            final String cover = JConstant.MEDIA_PATH + File.separator + uuid + "_cover.png";
            final String filePath = JConstant.MEDIA_PATH + File.separator + fileName;
            //需要删除之前的一条记录.
            if (save) {
                BitmapUtils.saveBitmap2file(bitmap, filePath);
                AppLogger.e("save bitmap to sdcard " + filePath);
            } else {
                BitmapUtils.saveBitmap2file(bitmap, cover);
                PreferencesUtils.putString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, System.currentTimeMillis() + "");
                PreferencesUtils.putString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_FILE + uuid, cover);

            }
            if (weakReference.get() != null && forPopWindow) {
                weakReference.get().onTakeSnapShot(bitmap);//弹窗
//                shareSnapshot(true, filePath);//最后一步处理分享 // 先不分享到 每日精彩
            }
            return new Pair<>(bitmap, filePath);
        }
    }


    private HotSeatStateMaintainer hotSeatStateMaintainer;

    public static final class HotSeatStateMaintainer {
        private static final String TAG = "HotSeatStateMaintainer";
        private WeakReference<CamLiveContract.View> viewWeakReference;
        private WeakReference<CamLiveContract.Presenter> presenterWeakReference;

        public HotSeatStateMaintainer(CamLiveContract.View view,
                                      CamLiveContract.Presenter presenter) {
            viewWeakReference = new WeakReference<>(view);
            presenterWeakReference = new WeakReference<>(presenter);
        }

        private volatile boolean micOn;
        private volatile boolean speakerOn;
        private boolean captureOn = true;

        private boolean filterRestore = true;

        /**
         * 停止播放了.
         */
        public void reset() {
            micOn = speakerOn = captureOn = false;
            if (viewWeakReference.get() != null) {
                viewWeakReference.get().switchHotSeat(false, false,
                        false, false,
                        false, false);
            }
            disableAudio();
        }

        /**
         *
         */
        private void disableAudio() {
            dump("disableAudio");
            setupLocalAudio(false, false);
//            Observable.just("reset")
//                    .subscribeOn(Schedulers.io())
//                    .subscribe(ret -> {
//                        if (presenterWeakReference.get() != null) {
//                            setupLocalAudio(false, false, false, false);
//                        }
//                    }, AppLogger::e);
        }

        private void dump(String tag) {
            Log.d(TAG, tag + ",micOn:" + micOn + ",speakerOn:" + speakerOn + ",captureOn:" + captureOn);
        }

        /**
         * 恢复三个按钮的状态.
         */
        public void restore() {
            filterRestore = true;
            if (viewWeakReference.get() != null && presenterWeakReference.get() != null) {
                int playType = presenterWeakReference.get().getPlayType();
                Observable.just("restoreAudio")
                        .subscribeOn(Schedulers.io())
                        .subscribe(ret -> {
                                    if (playType == TYPE_HISTORY) {
                                        micOn = false;
                                    }
                                    //设置客户端声音
                                    boolean result = setupLocalAudio(micOn, micOn || speakerOn);
                                    if (result) {
                                        //设置设备的声音
                                        setupRemoteAudio(micOn, micOn || speakerOn);
                                        //设置成功
                                    } else {
                                        micOn = false;
                                        setupRemoteAudio(micOn, micOn || speakerOn);
                                    }
                                    dump("restore?" + result);
                                    if (result || (!micOn && !speakerOn)) {
                                        viewWeakReference.get().switchHotSeat(micOn || speakerOn,
                                                !micOn,
                                                micOn,
                                                playType == TYPE_LIVE,
                                                captureOn, true);
                                    }
                                },
                                AppLogger::e);
            }
        }

        /**
         * 由于开始loading了.需要保存当前状态.
         */
        public void saveRestore() {
            filterRestore = false;
            if (viewWeakReference.get() != null && presenterWeakReference.get() != null) {
                viewWeakReference.get().switchHotSeat(false, false, false, false, false, false);
                dump("saveRestore");
                disableAudio();
            }
        }

        public void switchMic() {
            if (presenterWeakReference.get() != null && viewWeakReference.get() != null) {
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .flatMap(ret -> {
                            //当前状态,remoteSpeaker = localMic ,remoteMic=localSpeaker
                            micOn = !micOn;

                            //设置客户端声音
                            boolean result = setupLocalAudio(micOn, micOn || speakerOn);
                            if (result) {
                                //设置设备的声音
                                setupRemoteAudio(micOn, micOn || speakerOn);
                                //设置成功
//                                speakerOn = tmpSpeaker;
                            } else {
                                micOn = false;
                                setupRemoteAudio(micOn, micOn || speakerOn);
                            }
                            return Observable.just(result);
                        })
                        .filter(ret -> ret)
                        .filter(ret -> viewWeakReference.get() != null)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(ret -> {
                                    viewWeakReference.get().switchHotSeat(micOn || speakerOn, !micOn/*presenterWeakReference.get().getPlayType() == TYPE_LIVE*/,
                                            micOn,
                                            presenterWeakReference.get().getPlayType() == TYPE_LIVE,
                                            captureOn, true);
                                    dump("switchMic");
                                },
                                AppLogger::e);
            }
        }

        public void switchSpeaker() {
            if (presenterWeakReference.get() != null && viewWeakReference.get() != null) {
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .flatMap(ret -> {
                            //操作speaker的时候,本地的mic是关闭的.
                            speakerOn = !speakerOn;
                            boolean result = setupLocalAudio(micOn, speakerOn);
                            if (result) {
                                //设置设备的声音
                                setupRemoteAudio(micOn, speakerOn);
                                //说明已经有权限,并且设置成功
                                dump("switchSpeaker");
                                viewWeakReference.get().switchHotSeat(speakerOn, !micOn/*presenterWeakReference.get().getPlayType() == TYPE_LIVE*/,
                                        micOn,
                                        presenterWeakReference.get().getPlayType() == TYPE_LIVE,
                                        captureOn, true);
                            }
                            return Observable.just(result);
                        })
                        .filter(ret -> ret && viewWeakReference.get() != null)
                        .subscribe(ret -> {
                                },
                                AppLogger::e);
            }
        }


        // 参数与实际调用不一定是正确的 // modify
        private void setupRemoteAudio(boolean mic, boolean speaker) {
            if (presenterWeakReference.get().getLiveStream().playState == PLAY_STATE_PLAYING) {
                Command.getInstance().setAudio(false, speaker, mic);
                AppLogger.d(String.format(Locale.getDefault(), "remoteMic:%s,remoteSpeaker:%s", speaker, mic));
                AppLogger.d("切换远程:mic:" + speaker + ",speaker:" + mic);
            }
        }

        private boolean setupLocalAudio(boolean localMic, boolean localSpeaker) {
            AppLogger.d(String.format(Locale.getDefault(), "localMic:%s,localSpeaker:%s", localMic, localSpeaker));
            MediaRecorder mRecorder = null;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//这是为了兼容魅族4.4的权限
                try {
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.release();
                } catch (Exception e) {
                    AppLogger.d(e.getMessage());
                    if (mRecorder != null) {
                        mRecorder.release();
                    }
                    AndroidSchedulers.mainThread().createWorker().schedule(() -> {
                        if (viewWeakReference == null || viewWeakReference.get() == null) {
                            return;
                        }
                        if (viewWeakReference.get().isUserVisible()) {
                            viewWeakReference.get().audioRecordPermissionDenied();
                        }
                    });
                    return false;
                }
            } else {
                if (!PermissionUtils.hasSelfPermissions(viewWeakReference.get().getContext(), Manifest.permission.RECORD_AUDIO)) {
                    return false;
                }
            }
            AppLogger.d("切换本地:mic:" + localMic + ",speaker:" + localSpeaker);
            // 有视频直播中才能操作。
            if (presenterWeakReference.get().getLiveStream().playState == PLAY_STATE_PLAYING) {
                Command.getInstance().setAudio(true, localMic, localSpeaker);
            }
            if (presenterWeakReference.get().isEarpiecePlug()) {
                presenterWeakReference.get().switchEarpiece(true);
            }
            return true;
        }
    }

    @Override
    public void stop() {
        super.stop();
        SubscriptionSupervisor.unsubscribe("com.cylan.jiafeigou.misc.ver.DeviceVersionChecker", SubscriptionSupervisor.CATEGORY_DEFAULT, "DeviceVersionChecker.startCheck");
    }
}
