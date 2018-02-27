package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGHistoryVideoErrorInfo;
import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jfgapp.interfases.CallBack;
import com.cylan.jiafeigou.BuildConfig;
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
import com.cylan.jiafeigou.module.BellerSupervisor;
import com.cylan.jiafeigou.module.CameraLiveActionHelper;
import com.cylan.jiafeigou.module.CameraLiveHelper;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.module.DoorLockHelper;
import com.cylan.jiafeigou.module.HistoryManager;
import com.cylan.jiafeigou.module.HookerSupervisor;
import com.cylan.jiafeigou.module.Supervisor;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractFragmentPresenter;
import com.cylan.jiafeigou.push.BellPuller;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.APObserver;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.wheel.ex.DataExt;
import com.cylan.panorama.CameraParam;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_501_CAMERA_ALARM_FLAG;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLivePresenterImpl extends AbstractFragmentPresenter<CamLiveContract.View>
        implements CamLiveContract.Presenter, IFeedRtcp.MonitorListener, HistoryManager.HistoryObserver {
    private CameraLiveActionHelper liveActionHelper;
    /**
     * 帧率记录
     */
    private IFeedRtcp feedRtcp = new LiveFrameRateMonitor();
    private PanDeviceVersionChecker version;

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
        monitorBellLauncher();
    }

    private BellerSupervisor.BellerHooker bellerHooker = new BellerSupervisor.BellerHooker() {
        @Override
        protected void doHooker(@NotNull Supervisor.Action action, @NotNull BellerSupervisor.BellerParameter parameter) {
            performStopVideoActionInternal(CameraLiveHelper.isLive(liveActionHelper), true, () -> super.doHooker(action, parameter));
        }
    };

    private void monitorBellLauncher() {
        HookerSupervisor.addHooker(bellerHooker);
    }

    @Override
    public void start() {
        super.start();
        liveActionHelper.onUpdateDeviceInformation();
        HistoryManager.getInstance().addHistoryObserver(uuid, this);
        DataSourceManager.getInstance().syncAllProperty(uuid, 204, 222);
        monitorBattery();
        monitorRobotDataSync();
        monitorDeviceUnbind();
        monitorCheckNewVersionRsp();
        monitorSdcardFormatSub();

    }

    private void monitorVideoDisconnect() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                .retry()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGMsgVideoDisconn>() {
                    @Override
                    public void call(JFGMsgVideoDisconn jfgMsgVideoDisconn) {
                        Log.d(CameraLiveHelper.TAG, "monitorVideoDisconnect:" + jfgMsgVideoDisconn);
                        if (liveActionHelper.isPlaying) {
                            if (BuildConfig.DEBUG) {
                                Log.d(CameraLiveHelper.TAG, "monitorVideoDisconnect:" + jfgMsgVideoDisconn);
                            }
                            liveActionHelper.onVideoDisconnected(jfgMsgVideoDisconn);
                            int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                            if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                                performReportPlayError(playError);
                            }
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
                .retry()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGMsgVideoRtcp>() {
                    @Override
                    public void call(JFGMsgVideoRtcp jfgMsgVideoRtcp) {
                        if (!liveActionHelper.isPlaying) {//rtcp 可能停止不会很及时,导致退出页面后马上进入页面还有 rtcp 回调
                            return;
                        }
//                        AppLogger.d(CameraLiveHelper.TAG + ":live:" + (jfgMsgVideoRtcp.timestamp == 0));
                        boolean goodFrameRate = jfgMsgVideoRtcp.frameRate > 0;
                        if (goodFrameRate) {
                            if (!CameraLiveHelper.isLive(liveActionHelper) && jfgMsgVideoRtcp.timestamp > 0) {
                                boolean liveActionCompleted = liveActionHelper.onUpdatePendingPlayLiveActionCompleted();
                                if (!liveActionCompleted) {
                                    mView.onVideoPlayActionCompleted();
                                }
                            }
                        }
                        boolean videoPlaying = CameraLiveHelper.isVideoPlaying(liveActionHelper);
                        if (videoPlaying) {
                            liveActionHelper.onUpdateVideoRtcp(jfgMsgVideoRtcp);
                            feedRtcp.feed(jfgMsgVideoRtcp);
                            mView.onRtcp(jfgMsgVideoRtcp);
//                            boolean isVideoLive = CameraLiveHelper.checkIsVideoLiveWithRtcp(liveActionHelper, jfgMsgVideoRtcp);
//                            liveActionHelper.isDynamicLiving = isVideoLive;
//                            if (CameraLiveHelper.checkIsLiveTypeChanged(liveActionHelper, live)) {
//                                mView.onVideoPlayTypeChanged(liveActionHelper.isLive);
//                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e(CameraLiveHelper.TAG + ":RTCP ERROR:" + throwable.getMessage());
                        throwable.printStackTrace();
                        AppLogger.e(throwable);
                    }
                });
        addDestroySubscription(subscribe);
    }

    private void monitorHistoryVideoError() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(JFGHistoryVideoErrorInfo.class)
                .retry()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGHistoryVideoErrorInfo>() {
                    @Override
                    public void call(JFGHistoryVideoErrorInfo jfgHistoryVideoErrorInfo) {
                        AppLogger.d(CameraLiveHelper.TAG + ":monitorHistoryVideoError:" + jfgHistoryVideoErrorInfo);
                        liveActionHelper.onUpdateHistoryVideoError(jfgHistoryVideoErrorInfo);
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (CameraLiveHelper.shouldReportError(liveActionHelper, playError)) {
                            performReportPlayError(playError);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        AppLogger.e(throwable);
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        AppLogger.d(CameraLiveHelper.TAG + ":monitorHistoryVideoError:Action Error:" + throwable);
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                            performReportPlayError(playError);
                        }
                    }
                });
        addDestroySubscription(subscribe);
    }

    private void monitorVideoResolution() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                .filter(jfgMsgVideoResolution -> {
                    AppLogger.d(CameraLiveHelper.TAG + ":monitorVideoResolution:" + jfgMsgVideoResolution.peer);
                    return TextUtils.equals(jfgMsgVideoResolution.peer, uuid) && liveActionHelper.isPlaying;
                })
                .retry()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(jfgMsgVideoResolution -> {
                    AppLogger.d(CameraLiveHelper.TAG + ":monitorVideoResolution已经收到了分辨率消息了:" + jfgMsgVideoResolution);
                    liveActionHelper.onVideoResolutionReached(jfgMsgVideoResolution);
                    int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                    AppLogger.d(CameraLiveHelper.TAG + ":monitorVideoResolution,正在检查 playError:" + CameraLiveHelper.printError(playError));
                    if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                        performReportPlayError(playError);
                    } else {
                        try {
                            AppLogger.d(CameraLiveHelper.TAG + ":正在更新分辨率");
                            performBottomMenuRefresh();
                            mView.onResolution(jfgMsgVideoResolution);
                            mView.onVideoPlayActionCompleted();
                        } catch (Exception e) {
                            e.printStackTrace();
                            AppLogger.e(e);
                            liveActionHelper.playCode = CameraLiveHelper.PLAY_ERROR_JFG_EXCEPTION_THROW;
                            playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                            if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                                performReportPlayError(playError);
                                AppLogger.d(CameraLiveHelper.TAG + ":更新分辨率出现异常,正在检查 playError:" + playError);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                            performReportPlayError(playError);
                        }
                        throwable.printStackTrace();
                        AppLogger.e(throwable);
                    }
                });
        addDestroySubscription(subscribe);
    }

    private void performBottomMenuRefresh() {
        performUpdateBottomMenuEnable();
        performUpdateBottomMenuOn();
    }

    private void performReportPlayError(int playError) {
        boolean shouldReportError = CameraLiveHelper.shouldReportError(liveActionHelper, playError);
        AppLogger.d(CameraLiveHelper.TAG + ":performReportPlayError:" + CameraLiveHelper.printError(playError) + ",should report error:" + shouldReportError);
        Subscription schedule = AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                switch (playError) {
                    case CameraLiveHelper.PLAY_ERROR_STANDBY: {
                        if (shouldReportError) {
                            mView.onDeviceStandByChanged(true);
                        }
                        if (liveActionHelper.isPlaying) {
                            performStopVideoAction(false);
                        }
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_FIRST_SIGHT: {
                        if (shouldReportError) {
                            mView.onPlayErrorFirstSight();
                        }
                        if (liveActionHelper.isPlaying) {
                            performStopVideoAction(false);
                        }
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_NO_NETWORK: {
                        if (shouldReportError) {
                            mView.onPlayErrorNoNetwork();
                        }
                        if (liveActionHelper.isPlaying) {
                            performStopVideoAction(false);
                        }
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_DEVICE_OFF_LINE: {
                        if (shouldReportError) {
                            mView.onDeviceChangedToOffLine();
                        }
                        if (liveActionHelper.isPlaying) {
                            performStopVideoAction(false);
                        }
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_JFG_EXCEPTION_THROW: {
                        mView.onPlayErrorException();
                        if (liveActionHelper.isPlaying) {
                            performStopVideoAction(false);
                        }
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED: {
                        if (liveActionHelper.isPlaying) {
                            mView.onPlayErrorWaitForPlayCompleted();
                        }
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_BAD_FRAME_RATE: {
                        if (shouldReportError) {
                            mView.onPlayErrorBadFrameRate();
                        }
                        if (liveActionHelper.isPlaying && CameraLiveHelper.checkFrameBad(liveActionHelper)) {
                            performStopVideoAction(false);
                        }
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_LOW_FRAME_RATE: {
                        if (liveActionHelper.isPlaying) {
                            mView.onPlayErrorLowFrameRate();
                        }
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED_TIME_OUT: {
                        if (shouldReportError) {
                            mView.onPlayErrorWaitForPlayCompletedTimeout();
                        }
                        if (liveActionHelper.isPlaying) {
                            performStopVideoAction(false);
                        }
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_UN_KNOW_PLAY_ERROR: {
                        AppLogger.d(CameraLiveHelper.TAG + ":PLAY_ERROR_UN_KNOW_PLAY_ERROR:" + liveActionHelper.lastUnKnowPlayError);
                        if (liveActionHelper.isPlaying && shouldReportError) {
                            mView.onPlayErrorUnKnowPlayError(CameraLiveHelper.checkUnKnowErrorCode(liveActionHelper, false));
                        }
                        if (liveActionHelper.isPlaying) {
                            performStopVideoAction(false);
                        }
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_IN_CONNECTING: {
                        if (liveActionHelper.isPlaying) {
                            performStopVideoActionInternal(CameraLiveHelper.isLive(liveActionHelper), false, () -> {
                                mView.onPlayErrorInConnecting();
                            });
                        }
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_NO_ERROR: {
                        mView.onPlayErrorNoError();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_SD_FILE_IO: {
                        mView.onPlayErrorSDFileIO();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_SD_HISTORY_ALL: {
                        mView.onPlayErrorSDHistoryAll();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_SD_IO: {
                        mView.onPlayErrorSDIO();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_VIDEO_PEER_DISCONNECT: {
                        mView.onPlayErrorVideoPeerDisconnect();
                        if (liveActionHelper.isPlaying) {
                            performStopVideoAction(false);
                        }
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_VIDEO_PEER_NOT_EXIST: {
                        mView.onPlayErrorVideoPeerNotExist();
                        if (liveActionHelper.isPlaying) {
                            performStopVideoAction(false);
                        }
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_WAIT_FOR_FETCH_HISTORY_COMPLETED: {
                        mView.onPlayErrorWaitForFetchHistoryCompleted();
                    }
                    break;
                }
                liveActionHelper.lastReportedPlayError = playError;
            }
        });
        addDestroySubscription(schedule);
    }

    public void performUpdateBottomMenuEnable() {
        Subscription subscription = AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                boolean microphoneEnable = CameraLiveHelper.checkMicrophoneEnable(liveActionHelper);
                boolean speakerEnable = CameraLiveHelper.checkSpeakerEnable(liveActionHelper);
                boolean doorLockEnable = CameraLiveHelper.checkDoorLockEnable(liveActionHelper);
                boolean captureEnable = CameraLiveHelper.checkCaptureEnable(liveActionHelper);
                mView.onUpdateBottomMenuEnable(microphoneEnable, speakerEnable, doorLockEnable, captureEnable);
            }
        });
        addStopSubscription(subscription);
    }

    public void performUpdateBottomMenuOn() {
        Subscription subscription = AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                boolean microphoneOn = CameraLiveHelper.checkMicrophoneOn(liveActionHelper, liveActionHelper.isSpeakerOn);
                final boolean speakerOn = CameraLiveHelper.checkSpeakerOn(liveActionHelper, microphoneOn);
                mView.onUpdateBottomMenuOn(speakerOn, microphoneOn);
            }
        });
        addStopSubscription(subscription);
    }

    private void performPlayVideoActionInternal(boolean live, long timestamp, boolean notify) {
        Subscription subscribe = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                feedRtcp.stop();
                liveActionHelper.onVideoPlayPrepared(live);
                int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    //当前情况下不能播放
                    AppLogger.d(CameraLiveHelper.TAG + ":当前情况下无法开始直播:" + CameraLiveHelper.printError(playError));
                    performReportPlayError(playError);
                    subscriber.onCompleted();
                    return;
                }
                Subscription subscription = AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        mView.onVideoPlayPrepared(live);
                    }
                });
                subscriber.add(subscription);
                boolean shouldDisconnectFirst = CameraLiveHelper.shouldDisconnectFirst(liveActionHelper);
                AppLogger.d(CameraLiveHelper.TAG + ":updateVideoPlayType,isLive:" + live);
                if (shouldDisconnectFirst) {
                    //播放前需要先断开
                    AppLogger.d(CameraLiveHelper.TAG + ":播放前的断开直播过程:是否 Live:" + live);
                    performStopVideoActionInternal(live, false, new CompletedCallback() {
                        @Override
                        public void onCompleted() {
                            subscriber.onNext("停止直播已经结束");
                            subscriber.onCompleted();
                        }
                    });
                } else {
                    subscriber.onNext("当前无需停止直播,直接播放");
                    subscriber.onCompleted();
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .map(prepared -> {
                    AppLogger.d(CameraLiveHelper.TAG + ":将要开始播放视频了,isLive:" + live + ",timestamp:" + timestamp);
                    int playCode = liveActionHelper.playCode;
                    int playError;
                    do {
                        try {
                            if (CameraLiveHelper.shouldDisconnectWithPlayCode(liveActionHelper, playCode)) {
                                AppLogger.d(CameraLiveHelper.TAG + ":需要先断开下直播再播放,playCode:" + playCode);
                                Command.getInstance().stopPlay(uuid);
                            }
                            if (live) {
                                liveActionHelper.lastPlayTime = System.currentTimeMillis();
                                BellPuller.getInstance().currentCaller(uuid);//查看直播时禁止呼叫
                                playCode = Command.getInstance().playVideo(uuid);
                            } else {
                                liveActionHelper.lastPlayTime = CameraLiveHelper.LongTimestamp(timestamp);
                                BellPuller.getInstance().currentCaller(null);//查看直播时禁止呼叫
                                playCode = Command.getInstance().playHistoryVideo(uuid, liveActionHelper.lastPlayTime / 1000);
                            }
                        } catch (Exception e) {
                            playCode = CameraLiveHelper.PLAY_ERROR_JFG_EXCEPTION_THROW;
                            e.printStackTrace();
                            AppLogger.e(e);
                        }
                        AppLogger.d(CameraLiveHelper.TAG + ":正在更新 PlayCode:" + playCode);
                    }
                    while (CameraLiveHelper.shouldDisconnectWithPlayCode(liveActionHelper, playCode));
                    liveActionHelper.onVideoPlayStarted(live, playCode);
                    playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                    if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                        //开始播放历史视频或直播出现了错误
                        performReportPlayError(playError);
                    }
                    AppLogger.d(CameraLiveHelper.TAG + ":直播已经开始,正在等待直播完成,成功或超时?,playError is:" + CameraLiveHelper.printError(playError) + ",是否停止直播:" + prepared);
                    return prepared;
                })

                .delay(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        boolean pendingPlayActionCompleted = liveActionHelper.onUpdatePendingPlayLiveActionCompleted();
                        if (!pendingPlayActionCompleted) {
                            AppLogger.d(CameraLiveHelper.TAG + ":等了三十秒了还没播放成功");
                            liveActionHelper.onVideoPlayTimeOutReached();
                        }
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                            performReportPlayError(playError);
                        }
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
    public void performPlayVideoAction(boolean live, long timestamp) {
        performPlayVideoActionInternal(live, timestamp, true);
    }

    @Override
    public void performPlayVideoAction() {
        boolean live = CameraLiveHelper.isLive(liveActionHelper);
        long lastPlayTime = CameraLiveHelper.getLastPlayTime(live, liveActionHelper);
//        if (live) {
        performPlayVideoActionInternal(live, lastPlayTime, true);
//        } else {
//            performHistoryPlayAndCheckerAction(lastPlayTime);
//        }
    }


    private Observable<DpMsgDefine.DPSdStatus> performCheckSDCardErrorInternal() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                ArrayList<JFGDPMsg> params = new ArrayList<>();
                params.add(new JFGDPMsg(DpMsgMap.ID_204_SDCARD_STORAGE, 0, DpUtils.pack(0)));
                long seq = Command.getInstance().robotGetData(uuid, params, 1, false, 0);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class).first(rsp -> rsp.seq == seq))
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .map(robotoGetDataRsp -> {
                    JFGDPMsg msg = robotoGetDataRsp.map != null
                            && robotoGetDataRsp.map.containsKey(DpMsgMap.ID_204_SDCARD_STORAGE)
                            && robotoGetDataRsp.map.get(DpMsgMap.ID_204_SDCARD_STORAGE).size() > 0 ?
                            robotoGetDataRsp.map.get(DpMsgMap.ID_204_SDCARD_STORAGE).get(0) : null;
                    return DpUtils.unpackDataWithoutThrow(msg == null ? null : msg.packValue, DpMsgDefine.DPSdStatus.class, null);
                });
    }


    @Override
    public void performHistoryPlayAndCheckerAction(long playTime) {
        Subscription subscribe = Observable.create((Observable.OnSubscribe<DpMsgDefine.DPSdStatus>) subscriber -> {
            liveActionHelper.isPendingHistoryPlayActionCompleted = false;
            boolean isHistoryCheckerRequired = isHistoryEmpty();
            mView.onLoadHistoryPrepared(playTime, isHistoryCheckerRequired);
            if (isHistoryCheckerRequired) {
                Subscription subscription = performCheckSDCardErrorInternal().subscribe(errorCode -> {
                    subscriber.onNext(errorCode);
                    subscriber.onCompleted();
                }, throwable -> {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                });
                subscriber.add(subscription);
            } else {
                Device device = DataSourceManager.getInstance().getDevice(uuid);
                DpMsgDefine.DPSdStatus sdStatus = device.$(DpMsgMap.ID_204_SDCARD_STORAGE, new DpMsgDefine.DPSdStatus());
                liveActionHelper.onUpdateDeviceSDCardStatus(sdStatus);
                subscriber.onNext(liveActionHelper.deviceSDStatus);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sdStatus -> {
                    boolean hasSdcard = JFGRules.hasSdcard(sdStatus);
                    int errorCode = sdStatus == null ? -1 : sdStatus.err;
                    boolean historyPlayActionCompleted = liveActionHelper.isPendingHistoryPlayActionCompleted;
                    if (historyPlayActionCompleted) {
                        AppLogger.d(CameraLiveHelper.TAG + ":performHistoryPlayAndCheckerAction,playTime is:" + playTime + ",historyPlayActionCompleted:" + historyPlayActionCompleted);
                        return;
                    }
                    if (hasSdcard && isHistoryEmpty()) {
                        fetchHistoryDataListV2(uuid, (int) (TimeUtils.getTodayEndTime() / 1000), 1, 3, playTime);
                    } else if (hasSdcard) {
                        liveActionHelper.onUpdatePendingHistoryPlayActionCompleted();
                        performPlayVideoAction(false, playTime);
                    } else if (!hasSdcard) {
                        liveActionHelper.onUpdatePendingHistoryPlayActionCompleted();
                        mView.onHistoryCheckerErrorNoSDCard();
                    } else if (errorCode != 0) {
                        liveActionHelper.onUpdatePendingHistoryPlayActionCompleted();
                        mView.onHistoryCheckerErrorSDCardInitRequired(errorCode);
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    AppLogger.e(throwable);
                    liveActionHelper.onUpdatePendingHistoryPlayActionCompleted();
                    mView.onHistoryCheckerErrorNoSDCard();
                });
        addStopSubscription(subscribe);
    }

    private interface CompletedCallback {
        void onCompleted();
    }

    private void performStopVideoActionInternal(boolean live, boolean notify, CompletedCallback completedCallback) {
        Subscription subscribe = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                feedRtcp.stop();
                boolean stopLiveActionCompleted = liveActionHelper.isPendingStopLiveActionCompleted;
                boolean videoPlaying = liveActionHelper.isPlaying;
                boolean needlessStopAction = !videoPlaying || !stopLiveActionCompleted;
                liveActionHelper.onVideoStopPrepared(live);
                AppLogger.d(CameraLiveHelper.TAG + ":performStopVideoAction,live:" + live + ",是否无需再停止播放:" + needlessStopAction +
                        ",是否正在播放:" + videoPlaying + ",是否正在停止:" + !stopLiveActionCompleted + ",是否通知 UI 更新:" + notify);
                if (needlessStopAction) {
                    liveActionHelper.onVideoPlayStopped(live, liveActionHelper.playCode);
                    subscriber.onCompleted();
                    return;
                }
                BellPuller.getInstance().currentCaller(null);//查看直播时禁止呼叫
                int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                AppLogger.d(CameraLiveHelper.TAG + "断开播放前,先看下有没有错误:" + CameraLiveHelper.printError(playError));
                if (playError == CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    AppLogger.d(CameraLiveHelper.TAG + ":正在保存直播截图");
                    performLivePictureCaptureSaveActionInternal(false, () -> {
                        subscriber.onNext("截图已经成功");
                        subscriber.onCompleted();
                    });
                } else {
                    subscriber.onNext("无法截图,直接结束直播");
                    subscriber.onCompleted();
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .map(prepared -> {
                    AppLogger.d(CameraLiveHelper.TAG + ":截图是否已执行:" + prepared);
                    int playCode;
                    try {
                        playCode = Command.getInstance().stopPlay(uuid);
                    } catch (Exception e) {
                        playCode = CameraLiveHelper.PLAY_ERROR_JFG_EXCEPTION_THROW;
                        e.printStackTrace();
                        AppLogger.e(e);
                    }
                    AppLogger.d(CameraLiveHelper.TAG + ":停止直播是否成功呢: playCode is:" + CameraLiveHelper.printError(playCode));
                    int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                    liveActionHelper.onVideoPlayStopped(live, playCode);
                    if (CameraLiveHelper.shouldReportError(liveActionHelper, playError)) {
                        performReportPlayError(playError);
                    }

                    return prepared;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (CameraLiveHelper.shouldReportError(liveActionHelper, playError)) {
                            performReportPlayError(playError);
                        }
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        if (notify) {
                            mView.onVideoPlayStopped(live);
                        }
                        performUpdateBottomMenuEnable();
                        if (completedCallback != null) {
                            completedCallback.onCompleted();
                        }
                    }
                });
        addDestroySubscription(subscribe);
    }

    @Override
    public void performCheckVideoPlayError() {
        performReportPlayError(CameraLiveHelper.checkPlayError(liveActionHelper));
    }

    @Override
    public void performStopVideoAction(boolean notify) {
        performStopVideoActionInternal(CameraLiveHelper.isLive(liveActionHelper), notify, null);
    }


    @Override
    public void performLivePictureCaptureSaveAction(boolean saveInPhotoAndNotify) {
        performLivePictureCaptureSaveActionInternal(saveInPhotoAndNotify, null);
    }

    public void performLivePictureCaptureSaveActionInternal(boolean saveInPhotoAndNotify, CompletedCallback completedCallback) {
        if (!CameraLiveHelper.isVideoPlaying(liveActionHelper)) {
            liveActionHelper.onUpdatePendingCaptureActionCompleted();
            AppLogger.d(CameraLiveHelper.TAG + ":当前没有开始播放,无法截取缩略图, notify:" + saveInPhotoAndNotify);
            return;
        }
        Subscription subscribe = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                liveActionHelper.isPendingCaptureActionCompleted = false;
                Command.getInstance().screenshot(false, new CallBack<Bitmap>() {
                    @Override
                    public void onSucceed(Bitmap bitmap) {
                        AppLogger.d(CameraLiveHelper.TAG + ":截图已经成功了, bitmap 是否为空:" + (bitmap == null));
                        if (bitmap != null) {
                            liveActionHelper.onUpdateLastLiveThumbPicture(liveActionHelper, bitmap);
                        }
                        liveActionHelper.onUpdatePendingCaptureActionCompleted();
                        if (completedCallback != null) {
                            completedCallback.onCompleted();
                        }
                        subscriber.onNext(bitmap);
                        subscriber.onCompleted();
                        if (bitmap != null) {
                            final String cover = JConstant.MEDIA_PATH + File.separator + uuid + "_cover.png";
//                            byte[] bytes = CameraLiveHelper.putCache(liveActionHelper, bitmap, true);
//                            if (bytes != null) {
//                                BitmapUtils.saveByteArray2File(bytes, cover);
                            BitmapUtils.saveBitmap2file(bitmap, cover);
                            PreferencesUtils.putString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, System.currentTimeMillis() + "");
                            if (saveInPhotoAndNotify) {
                                final String fileName = uuid + System.currentTimeMillis() + ".png";
                                final String filePath = JConstant.MEDIA_PATH + File.separator + fileName;
//                                    BitmapUtils.saveByteArray2File(bytes, filePath);
                                BitmapUtils.saveBitmap2file(bitmap, filePath);
                            }
//                            }
                        }
                    }

                    @Override
                    public void onFailure(String s) {
                        AppLogger.d(CameraLiveHelper.TAG + ":截图失败了:" + s);
                        liveActionHelper.isPendingCaptureActionCompleted = true;
                        if (completedCallback != null) {
                            completedCallback.onCompleted();
                        }
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                    }
                });
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        if (saveInPhotoAndNotify) {
                            mView.onCaptureFinished(bitmap);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        AppLogger.e(throwable);
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                            performReportPlayError(playError);
                        }
                    }
                });
        addDestroySubscription(subscribe);
    }

    private void performDeviceInfoChangedAction(ArrayList<JFGDPMsg> dpList) {
        for (JFGDPMsg msg : dpList) {
            switch ((int) msg.id) {
                case DpMsgMap.ID_222_SDCARD_SUMMARY: {
                    AppLogger.d(CameraLiveHelper.TAG + ":performDeviceInfoChangedAction:设备SD发生变化:222");
                    DpMsgDefine.DPSdcardSummary sdStatus = DpUtils.unpackDataWithoutThrow(msg.packValue, DpMsgDefine.DPSdcardSummary.class, null);
                    if (sdStatus != null) {
                        decideReportDevice222Event(sdStatus);
                    }
                }
                break;
                case DpMsgMap.ID_204_SDCARD_STORAGE: {
                    DpMsgDefine.DPSdStatus dpSdStatus = DpUtils.unpackDataWithoutThrow(msg.packValue, DpMsgDefine.DPSdStatus.class, null);
                    if (dpSdStatus != null) {
                        decideReportDevice204Event(dpSdStatus);
                    }
                }
                break;
                case DpMsgMap.ID_206_BATTERY: {
                    AppLogger.d(CameraLiveHelper.TAG + ":performDeviceInfoChangedAction:设备电量发生变化:206");
                    Integer battery = DpUtils.unpackDataWithoutThrow(msg.packValue, Integer.class, 0);
                    decideReportDevice206Event(battery);
                }
                break;
                case DpMsgMap.ID_508_CAMERA_STANDBY_FLAG: {
                    AppLogger.d(CameraLiveHelper.TAG + ":performDeviceInfoChangedAction:设备待机状态发生变化:508");
                    DpMsgDefine.DPStandby standby = DpUtils.unpackDataWithoutThrow(msg.packValue, DpMsgDefine.DPStandby.class, null);
                    if (standby != null) {
                        decideReportDevice508Event(standby);
                    }
                }
                break;
                case DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD: {
                    AppLogger.d(CameraLiveHelper.TAG + ":performDeviceInfoChangedAction:设备SD 卡已被格式化:218");
                    Integer formatted = DpUtils.unpackDataWithoutThrow(msg.packValue, int.class, 0);
                    decideReportDevice218Event(formatted);
                }
                break;
                case DpMsgMap.ID_509_CAMERA_MOUNT_MODE: {
                    AppLogger.d(CameraLiveHelper.TAG + ":performDeviceInfoChangedAction:设备MountMode发生变化了:509");
                    String _509 = DpUtils.unpackDataWithoutThrow(msg.packValue, String.class, "1");
                    Device device = DataSourceManager.getInstance().getDevice(uuid);
                    if (device.pid == 39 || device.pid == 49) {
                        _509 = "0";
                    }
                    if (!TextUtils.isEmpty(_509)) {
                        decideReportDevice509Event(_509);
                    }
                }
                break;
                case DpMsgMap.ID_201_NET: {
                    AppLogger.d(CameraLiveHelper.TAG + ":performDeviceInfoChangedAction:设备离线了:201");
                    DpMsgDefine.DPNet dpNet = DpUtils.unpackDataWithoutThrow(msg.packValue, DpMsgDefine.DPNet.class, null);
                    if (dpNet != null) {
                        decideReportDevice201Event(dpNet);
                    }
                }
                break;
                case DpMsgMap.ID_214_DEVICE_TIME_ZONE: {
                    AppLogger.d(CameraLiveHelper.TAG + ":performDeviceInfoChangedAction:设备时区发生了变化:214");
                    DpMsgDefine.DPTimeZone dpTimeZone = DpUtils.unpackDataWithoutThrow(msg.packValue, DpMsgDefine.DPTimeZone.class, null);
                    if (dpTimeZone != null) {
                        decideReportDevice214Event(dpTimeZone);
                    }
                }
                break;
                case DpMsgMap.ID_510_CAMERA_COORDINATE: {
                    AppLogger.d(CameraLiveHelper.TAG + ":performDeviceInfoChangedAction:设备视频参数发生变化:510");
                    DpMsgDefine.DpCoordinate dpCoordinate = DpUtils.unpackDataWithoutThrow(msg.packValue, DpMsgDefine.DpCoordinate.class, null);
                    if (dpCoordinate != null) {
                        decideReportDevice510Event(dpCoordinate);
                    }
                }
                break;
                case DpMsgMap.ID_501_CAMERA_ALARM_FLAG: {
                    AppLogger.d(CameraLiveHelper.TAG + ":performDeviceInfoChangedAction:设备报警设置发生变化:501");
                    Boolean alarmOpen = DpUtils.unpackDataWithoutThrow(msg.packValue, boolean.class, false);
                    decideReportDevice501Action(alarmOpen);
                }
                break;
            }
        }
    }

    private void decideReportDevice204Event(DpMsgDefine.DPSdStatus dpSdStatus) {
        boolean isSDCardExist = liveActionHelper.onUpdateDeviceSDCardStatus(dpSdStatus);
        if (CameraLiveHelper.checkIsDeviceSDCardExistStateChanged(liveActionHelper, isSDCardExist)) {
            if (!liveActionHelper.isSDCardExist) {
                AppLogger.d(CameraLiveHelper.TAG + ":SD 卡已被拔出");
                mView.onDeviceSDCardOut();
                if (CameraLiveHelper.isVideoRealPlaying(liveActionHelper) && !CameraLiveHelper.isLive(liveActionHelper)) {
                    performStopVideoAction(false);
                }
            }
        }
    }

    private void decideReportDevice501Action(Boolean alarmOpen) {
        alarmOpen = liveActionHelper.onUpdateDeviceAlarmOpenState(alarmOpen);
        if (CameraLiveHelper.checkIsDeviceAlarmOpenStateChanged(liveActionHelper, alarmOpen)) {
            mView.onUpdateAlarmOpenChanged(liveActionHelper.isDeviceAlarmOpened);
        }
    }

    private void decideReportDevice510Event(DpMsgDefine.DpCoordinate dpCoordinate) {
        dpCoordinate = liveActionHelper.onUpdateDeviceCoordinate(dpCoordinate);
        if (CameraLiveHelper.checkIsDeviceCoordinateChanged(liveActionHelper, dpCoordinate)) {
            mView.onUpdateCameraCoordinate(dpCoordinate);
        }
    }

    private void decideReportDevice214Event(DpMsgDefine.DPTimeZone dpTimeZone) {
        dpTimeZone = liveActionHelper.onUpdateDeviceTimezone(dpTimeZone);
        if (CameraLiveHelper.checkIsDeviceTimeZoneChanged(liveActionHelper, dpTimeZone)) {
            mView.onDeviceTimeZoneChanged(liveActionHelper.deviceTimezone.offset);
        }
    }

    private void decideReportDevice201Event(DpMsgDefine.DPNet dpNet) {
        DpMsgDefine.DPNet preNet = liveActionHelper.onUpdateDeviceNet(dpNet);
        boolean netChanged = CameraLiveHelper.checkIsDeviceNetChanged(liveActionHelper, preNet);
        boolean deviceOnline = JFGRules.isDeviceOnline(dpNet);
        boolean preOnline = JFGRules.isDeviceOnline(preNet);
        AppLogger.d(CameraLiveHelper.TAG + ":decideReportDevice201Event,netChanged:" + netChanged + ",isOnline now:" + deviceOnline + ",isOnline pre:" + preOnline);
        if (netChanged) {
            liveActionHelper.onUpdatePendingHistoryPlayActionCompleted();
            liveActionHelper.onUpdateLive(deviceOnline || liveActionHelper.isLive);
            mView.onDeviceNetChanged(liveActionHelper.deviceNet, liveActionHelper.isLocalOnline);
            if (deviceOnline) {
                mView.onDeviceChangedToOnline();
            } else {
                mView.onDeviceChangedToOffLine();
                performStopVideoAction(false);
            }
        }
    }

    private void decideReportDevice509Event(String _509) {
        _509 = liveActionHelper.onUpdateDeviceMountMode(_509);
        if (CameraLiveHelper.checkIsDeviceViewMountModeChanged(liveActionHelper, _509)) {
            mView.onUpdateLiveViewMode(_509);
        }
    }

    private void decideReportDevice218Event(Integer formatted) {
        boolean isSDCardFormatted = liveActionHelper.onUpdateSDCardFormatted(formatted);
        if (isSDCardFormatted != liveActionHelper.isSDCardFormatted) {
            if (liveActionHelper.isSDCardFormatted) {
                mView.onDeviceSDCardFormat();
            }
        }
    }

    private void decideReportDevice508Event(DpMsgDefine.DPStandby standby) {
        boolean isStandBy = liveActionHelper.onUpdateStandBy(JFGRules.isStandBy(standby));
        if (isStandBy != liveActionHelper.isStandBy) {
            mView.onDeviceStandByChanged(liveActionHelper.isStandBy);
            if (liveActionHelper.isStandBy) {
                performStopVideoAction(false);
            } else if (CameraLiveHelper.shouldResumeToPlayVideo(liveActionHelper)) {
                performPlayVideoAction();
            }
        }
    }

    private void decideReportDevice206Event(Integer battery) {
        battery = liveActionHelper.onUpdateDeviceBattery(battery);
        if (CameraLiveHelper.checkIsDeviceBatteryChanged(liveActionHelper, battery)) {
            if (JFGRules.popPowerDrainOut(getDevice().pid)) {
                if (battery <= 20 && getDevice().$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet()).net > 0) {
                    mView.onBatteryDrainOut();
                }
            }
        }
    }

    private void decideReportDevice222Event(DpMsgDefine.DPSdcardSummary sdStatus) {
        boolean isSDCardExist = liveActionHelper.onUpdateDeviceSDCardStatus(sdStatus);
        if (CameraLiveHelper.checkIsDeviceSDCardExistStateChanged(liveActionHelper, isSDCardExist)) {
            if (!liveActionHelper.isSDCardExist) {
                AppLogger.d(CameraLiveHelper.TAG + ":SD 卡已被拔出");
                mView.onDeviceSDCardOut();
                if (CameraLiveHelper.isVideoRealPlaying(liveActionHelper) && !CameraLiveHelper.isLive(liveActionHelper)) {
                    performStopVideoAction(false);
                }
            }
        }
    }

    @Override
    public void performChangeSpeakerAction(boolean speakerOn) {
        Subscription subscription = Schedulers.io().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                liveActionHelper.onUpdateSpeakerOn(speakerOn);
                boolean microphoneOn = CameraLiveHelper.checkMicrophoneOn(liveActionHelper, speakerOn);
                Command.getInstance().setAudio(true, microphoneOn, speakerOn);
                Command.getInstance().setAudio(false, speakerOn, microphoneOn);
                switchEarpiece(isEarpiecePlug());
                performBottomMenuRefresh();
                AppLogger.d(CameraLiveHelper.TAG + ":performChangeSpeakerAction,speakerOn:" + speakerOn + ",microphoneOn:" + microphoneOn);
            }
        });
        addStopSubscription(subscription);
    }

    @Override
    public void performChangeSpeakerAction() {
        performChangeSpeakerAction(!CameraLiveHelper.checkSpeakerOn(liveActionHelper, liveActionHelper.isMicrophoneOn));
    }

    @Override
    public void performChangeMicrophoneAction(boolean microphoneOn) {
        Subscription subscription = Schedulers.io().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                liveActionHelper.onUpdateMicrophoneOn(microphoneOn);
                boolean speakerOn = CameraLiveHelper.checkSpeakerOn(liveActionHelper, microphoneOn);
                Command.getInstance().setAudio(true, microphoneOn, speakerOn);
                Command.getInstance().setAudio(false, speakerOn, microphoneOn);
                switchEarpiece(isEarpiecePlug());
                performBottomMenuRefresh();
                AppLogger.d(CameraLiveHelper.TAG + ":performChangeMicrophoneAction,speakerOn:" + speakerOn + ",microphoneOn:" + microphoneOn);
            }
        });
        addStopSubscription(subscription);
    }

    @Override
    public void performChangeMicrophoneAction() {
        performChangeMicrophoneAction(!liveActionHelper.isMicrophoneOn);
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
                    performStopVideoAction(true);
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
        if (version != null) {
            version.clean();
        }
        version = new PanDeviceVersionChecker();
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

    @Override
    public float getVideoPortHeightRatio(boolean isLand) {
        AppLogger.d("获取分辨率?");
        return CameraLiveHelper.checkVideoRadio(liveActionHelper, isLand);

    }

    @Override
    public void fetchHistoryDataListV1(String uuid, long playTime) {
        Runnable runnable = () -> HistoryManager.getInstance().fetchHistoryV1(uuid);
        fetchHistoryDataListCompat(runnable, playTime);
    }

    @Override
    public void fetchHistoryDataListV2(String uuid, int time, int way, int count, long playTime) {
        Runnable runnable = () -> HistoryManager.getInstance().fetchHistoryV2(uuid, time, way, count);
        fetchHistoryDataListCompat(runnable, playTime);
    }


    private void fetchHistoryDataListCompat(Runnable runnable, long playTime) {
        Subscription subscription = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                if (playTime >= 0) {
                    liveActionHelper.lastPlayTime = playTime;
                }
                performStopVideoActionInternal(CameraLiveHelper.isLive(liveActionHelper), false, () -> {
                    subscriber.onNext("停止直播已完成");
                    subscriber.onCompleted();
                });
                AppLogger.d(CameraLiveHelper.TAG + ":获取历史录像,先断开直播,或者历史录像");
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(prepared -> {
                    liveActionHelper.isPendingHistoryPlayActionCompleted = false;
                    int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                    if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                        performReportPlayError(playError);
                    }
                    runnable.run();
                    AppLogger.d(CameraLiveHelper.TAG + ":fetchHistoryDataListCompat 已经开始获取历史视频了,正在等待成功或者超时");
                    return prepared;
                })
                .delay(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        boolean historyPlayActionCompleted = liveActionHelper.onUpdatePendingHistoryPlayActionCompleted();
                        AppLogger.d(CameraLiveHelper.TAG + ":fetchHistoryDataListCompat 30秒已经过去了:" + historyPlayActionCompleted);
                        if (!historyPlayActionCompleted) {
                            liveActionHelper.onUpdateLive(true);
                            mView.onLoadHistoryFailed();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        AppLogger.e(throwable);
                        boolean historyPlayActionCompleted = liveActionHelper.onUpdatePendingHistoryPlayActionCompleted();
                        if (!historyPlayActionCompleted) {
                            liveActionHelper.onUpdateLive(true);
                            mView.onLoadHistoryFailed();
                        }
                    }
                });
        addStopSubscription(subscription);
    }

    @Override
    public void openDoorLock(String password) {
        if (!CameraLiveHelper.isVideoRealPlaying(liveActionHelper)) {
            //还没有开始直播,则需要开始直播
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
    public boolean isLive() {
        return CameraLiveHelper.isLive(liveActionHelper);
    }

    @Override
    public boolean isLivePlaying() {
        return CameraLiveHelper.isVideoRealPlaying(liveActionHelper);
    }

    @Override
    public boolean isLoading() {
        return CameraLiveHelper.isVideoLoading(liveActionHelper);
    }

    @Override
    public boolean canShowLoadingBar() {
        return !CameraLiveHelper.isNoError(liveActionHelper) || isLoading() || !MiscUtils.isLand();
    }

    @Override
    public boolean canHideLoadingBar() {
        return CameraLiveHelper.isNoError(liveActionHelper) && !isLoading() && liveActionHelper.isPendingHistoryPlayActionCompleted && (isLivePlaying() || MiscUtils.isLand());
    }

    @Override
    public boolean canShowViewModeMenu() {
        return CameraLiveHelper.canShowViewModeMenu(liveActionHelper);
    }

    @Override
    public boolean canShowStreamSwitcher() {
        return CameraLiveHelper.canShowStreamSwitcher(liveActionHelper);
    }

    @Override
    public boolean canShowXunHuan() {
        return CameraLiveHelper.canShowXunHuan(liveActionHelper);
    }

    @Override
    public boolean canShowHistoryWheel() {
        return CameraLiveHelper.canShowHistoryWheel(liveActionHelper);
    }

    @Override
    public boolean canPlayVideoNow() {
        return CameraLiveHelper.canPlayVideoNow(liveActionHelper);
    }

    @Override
    public boolean canShowDoorLock() {
        return CameraLiveHelper.canShowDoorLock(liveActionHelper);
    }

    @Override
    public boolean canShowMicrophone() {
        return CameraLiveHelper.canShowMicrophone(liveActionHelper);
    }

    @Override
    public boolean canStreamSwitcherEnable() {
        return CameraLiveHelper.canStreamSwitcherEnable(liveActionHelper);
    }

    @Override
    public boolean isVideoLoading() {
        return CameraLiveHelper.isVideoLoading(liveActionHelper);
    }

    @Override
    public boolean canCaptureEnable() {
        return CameraLiveHelper.checkCaptureEnable(liveActionHelper);
    }

    @Override
    public boolean canMicrophoneEnable() {
        return CameraLiveHelper.checkMicrophoneEnable(liveActionHelper);
    }

    @Override
    public boolean canSpeakerEnable() {
        return CameraLiveHelper.checkSpeakerEnable(liveActionHelper);
    }

    @Override
    public boolean isNoPlayError() {
        return CameraLiveHelper.isNoError(liveActionHelper);
    }

    @Override
    public int getDisplayMode() {
        return CameraLiveHelper.checkViewDisplayMode(liveActionHelper);
    }

    @Override
    public int getMountMode() {
        return CameraLiveHelper.checkViewMountMode(liveActionHelper);
    }

    @Override
    public CameraParam getCameraParam() {
        return null;
    }

    @Override
    public boolean canHideStreamSwitcher() {
        return CameraLiveHelper.canHideStreamSwitcher(liveActionHelper);
    }

    @Override
    public boolean canHideViewModeMenu() {
        return CameraLiveHelper.canHideViewMode(liveActionHelper);
    }

    @Override
    public boolean isSafeProtectionOpened() {
        return CameraLiveHelper.isDeviceAlarmOpened(liveActionHelper);
    }

    @Override
    public int getStreamMode() {
        return CameraLiveHelper.checkViewStreamMode(liveActionHelper);
    }

    @Override
    public boolean canShowFlip() {
        return CameraLiveHelper.canShowFlip(liveActionHelper);
    }

    @Override
    public boolean canShowFirstSight() {
        return CameraLiveHelper.isFirstSight(liveActionHelper);
    }

    @Override
    public boolean canDoorLockEnable() {
        return CameraLiveHelper.checkDoorLockEnable(liveActionHelper);
    }

    @Override
    public boolean canShowHistoryCase() {
        return CameraLiveHelper.canShowHistoryCase(liveActionHelper) && canShowHistoryWheel() && !isHistoryEmpty() && !isLive();
    }

    @Override
    public boolean canLoadHistoryEnable() {
        return CameraLiveHelper.canLoadHistoryEnable(liveActionHelper);
    }

    @Override
    public boolean canModeSwitchEnable() {
        return CameraLiveHelper.canModeSwitchEnable(liveActionHelper);
    }

    @Override
    public boolean canXunHuanEnable() {
        return CameraLiveHelper.canXunHuanEnable(liveActionHelper);
    }

    @Override
    public boolean isStandBy() {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        DpMsgDefine.DPStandby standby = device.$(508, new DpMsgDefine.DPStandby());
        return standby.standby;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    /**
     * 高清,标清,自动模式切换
     *
     * @param mode
     * @return
     */
    @Override
    public void performChangeStreamModeAction(int mode) {
        Subscription subscribe = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                int streamMode = liveActionHelper.onUpdateDeviceStreamMode(mode);
                boolean streamModeChanged = CameraLiveHelper.checkIsDeviceStreamModeChanged(liveActionHelper, streamMode);
                if (streamModeChanged) {
                    updateInfoReq(new DpMsgDefine.DPPrimary<>(mode), DpMsgMap.ID_513_CAM_RESOLUTION);
                }
                subscriber.onNext(streamModeChanged);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean streamModeChanged) {
                        if (streamModeChanged) {
                            mView.onStreamModeChanged(mode);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e(throwable);
                        AppLogger.e(CameraLiveHelper.TAG + ":performChangeStreamModeAction error:" + throwable.getMessage());
                    }
                });
        addStopSubscription(subscribe);
    }

    @Override
    public void performLoadLiveThumbPicture() {
        Subscription subscribe = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Bitmap lastLiveThumbPicture = CameraLiveHelper.checkLastLiveThumbPicture(liveActionHelper);
                subscriber.onNext(lastLiveThumbPicture);
                subscriber.onCompleted();
                boolean isThumbPictureChanged = CameraLiveHelper.checkIsThumbPictureChanged(liveActionHelper);
                AppLogger.d(CameraLiveHelper.TAG + ":是否有可用的预览图片:" + lastLiveThumbPicture);
//                if (isThumbPictureChanged) {
//                    CameraLiveHelper.putCache(liveActionHelper, lastLiveThumbPicture, true);
//                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        boolean isPanoramaView = CameraLiveHelper.checkIsPanoramaView(liveActionHelper);
                        if (isPanoramaView) {
                            mView.onUpdatePanoramaThumbPicture(bitmap);
                        } else {
                            mView.onUpdateNormalThumbPicture(bitmap);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        AppLogger.e(throwable);
                        mView.onUpdateNormalThumbPicture(null);
                    }
                });
        addStopSubscription(subscribe);
    }

    @Override
    public void performResetFirstSight() {
        CameraLiveHelper.resetFirstSight(liveActionHelper);
    }

    @Override
    public void performLocalNetworkPingAction() {
        if (JFGRules.shouldObserverAP()) {//需要监听是否局域网在线
            Subscription subscribe = APObserver.scan(uuid)
                    .timeout(5, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ret -> {
                        boolean localOnlineState = liveActionHelper.onUpdateDeviceLocalOnlineState(true);
                        boolean localOnlineChanged = CameraLiveHelper.checkIsDeviceLocalOnlineChanged(liveActionHelper, localOnlineState);
                        if (localOnlineChanged) {
                            mView.onDeviceNetChanged(liveActionHelper.deviceNet, liveActionHelper.isLocalOnline);
                        }
                    }, e -> {
                        e.printStackTrace();
                        AppLogger.e(e);
                        boolean localOnlineState = liveActionHelper.onUpdateDeviceLocalOnlineState(false);
                        boolean localOnlineChanged = CameraLiveHelper.checkIsDeviceLocalOnlineChanged(liveActionHelper, localOnlineState);
                        if (localOnlineChanged) {
                            mView.onDeviceNetChanged(liveActionHelper.deviceNet, liveActionHelper.isLocalOnline);
                        }
                    });
            addStopSubscription(subscribe);
        }
    }

    @Override
    public void performChangeSafeProtection(int event) {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        DpMsgDefine.DPSdStatus dpSdStatus = device.$(DpMsgMap.ID_204_SDCARD_STORAGE, new DpMsgDefine.DPSdStatus());
        boolean autoRecordEnabled = device.$(ID_303_DEVICE_AUTO_VIDEO_RECORD, -1) > 0;
        boolean safeProtectionOpened = device.$(ID_501_CAMERA_ALARM_FLAG, false);
        boolean hasSDCard = JFGRules.hasSdcard(dpSdStatus);
        //先判断是否关闭了自动录像,关闭了提示 :若关闭，“侦测到异常时”将不启用录像
        //若自动录像未关闭 则提示:关闭“移动侦测”，将停止“侦测报警录像”
        //无卡不需要显示 //oldOption 不等于2 说明没有关闭自动录像则提示:关闭“移动侦测”，将停止“侦测报警录像”
        if (event == 0 && safeProtectionOpened && !autoRecordEnabled && hasSDCard) {
            mView.onChangeSafeProtectionErrorAutoRecordClosed();
        } else if (event == 0 && safeProtectionOpened) {
            mView.onChangeSafeProtectionErrorNeedConfirm();
        } else {
            //之前未开启,则开启
            safeProtectionOpened = !safeProtectionOpened;
            DpMsgDefine.DPPrimary<Boolean> safe = new DpMsgDefine.DPPrimary<>(safeProtectionOpened);
            updateInfoReq(safe, ID_501_CAMERA_ALARM_FLAG);
            mView.onSafeProtectionChanged(safeProtectionOpened);
        }
    }

    @Override
    public void performViewModeChecker(int displayMode) {
        int viewModeError = CameraLiveHelper.checkViewModeError(liveActionHelper);
        if (viewModeError == 0) {
            liveActionHelper.onUpdateDeviceDisplayMode(displayMode);
            mView.onViewModeAvailable(displayMode);
        } else if (viewModeError == 1) {
            mView.onViewModeHangError();
        } else if (viewModeError == 2) {
            mView.onViewModeForceHangError(displayMode);
        } else if (viewModeError == 3) {
            mView.onViewModeNotSupportError();
        }
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
        feedRtcp.stop();
        liveActionHelper.onUpdateVideoFrameFailed();
        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
            performReportPlayError(playError);
        } else if (CameraLiveHelper.checkFrameBad(liveActionHelper)) {
            //说明当前是正常播放状态下 FrameFailed
            performStopVideoAction(false);
        }
    }

    @Override
    public void onFrameRate(boolean slow) {
        boolean frameSlow = CameraLiveHelper.checkFrameSlow(liveActionHelper, slow);
        AppLogger.d(CameraLiveHelper.TAG + ":onFrameRate:slow:" + slow + ",preSlow:" + frameSlow);
        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
            performReportPlayError(playError);
        } else if (!slow) {
            mView.onPlayFrameResumeGood();
        }
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{ConnectivityManager.CONNECTIVITY_ACTION,
                NETWORK_STATE_CHANGED_ACTION};
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION) || TextUtils.equals(action, NETWORK_STATE_CHANGED_ACTION)) {
            boolean networkConnected = NetUtils.getJfgNetType() != 0;
            boolean preNetworkConnected = liveActionHelper.onUpdateNetWorkChangedAction(networkConnected);
            if (networkConnected != preNetworkConnected) {
                int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    performReportPlayError(playError);
                } else if (liveActionHelper.isNetworkConnected) {
                    mView.onNetworkResumeGood();
                }
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (version != null) {
            version.clean();
        }
        liveActionHelper.onUpdateLastLiveThumbPicture(liveActionHelper, null);
        HistoryManager.getInstance().removeHistoryObserver(uuid, this);
    }

    @Override
    public void destroy() {
        super.destroy();
        HookerSupervisor.removeHooker(bellerHooker);
    }

    @Override
    public void onHistoryChanged(Collection<JFGVideo> history) {
        AppLogger.d(CameraLiveHelper.TAG + ":onHistoryChanged:" + history);
        if (mView == null) {
            return;
        }
        Subscription schedule = AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                boolean isHistoryEmpty = history == null || history.size() == 0;
                mView.onHistoryReached(history, isHistoryEmpty); //是否有历史视频
                boolean historyPlayActionCompleted = liveActionHelper.onUpdatePendingHistoryPlayActionCompleted();
                if (!historyPlayActionCompleted) {
                    performPlayVideoActionInternal(isHistoryEmpty, CameraLiveHelper.LongTimestamp(Math.max(0, liveActionHelper.lastPlayTime)), false);
                }
            }
        });
        addStopSubscription(schedule);
    }
}
