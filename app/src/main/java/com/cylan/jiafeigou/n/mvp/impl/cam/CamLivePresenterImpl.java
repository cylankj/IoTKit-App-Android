package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;

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
    private CameraLiveActionHelper liveActionHelper;
    /**
     * 帧率记录
     */
    private IFeedRtcp feedRtcp = new LiveFrameRateMonitor();

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
                        boolean liveActionCompleted = liveActionHelper.isPendingPlayLiveActionCompleted;
                        boolean goodFrameRate = jfgMsgVideoRtcp.frameRate > 0;
                        if (goodFrameRate) {
                            liveActionHelper.isPendingPlayLiveActionCompleted = true;
                            if (!liveActionCompleted) {
                                mView.onVideoPlayActionCompleted();
                            }
                        }
                        boolean videoPlaying = CameraLiveHelper.isVideoPlaying(liveActionHelper);
                        if (videoPlaying) {
                            feedRtcp.feed(jfgMsgVideoRtcp);
                            mView.onRtcp(jfgMsgVideoRtcp);
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
                        boolean isPendingPlayActionCompleted = liveActionHelper.isPendingPlayLiveActionCompleted;
                        liveActionHelper.onVideoResolutionReached(jfgMsgVideoResolution);
                        resolutionH = jfgMsgVideoResolution.height;
                        resolutionW = jfgMsgVideoResolution.width;
                        PreferencesUtils.putFloat(JConstant.KEY_UUID_RESOLUTION + uuid, (float) jfgMsgVideoResolution.height / jfgMsgVideoResolution.width);
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (BuildConfig.DEBUG) {
                            Log.d(CameraLiveHelper.TAG, "monitorVideoResolution,正在检查 playError:" + CameraLiveHelper.printError(playError));
                        }
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                            performReportPlayError(playError);
                        } else {
                            try {
                                mView.onResolution(jfgMsgVideoResolution);
                                if (!isPendingPlayActionCompleted) {
                                    mView.onVideoPlayActionCompleted();
                                }
                                performUpdateBottomMenuEnable();
                                performUpdateBottomMenuOn();
                                if (BuildConfig.DEBUG) {
                                    Log.d(CameraLiveHelper.TAG, "正在更新分辨率");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                AppLogger.e(e);
                                liveActionHelper.playCode = CameraLiveHelper.PLAY_ERROR_JFG_EXCEPTION_THROW;
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
                liveActionHelper.lastPlayError = playError;
                switch (playError) {
                    case CameraLiveHelper.PLAY_ERROR_STANDBY: {
                        mView.onPlayErrorStandBy();
                        performStopVideoAction();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_FIRST_SIGHT: {
                        mView.onPlayErrorFirstSight();
                        performStopVideoAction();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_NO_NETWORK: {
                        mView.onPlayErrorNoNetwork();
                        performStopVideoAction();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_DEVICE_OFF_LINE: {
                        mView.onPlayErrorDeviceOffLine();
                        performStopVideoAction();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_JFG_EXCEPTION_THROW: {
                        mView.onPlayErrorException();
                        performStopVideoAction();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED: {
                        mView.onPlayErrorWaitForPlayCompleted();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_BAD_FRAME_RATE: {
                        mView.onPlayErrorBadFrameRate();
                        performUpdateBottomMenuEnable();
                        performStopVideoAction();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_LOW_FRAME_RATE: {
                        mView.onPlayErrorLowFrameRate();
                        performUpdateBottomMenuEnable();
                    }
                    break;
                    case CameraLiveHelper.PLAY_ERROR_WAIT_FOR_PLAY_COMPLETED_TIME_OUT: {
                        mView.onPlayErrorWaitForPlayCompletedTimeout();
                        performStopVideoAction();
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
                final boolean microphoneOn = liveActionHelper.isMicrophoneOn;
                final boolean speakerOn = CameraLiveHelper.checkSpeakerOn(liveActionHelper, microphoneOn);
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
                feedRtcp.stop();
                liveActionHelper.onVideoPlayStopped(live);
                Subscription subscription = AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        mView.onUpdateVideoLoading(true);
                    }
                });
                subscriber.add(subscription);
                performUpdateBottomMenuEnable();
                int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    //当前情况下不能播放
                    if (BuildConfig.DEBUG) {
                        Log.d(CameraLiveHelper.TAG, "checkPlayError Failed:" + CameraLiveHelper.printError(playError) + ",throw HelperBreaker");
                    }
                    subscriber.onError(new RxEvent.HelperBreaker(playError));
                    return;
                }
                liveActionHelper.isLive = live;
                if (BuildConfig.DEBUG) {
                    Log.d(CameraLiveHelper.TAG, "updateVideoPlayType,isLive:" + live);
                }

                boolean shouldDisconnectFirst = CameraLiveHelper.shouldDisconnectFirst(liveActionHelper);
                if (shouldDisconnectFirst) {
                    //播放前需要先断开
                    if (BuildConfig.DEBUG) {
                        Log.d(CameraLiveHelper.TAG, "播放前的断开直播过程:是否 Live:" + live);
                    }
                    performStopVideoAction(live);
                }

                while (!subscriber.isUnsubscribed() && !liveActionHelper.isPendingStopLiveActionCompleted) {
                    if (BuildConfig.DEBUG) {
                        Log.d(CameraLiveHelper.TAG, "正在等待停止播放结束");
                    }
                    SystemClock.sleep(500);
                }

                if (BuildConfig.DEBUG) {
                    Log.d(CameraLiveHelper.TAG, "将要开始播放视频了,isLive:" + live + ",timestamp:" + timestamp);
                }
                int playCode;
                try {
                    if (live) {
                        liveActionHelper.lastPlayTime = System.currentTimeMillis();
                        BellPuller.getInstance().currentCaller(uuid);//查看直播时禁止呼叫
                        playCode = Command.getInstance().playVideo(uuid);
                    } else {
                        liveActionHelper.lastPlayTime = timestamp;
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
                liveActionHelper.playCode = playCode;
                playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    //开始播放历史视频或直播出现了错误
                    if (BuildConfig.DEBUG) {
                        Log.d(CameraLiveHelper.TAG, "开始播放历史视频或者直播出现了错误:" + CameraLiveHelper.printError(playError));
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
                        boolean pendingPlayActionCompleted = liveActionHelper.isPendingPlayLiveActionCompleted;
                        liveActionHelper.isPendingPlayLiveActionCompleted = true;
                        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                        if (!pendingPlayActionCompleted) {
                            if (BuildConfig.DEBUG) {
                                Log.d(CameraLiveHelper.TAG, "等了三十秒了还没播放成功");
                            }
                            liveActionHelper.onUpdateVideoPlayTimeOutAction();
                        }
                        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
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
                boolean stopLiveActionCompleted = liveActionHelper.isPendingStopLiveActionCompleted;
                boolean videoPlaying = CameraLiveHelper.isVideoPlaying(liveActionHelper);
                if (!videoPlaying || !stopLiveActionCompleted) {
                    if (BuildConfig.DEBUG) {
                        Log.d(CameraLiveHelper.TAG, "performStopVideoAction,live:" + live + ",当前情况下无需再停止直播了,是否正在播放:" + videoPlaying + ",是否有正在执行的停止Action:" + !stopLiveActionCompleted);
                    }
                    return;
                }
                liveActionHelper.isPendingStopLiveActionCompleted = false;
                BellPuller.getInstance().currentCaller(null);//查看直播时禁止呼叫
                int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                if (BuildConfig.DEBUG) {
                    Log.d(CameraLiveHelper.TAG, "断开播放前,先看下有没有错误:" + CameraLiveHelper.printError(playError));
                }
                if (playError == CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    if (BuildConfig.DEBUG) {
                        Log.d(CameraLiveHelper.TAG, "正在保存直播截图");
                    }
                    performLivePictureCaptureSaveAction(false);
                }
                int waitCount = 0;
                while (waitCount < 10 && !liveActionHelper.isPendingCaptureActionCompleted) {
                    if (BuildConfig.DEBUG) {
                        Log.d(CameraLiveHelper.TAG, "正在等待截图结束.....");
                    }
                    SystemClock.sleep(500);
                    waitCount++;
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
                    Log.d(CameraLiveHelper.TAG, "停止直播是否成功呢: playCode is:" + CameraLiveHelper.printError(playError));
                }
                liveActionHelper.onVideoPlayStopped(live);
                liveActionHelper.playCode = playCode;
                liveActionHelper.isLive = live;
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
        addDestroySubscription(subscribe);
    }

    @Override
    public void performStopVideoAction() {
        performStopVideoAction(CameraLiveHelper.isLive(liveActionHelper));
    }

    @Override
    public void performLivePictureCaptureSaveAction(boolean saveInPhotoAndNotify) {
        if (!CameraLiveHelper.isVideoPlaying(liveActionHelper)) {
            if (BuildConfig.DEBUG) {
                Log.d(CameraLiveHelper.TAG, "当前没有开始播放,无法截取缩略图, notify:" + saveInPhotoAndNotify);
            }
            return;
        }
        Subscription subscribe = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                liveActionHelper.isPendingCaptureActionCompleted = false;
                Command.getInstance().screenshot(false, new CallBack<Bitmap>() {
                    @Override
                    public void onSucceed(Bitmap bitmap) {
                        PerformanceUtils.startTrace("takeCapture");
                        Log.d(CameraLiveHelper.TAG, "截图已经成功了, bitmap 是否为空:" + (bitmap == null));
                        subscriber.onNext(bitmap);
                        subscriber.onCompleted();
                        if (bitmap != null) {
                            final String cover = JConstant.MEDIA_PATH + File.separator + uuid + "_cover.png";
                            BitmapUtils.saveBitmap2file(bitmap, cover);
                            PreferencesUtils.putString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_FILE + uuid, cover);
                            PreferencesUtils.putString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, System.currentTimeMillis() + "");

                            if (saveInPhotoAndNotify) {
                                final String fileName = uuid + System.currentTimeMillis() + ".png";
                                final String filePath = JConstant.MEDIA_PATH + File.separator + fileName;
                                BitmapUtils.saveBitmap2file(bitmap, filePath);
                            }
                            liveActionHelper.isPendingCaptureActionCompleted = true;
                        }
                    }

                    @Override
                    public void onFailure(String s) {
                        Log.d(CameraLiveHelper.TAG, "截图已经失败了");
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                        liveActionHelper.isPendingCaptureActionCompleted = true;
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

                    }
                });
        addDestroySubscription(subscribe);
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
                    liveActionHelper.isSDCardExist = hasSdcard;
                    if (!hasSdcard) {
                        AppLogger.d("sdcard 被拔出");
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
                    liveActionHelper.isStandBy = standBy;
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
                liveActionHelper.isSpeakerOn = speakerOn;
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
    public void performChangeSpeakerAction() {
        performChangeSpeakerAction(!liveActionHelper.isSpeakerOn);
    }

    @Override
    public void performChangeMicrophoneAction(boolean microphoneOn) {
        Subscription subscription = Schedulers.io().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                liveActionHelper.isMicrophoneOn = microphoneOn;
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
    public void performChangeMicrophoneAction() {
        performChangeMicrophoneAction(!liveActionHelper.isMicrophoneOn);
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
        if (!CameraLiveHelper.isVideoPlaying(liveActionHelper)) {
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
    public boolean isShareDevice() {
        return JFGRules.isShareDevice(uuid);
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
                DpMsgDefine.DPPrimary<Integer> dpPrimary = new DpMsgDefine.DPPrimary<>(mode);
                try {
                    AppLogger.e("还需要发送局域网消息");
                    boolean updateValue = DataSourceManager.getInstance().updateValue(uuid, dpPrimary, 513);
                    subscriber.onNext(updateValue);
                    subscriber.onCompleted();
                } catch (IllegalAccessException e) {
                    AppLogger.e(e);
                    subscriber.onNext(false);
                    subscriber.onCompleted();
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        addStopSubscription(subscribe);
    }

    @Override
    public void performLoadLiveThumbPicture() {
        Subscription subscribe = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                String filePath = PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_FILE + uuid);
                Bitmap lastLiveThumbPicture = CameraLiveHelper.checkLastLiveThumbPicture(liveActionHelper);
                if (lastLiveThumbPicture == null) {
                    lastLiveThumbPicture = BitmapFactory.decodeFile(filePath);
                    liveActionHelper.lastLiveThumbPicture = lastLiveThumbPicture;
                }
                if (BuildConfig.DEBUG) {
                    Log.d(CameraLiveHelper.TAG, "是否有可用的预览图片:" + lastLiveThumbPicture);
                }
                subscriber.onNext(lastLiveThumbPicture);
                subscriber.onCompleted();
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
        feedRtcp.stop();
        liveActionHelper.onUpdateVideoFrameFailed();
        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
        if (playError == CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
            //说明当前是正常播放状态下 FrameFailed
            performStopVideoAction(CameraLiveHelper.isLive(liveActionHelper));
        } else {
            performReportPlayError(playError);
        }
    }

    @Override
    public void onFrameRate(boolean slow) {
        AppLogger.e("is bad net work show loading?" + slow);
        int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
            performReportPlayError(playError);
        }
        liveActionHelper.onUpdateVideoSlowState(slow);
        boolean frameSlow = CameraLiveHelper.checkFrameSlow(liveActionHelper);
        if (frameSlow)
            playError = CameraLiveHelper.checkPlayError(liveActionHelper);
        if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
            performReportPlayError(playError);
        }
        liveActionHelper.onUpdateVideoSlowState(slow);
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
            boolean networkConnected = liveActionHelper.isNetworkConnected;
            liveActionHelper.isNetworkConnected = NetUtils.getJfgNetType() != 0;
            if (networkConnected != liveActionHelper.isNetworkConnected) {
                int playError = CameraLiveHelper.checkPlayError(liveActionHelper);
                if (playError != CameraLiveHelper.PLAY_ERROR_NO_ERROR) {
                    performReportPlayError(playError);
                }
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        Bitmap picture = liveActionHelper.lastLiveThumbPicture;
        if (picture != null && !picture.isRecycled()) {
            picture.recycle();
        }
        liveActionHelper.lastLiveThumbPicture = null;
        SubscriptionSupervisor.unsubscribe("com.cylan.jiafeigou.misc.ver.DeviceVersionChecker", SubscriptionSupervisor.CATEGORY_DEFAULT, "DeviceVersionChecker.startCheck");
    }
}
