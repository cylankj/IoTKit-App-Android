package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGHistoryVideoErrorInfo;
import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.entity.jniCall.JFGVideo;
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
import com.cylan.jiafeigou.module.CameraLiveActionHelper;
import com.cylan.jiafeigou.module.CameraLiveHelper;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.module.DPTimeZone;
import com.cylan.jiafeigou.module.DoorLockHelper;
import com.cylan.jiafeigou.module.HistoryManager;
import com.cylan.jiafeigou.module.SubscriptionSupervisor;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractFragmentPresenter;
import com.cylan.jiafeigou.push.BellPuller;
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
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
                        boolean videoPlaying = CameraLiveHelper.isVideoPlaying(liveActionHelper);
                        if (videoPlaying) {
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGMsgVideoRtcp>() {
                    @Override
                    public void call(JFGMsgVideoRtcp jfgMsgVideoRtcp) {
                        feedRtcp.feed(jfgMsgVideoRtcp);
                        mView.onRtcp(jfgMsgVideoRtcp);
                        boolean goodFrameNow = feedRtcp.isGoodFrameNow();
                        if (goodFrameNow && !liveActionHelper.isPendingPlayActionCompleted()) {
                            liveActionHelper.onUpdatePendingPlayAction(true);
                            mView.onVideoPlayActionCompleted();
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
                        if (BuildConfig.DEBUG) {
                            Log.d(CameraLiveHelper.TAG, "monitorHistoryVideoError:" + jfgHistoryVideoErrorInfo);
                        }
                        liveActionHelper.onUpdateHistoryVideoError(jfgHistoryVideoErrorInfo);
                        performStopVideoAction(false);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        AppLogger.e(throwable);
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (BuildConfig.DEBUG) {
                            Log.d(CameraLiveHelper.TAG, "monitorHistoryVideoError:Action Error:" + throwable);
                        }
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                            performReportPlayError(playError);
                        }
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
                        if (BuildConfig.DEBUG) {
                            Log.d(CameraLiveHelper.TAG, "monitorVideoResolution已经收到了分辨率消息了:" + jfgMsgVideoResolution);
                        }
                        liveActionHelper.onVideoResolutionReached(jfgMsgVideoResolution);
                        resolutionH = jfgMsgVideoResolution.height;
                        resolutionW = jfgMsgVideoResolution.width;
                        PreferencesUtils.putFloat(JConstant.KEY_UUID_RESOLUTION + uuid, (float) jfgMsgVideoResolution.height / jfgMsgVideoResolution.width);
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (BuildConfig.DEBUG) {
                            Log.d(CameraLiveHelper.TAG, "monitorVideoResolution,正在检查 playError:" + playError);
                        }
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                            performReportPlayError(playError);
                        } else {
                            try {
                                mView.onResolution(jfgMsgVideoResolution);
                                mView.onVideoPlayActionCompleted();
                                performUpdateBottomMenuEnable();
                                performUpdateBottomMenuOn();
                                if (BuildConfig.DEBUG) {
                                    Log.d(CameraLiveHelper.TAG, "正在更新分辨率");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                AppLogger.e(e);
                                liveActionHelper.onUpdateVideoPlayCode(CameraLiveHelper.PLAY_ERROR_JFG_EXCEPTION_THROW);
                                playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                                if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                                    performReportPlayError(playError);
                                    if (BuildConfig.DEBUG) {
                                        Log.d(CameraLiveHelper.TAG, "更新分辨率出现异常,正在检查 playError:" + playError);
                                    }
                                }
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

    private void performReportPlayError(int playError) {
        if (BuildConfig.DEBUG) {
            Log.d(CameraLiveHelper.TAG, "performReportPlayError:" + CameraLiveHelper.printError(playError));
        }
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
                    case CameraLiveHelper.PLAY_ERROR_BAD_FRAME_RATE: {
                        mView.onPlayErrorBadFrameRate();
                        performUpdateBottomMenuEnable();
                        performStopVideoAction(CameraLiveHelper.isLive(liveActionHelper));
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_LOW_FRAME_RATE: {
                        mView.onPlayErrorLowFrameRate();
                        performUpdateBottomMenuEnable();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED_TIME_OUT: {
                        mView.onPlayErrorWaitForPlayCompletedTimeout();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_UN_KNOW_PLAY_ERROR: {
                        mView.onPlayErrorUnKnowPlayError(CameraLiveHelper.checkUnKnowErrorCode(true));
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_IN_CONNECTING: {
                        mView.onPlayErrorInConnecting();
                    }
                    break;
                }
            }
        });
        addStopSubscription(schedule);
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
                boolean microphoneOn = liveActionHelper.isMicrophoneOn();
                boolean speakerOn = CameraLiveHelper.checkSpeakerOn(liveActionHelper, microphoneOn);
                mView.onUpdateBottomMenuOn(speakerOn, microphoneOn);
            }
        });
        addStopSubscription(subscription);
    }

    @Override
    public void performPlayVideoAction(boolean live, long timestamp) {
        Subscription subscribe = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                Subscription subscription = AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        mView.onUpdateVideoLoading(true);
                    }
                });
                subscriber.add(subscription);
                liveActionHelper.onUpdatePendingPlayAction(true);
                feedRtcp.stop();
                performUpdateBottomMenuEnable();
                int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    //当前情况下不能播放
                    if (BuildConfig.DEBUG) {
                        Log.d(CameraLiveHelper.TAG, "checkPlayError Failed:" + playError + ",throw HelperBreaker");
                    }
                    subscriber.onError(new RxEvent.HelperBreaker(playError));
                    return;
                }

                liveActionHelper.onUpdateVideoPlayType(live);
                if (BuildConfig.DEBUG) {
                    Log.d(CameraLiveHelper.TAG, "updateVideoPlayType,isLive:" + live);
                }

                boolean shouldDisconnectFirst = CameraLiveHelper.shouldDisconnectFirst(liveActionHelper);
                if (shouldDisconnectFirst) {
                    //播放前需要先断开
                    if (BuildConfig.DEBUG) {
                        Log.d(CameraLiveHelper.TAG, "播放前的断开直播过程:是否 Live:" + live);
                    }
                    liveActionHelper.onUpdateSyncAction(CameraLiveActionHelper.SYNC_EVENT_WAIT_FOR_STOP_COMPLETED);
                    performStopVideoAction(live);
                }

                while (!subscriber.isUnsubscribed()&&CameraLiveHelper.checkSyncEvent(liveActionHelper, CameraLiveActionHelper.SYNC_EVENT_WAIT_FOR_STOP_COMPLETED)) {
                    Log.d(CameraLiveHelper.TAG, "等待同步事件结束");
                    SystemClock.sleep(500);
                }

                if (BuildConfig.DEBUG) {
                    Log.d(CameraLiveHelper.TAG, "将要开始播放视频了,isLive:" + live + ",timestamp:" + timestamp);
                }
                int playCode;
                try {
                    if (live) {
                        liveActionHelper.onUpdateLastPlayTime(System.currentTimeMillis());
                        BellPuller.getInstance().currentCaller(uuid);//查看直播时禁止呼叫
                        playCode = Command.getInstance().playVideo(uuid);
                    } else {
                        liveActionHelper.onUpdateLastPlayTime(timestamp);
                        BellPuller.getInstance().currentCaller(null);//查看直播时禁止呼叫
                        playCode = Command.getInstance().playHistoryVideo(uuid, System.currentTimeMillis() / timestamp > 100 ? timestamp : timestamp / 1000);
                    }
                } catch (Exception e) {
                    playCode = CameraLiveHelper.PLAY_ERROR_JFG_EXCEPTION_THROW;
                    e.printStackTrace();
                    AppLogger.e(e);
                }
                if (BuildConfig.DEBUG) {
                    Log.d(CameraLiveHelper.TAG, "正在更新 PlayCode:" + playCode);
                }
                liveActionHelper.onUpdateVideoPlayCode(playCode);

                playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    //开始播放历史视频或直播出现了错误
                    if (BuildConfig.DEBUG) {
                        Log.d(CameraLiveHelper.TAG, "开始播放历史视频或者直播出现了错误:" + playError);
                    }
                    subscriber.onError(new RxEvent.HelperBreaker(playError));
                    return;
                }

                liveActionHelper.onVideoPlayStarted(live);
                playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    performReportPlayError(playError);
                }
                if (BuildConfig.DEBUG) {
                    Log.d(CameraLiveHelper.TAG, "直播已经开始,正在等待直播完成,成功或超时?........");
                }

                Subscription schedule = Schedulers.io().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                            if (BuildConfig.DEBUG) {
                                Log.d(CameraLiveHelper.TAG, "等了三十秒了还没播放成功");
                            }
                            liveActionHelper.onUpdateVideoPlayTimeOutAction();
                            subscriber.onError(new RxEvent.HelperBreaker(playError));
                        } else {
                            if (BuildConfig.DEBUG) {
                                Log.d(CameraLiveHelper.TAG, "应该是播放成功了");
                            }
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
                        if (throwable instanceof RxEvent.HelperBreaker) {
                            int playError = ((RxEvent.HelperBreaker) throwable).breakerCode;
                            if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                                performReportPlayError(playError);
                            }
                        }
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                            performReportPlayError(playError);
                        }
                    }
                });
        addStopSubscription(subscribe);
    }

    @Override
    public void performPlayVideoAction() {
        boolean live = CameraLiveHelper.isLive(liveActionHelper);
        long lastPlayTime = CameraLiveHelper.getLastPlayTime(live, liveActionHelper);
        performPlayVideoAction(live, lastPlayTime);
    }

    @Override
    public void performStopVideoAction(boolean live) {
        Subscription subscribe = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                feedRtcp.stop();
                BellPuller.getInstance().currentCaller(null);//查看直播时禁止呼叫
                int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                if (BuildConfig.DEBUG) {
                    Log.d(CameraLiveHelper.TAG, "断开播放前,先看下有没有错误:" + CameraLiveHelper.printError(playError));
                }
                if (playError == CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    if (BuildConfig.DEBUG) {
                        Log.d(CameraLiveHelper.TAG, "正在保存直播截图");
                    }
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
                if (BuildConfig.DEBUG) {
                    Log.d(CameraLiveHelper.TAG, "停止直播是否成功呢: playCode is:" + playCode);
                }
                liveActionHelper.onUpdateVideoPlayCode(playCode);
                liveActionHelper.onVideoPlayStopped(live);
                liveActionHelper.onUpdateVideoPlayType(live);
                performUpdateBottomMenuEnable();
                playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    subscriber.onError(new RxEvent.HelperBreaker(playError));
                    return;
                }
                subscriber.onNext("");
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                            performReportPlayError(playError);
                        } else {
                            mView.onVideoPlayStopped(live);
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof RxEvent.HelperBreaker) {
                            int playError = ((RxEvent.HelperBreaker) throwable).breakerCode;
                            if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                                performReportPlayError(playError);
                            }
                        }
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                            performReportPlayError(playError);
                        }
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
                        Log.d(CameraLiveHelper.TAG, "截图已经成功了");
                        final String cover = JConstant.MEDIA_PATH + File.separator + uuid + "_cover.png";
                        //需要删除之前的一条记录.
                        BitmapUtils.saveBitmap2file(bitmap, cover);
                        PreferencesUtils.putString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, System.currentTimeMillis() + "");
                        PreferencesUtils.putString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_FILE + uuid, cover);
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onFailure(String s) {
                        Log.d(CameraLiveHelper.TAG, "截图已经失败了");
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


    @Override
    public CameraLiveActionHelper getCameraLiveAction() {
        return liveActionHelper;
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
    public void performChangeSpeakerAction(boolean speakerOn) {
        Subscription subscription = Schedulers.io().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                liveActionHelper.onUpdateSpeakerOn(speakerOn);
                boolean microphoneOn = CameraLiveHelper.checkMicrophoneOn(liveActionHelper, speakerOn);
                Command.getInstance().setAudio(true, microphoneOn, speakerOn);
                Command.getInstance().setAudio(false, speakerOn, microphoneOn);
                switchEarpiece(isEarpiecePlug());
                performUpdateBottomMenuEnable();
                performUpdateBottomMenuOn();
                Log.d(CameraLiveHelper.TAG, "performChangeSpeakerAction,speakerOn:" + speakerOn + ",microphoneOn:" + microphoneOn);
            }
        });
        addStopSubscription(subscription);
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
                performUpdateBottomMenuEnable();
                performUpdateBottomMenuOn();
                Log.d(CameraLiveHelper.TAG, "performChangeMicrophoneAction,speakerOn:" + speakerOn + ",microphoneOn:" + microphoneOn);
            }
        });
        addStopSubscription(subscription);
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

    @Override
    public String getThumbnailKey() {
        return PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_FILE + uuid);
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
        playError = CameraLiveHelper.checkPlayError(liveActionHelper);
        if (playError == CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
            //说明当前是正常播放状态下 FrameFailed
            performStopVideoAction(CameraLiveHelper.isLive(liveActionHelper));
        } else {
            performReportPlayError(playError);
        }
        feedRtcp.stop();
    }

    @Override
    public void onFrameRate(boolean slow) {
        AppLogger.e("is bad net work show loading?" + slow);
        liveActionHelper.onUpdateVideoSlowState(slow);
        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
            performReportPlayError(playError);
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

    @Override
    public void stop() {
        super.stop();
        SubscriptionSupervisor.unsubscribe("com.cylan.jiafeigou.misc.ver.DeviceVersionChecker", SubscriptionSupervisor.CATEGORY_DEFAULT, "DeviceVersionChecker.startCheck");
    }
}
