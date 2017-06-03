package com.cylan.jiafeigou.base.wrapper;

import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jfgapp.interfases.CallBack;
import com.cylan.jiafeigou.base.view.ViewablePresenter;
import com.cylan.jiafeigou.base.view.ViewableView;
import com.cylan.jiafeigou.misc.ApFilter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.live.IFeedRtcp;
import com.cylan.jiafeigou.misc.live.LiveFrameRateMonitor;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.base.view.ViewableView.BAD_FRAME_RATE;
import static com.cylan.jiafeigou.base.view.ViewableView.BAD_NET_WORK;
import static com.cylan.jiafeigou.base.view.ViewableView.STOP_VIERER_BY_SYSTEM;
import static com.cylan.jiafeigou.misc.JError.ErrorVideoPeerDisconnect;

/**
 * Created by yzd on 16-12-30.
 */

public abstract class BaseViewablePresenter<V extends ViewableView> extends BasePresenter<V> implements ViewablePresenter<V>, IFeedRtcp.MonitorListener {
    protected String mViewLaunchType;

    protected ViewableView.LiveStreamAction liveStreamAction = new ViewableView.LiveStreamAction();
    IFeedRtcp feedRtcp = new LiveFrameRateMonitor();

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getDeviceUnBindSub());
        registerSubscription(getLoadSub());
    }

    protected Subscription getLoadSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.VideoLoadingEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .map(load -> {
                    AppLogger.d("正在加载中" + load.slow);
                    mView.onLoading(load.slow);
                    return load;
                })
                .filter(load -> load.slow)
                .delay(4, TimeUnit.SECONDS)
                .throttleFirst(2, TimeUnit.SECONDS)
                .subscribe(ret -> {
                    if (!sourceManager.isOnline() && liveStreamAction.hasStarted && !ApFilter.isAPMode(uuid)) {
                        AppLogger.d("无网络连接");
                        JFGMsgVideoDisconn disconn = new JFGMsgVideoDisconn();
                        disconn.code = BAD_NET_WORK;
                        disconn.remote = getViewHandler();
                        RxBus.getCacheInstance().post(disconn);
                    }
                }, AppLogger::e);
    }

    private Subscription getDeviceUnBindSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceUnBindedEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(event -> TextUtils.equals(event.uuid, uuid))
                .subscribe(event -> {
                    if (mView != null) {
                        mView.onDeviceUnBind();
                    }
                }, e -> AppLogger.d(e.getMessage()));
    }
//    #104321  #106065 #106553 #107075 #107095

    @Override
    public void startViewer() {
        Subscription subscribe = Observable.just(sourceManager.isOnline())
                .observeOn(AndroidSchedulers.mainThread())
                .map(account -> {
                    feedRtcp.stop();//清空之前的状态
                    liveStreamAction.reset();
                    mView.onViewer();
                    if (shouldShowPreview()) {
                        File file = new File(JConstant.MEDIA_PATH, "." + uuid + ".jpg");
                        mView.onShowVideoPreviewPicture(file.toString());
                    }
                    return getViewHandler();
                })
                .observeOn(Schedulers.io())
                .map(handle -> {
                    try {
                        AppLogger.d("正在准备开始直播,对端 cid 为:" + handle);
                        int ret = appCmd.playVideo(handle);
                        AppLogger.d("准备开始直播返回的结果码为:" + ret);
                        if (ret != 0) {
                            appCmd.stopPlay(handle);
                            appCmd.playVideo(handle);
                            AppLogger.d("正在重试播放直播");
                        }
                        liveStreamAction.hasStarted = true;
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.d("准备开始直播失败!");
                    }
                    return handle;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(this::handleVideoResponse)
                .filter(response -> response.success)
                .map(response -> (JFGMsgVideoResolution) response.response)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(rsp -> {
                    try {
                        AppLogger.d("接收到分辨率消息,准备播放直播");
                        liveStreamAction.hasResolution = true;
                        if (mView != null) {
                            mView.onResolution(rsp);
                        }
                        mViewLaunchType = onResolveViewIdentify();
                        RxBus.getCacheInstance().post(new BaseCallablePresenter.Notify(false));//发送一条 Notify 消息表明不需要再查询预览图了
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                            .takeUntil(handlerVideoDisconnect(rsp));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rtcp -> {
                    liveStreamAction.hasStarted = true;
                    feedRtcp.feed(rtcp);
                    if (mView != null) {
                        mView.onFlowSpeed(rtcp.bitRate);
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                    feedRtcp.stop();
                    e.printStackTrace();
                    if (e instanceof TimeoutException) {
                        AppLogger.d("连接设备超时,即将退出!");
                        try {
                            appCmd.stopPlay(getViewHandler());
                        } catch (JfgException e1) {
                            e1.printStackTrace();
                        }
                        if (liveStreamAction.hasStarted) {
                            if (mView != null) {
                                mView.onVideoDisconnect(BAD_FRAME_RATE);
                            }
                        } else {
                            if (mView != null) {
                                mView.onConnectDeviceTimeOut();
                            }
                        }
                    }
                });
        registerSubscription(subscribe);
    }

    protected boolean shouldShowPreview() {
        return true;
    }


    public void cancelViewer() {
        Subscription subscribe = stopViewer().subscribe(ret -> {
        }, AppLogger::e);
        registerSubscription(subscribe);
    }

    /**
     * stopViewer是被动的,dismiss是主动的,即stop虽然停止了直播,但不会清除播放状态
     * 这样当我们onPause时停止直播后可以在onResume中进行恢复,dismiss不仅会停止直播
     * 而且还会清除播放状态
     */
    protected Observable<Boolean> stopViewer() {
        return Observable.just(getViewHandler())
                .filter(handler -> liveStreamAction.hasStarted)
                .subscribeOn(Schedulers.io())
                .map(handler -> {
                    if (!TextUtils.isEmpty(handler)) {
                        liveStreamAction.hasStarted = false;
                        try {
                            appCmd.screenshot(false, new CallBack<Bitmap>() {
                                @Override
                                public void onSucceed(Bitmap bitmap) {
                                    BitmapUtils.saveBitmap2file(bitmap, JConstant.MEDIA_PATH + File.separator + "." + uuid + ".jpg");
                                }

                                @Override
                                public void onFailure(String s) {
                                    AppLogger.d("保存门铃画像失败" + s);
                                }
                            });
                            appCmd.stopPlay(handler);
                            JFGMsgVideoDisconn disconn = new JFGMsgVideoDisconn();
                            disconn.remote = getViewHandler();
                            disconn.code = STOP_VIERER_BY_SYSTEM;
                            RxBus.getCacheInstance().post(disconn);//结束 startView 的订阅链
                            AppLogger.d("正在发送停止直播消息:" + getViewHandler());
                            return true;
                        } catch (JfgException e) {
                            e.printStackTrace();
                            AppLogger.d("停止直播失败");
                        }
                    }
                    return false;
                });
    }

    protected Observable<RxEvent.LiveResponse> handleVideoResponse(String peer) {
        return Observable.merge(
                RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                        .subscribeOn(Schedulers.io())
                        .filter(rsp -> TextUtils.equals(rsp.peer, peer))
                        .timeout(30, TimeUnit.SECONDS)
                        .map(RxEvent.LiveResponse::new),
                RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(dis -> {
                            AppLogger.d("视频连接断开了: remote:" + dis.remote + "code:" + dis.code);
                            if (mView != null) {
                                switch (dis.code) {
                                    case STOP_VIERER_BY_SYSTEM:
                                        break;
                                    default:
                                        mView.onVideoDisconnect(dis.code);
                                }
                            }
                            return new RxEvent.LiveResponse(dis, false);
                        })
        ).first().map(rsp -> {
            RxBus.getCacheInstance().post(new RxEvent.CallResponse(true));//发送一条 CallAnswer 消息表明不需要再等待门铃超时了
            return rsp;
        });
    }

    protected Observable<JFGMsgVideoDisconn> handlerVideoDisconnect(JFGMsgVideoResolution resolution) {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                .first(jfgMsgVideoDisconn -> jfgMsgVideoDisconn.code > 0)
                .observeOn(AndroidSchedulers.mainThread())
                .map(dis -> {
                    AppLogger.d("收到了断开视频的消息:" + dis.code);
                    mView.onVideoDisconnect(dis.code);
                    return dis;
                });
    }

    protected Subscription watchLoginState() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultLogin.class)
                .filter(ret -> sourceManager.getAccount() != null && sourceManager.getAccount().isOnline())
                .observeOn(Schedulers.io())
                .subscribe(ret -> {
                    try {
                        AppLogger.d("网络状态发生变化,正在发送断开视频消息");
                        JFGMsgVideoDisconn disconn = new JFGMsgVideoDisconn();
                        disconn.code = ErrorVideoPeerDisconnect;//连接互联网不可用,
                        disconn.remote = getViewHandler();
                        RxBus.getCacheInstance().post(disconn);
                        appCmd.stopPlay(uuid);
                        liveStreamAction.hasStarted = false;
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }

    @Override
    protected String onResolveViewIdentify() {
        return uuid;
    }

    @Override
    public void onScreenRotationChanged(boolean land) {
        if (mView != null) {
            mView.onSpeaker(liveStreamAction.speakerOn);
            mView.onMicrophone(liveStreamAction.microphoneOn);
        }
    }

    @Override
    public void onStop() {
        AppLogger.d("stop" + getViewHandler());
        if (getViewHandler() != null) {
            if (liveStreamAction.hasStarted) {
                Subscription subscribe = stopViewer().subscribe(s -> setViewHandler(null), AppLogger::e);
                registerSubscription(subscribe);
            }
        }
        super.onStop();
    }

    protected void setViewHandler(String handler) {
    }

    @Override
    public void onStart() {
        super.onStart();
        feedRtcp.setMonitorListener(this);
    }

    protected String getViewHandler() {
        return onResolveViewIdentify();
    }

    @Override
    public void dismiss() {
        Subscription subscribe = stopViewer().observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                }, () -> {
                    setViewHandler(null);
                    if (mView != null) {
                        mView.onDismiss();
                    }
                });
        registerSubscription(subscribe);
    }

    @Override
    public void switchSpeaker() {
        Subscription subscribe = setSpeaker(!liveStreamAction.speakerOn)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(on -> {
                    if (mView != null) {
                        mView.onSpeaker(on);
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                });
        registerSubscription(subscribe);
    }

    @Override
    public void switchMicrophone() {
        Subscription subscribe = setMicrophone(liveStreamAction.microphoneOn)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(on -> {
                    if (mView != null) {
                        mView.onMicrophone(on);
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                });
        registerSubscription(subscribe);
    }

    private Observable<Boolean> setMicrophone(boolean on) {
        return Observable.just(on)
                .observeOn(Schedulers.io())
                .map(s -> {
                    AppLogger.d("正在切换 setMicrophone :" + on);
                    switchSpeakAndMicroPhone(true, true, on);
                    switchSpeakAndMicroPhone(false, on, true);
                    liveStreamAction.microphoneOn = !on;
                    return s;
                }).subscribeOn(Schedulers.io());
    }

    protected Observable<Boolean> setSpeaker(boolean on) {
        return Observable.just(on)
                .observeOn(Schedulers.io())
                .map(s -> {
                    AppLogger.d("正在切换 Speaker :" + on);
                    //sdk存在bug.不能连续两次打开mic.
                    boolean success = switchSpeakAndMicroPhone(true, true, on);
                    switchSpeakAndMicroPhone(false, on, true);
                    liveStreamAction.speakerOn = success && on;
                    return liveStreamAction.speakerOn;
                }).subscribeOn(Schedulers.io());
    }

    protected boolean switchSpeakAndMicroPhone(boolean local, boolean speaker, boolean microphone) {
        MediaRecorder mRecorder = null;
        if (speaker && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//这是为了兼容魅族4.4的权限
            try {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.release();
            } catch (Exception e) {
                AppLogger.d(e.getMessage());
                if (mRecorder != null) {
                    mRecorder.release();
                }
                mView.hasNoAudioPermission();
                return false;
            }
        }
        Log.d("switchSpeakAndMicro", "local:" + local + ",speaker:" + speaker + ",mic:" + microphone);
        appCmd.setAudio(local, microphone, speaker);//开启设备的扬声器和麦克风
//        appCmd.setAudio(true, speaker, microphone);//开启客户端的扬声器和麦克风
        return true;
    }


    @Override
    public SurfaceView getViewerInstance() {
        SurfaceView surfaceView = (SurfaceView) VideoViewFactory.CreateRendererExt(false, mView.getAppContext(), true);
        surfaceView.setId("IVideoView".hashCode());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        surfaceView.setLayoutParams(params);
        return surfaceView;
    }

    @Override
    public boolean checkAudio(int type) {//0: speaker,1: microphone
        if (type == 0) {
            return liveStreamAction.speakerOn;
        } else if (type == 1) {
            return liveStreamAction.microphoneOn;
        }
        return false;
    }

    @Override
    public void onFrameFailed() {
        Schedulers.io().createWorker().schedule(() -> {
            liveStreamAction.hasLiveError = true;
            feedRtcp.stop();
            AppLogger.d("加载失败了..........");
            JFGMsgVideoDisconn disconn = new JFGMsgVideoDisconn();
            disconn.code = BAD_FRAME_RATE;
            disconn.remote = getViewHandler();
            RxBus.getCacheInstance().post(disconn);
        });
    }

    @Override
    public void onFrameRate(boolean slow) {
        RxBus.getCacheInstance().post(new RxEvent.VideoLoadingEvent(slow));
    }
}
