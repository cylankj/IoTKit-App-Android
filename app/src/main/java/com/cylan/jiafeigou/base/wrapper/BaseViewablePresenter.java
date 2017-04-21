package com.cylan.jiafeigou.base.wrapper;

import android.media.MediaRecorder;
import android.os.Build;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.view.ViewablePresenter;
import com.cylan.jiafeigou.base.view.ViewableView;
import com.cylan.jiafeigou.misc.live.IFeedRtcp;
import com.cylan.jiafeigou.misc.live.LiveFrameRateMonitor;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

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
    protected boolean mIsMicrophoneOn = false;
    protected boolean hasLiveStream = false;
    protected boolean mIsSpeakerOn = false;
    protected String mViewLaunchType;
    IFeedRtcp feedRtcp = new LiveFrameRateMonitor();

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getDeviceUnBindSub());
//        registerSubscription(watchLoginState());
    }

    private Subscription getDeviceUnBindSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceUnBindedEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(event -> TextUtils.equals(event.uuid, mUUID))
                .subscribe(event -> {
                    if (mView != null) {
                        mView.onDeviceUnBind();
                    }
                }, e -> AppLogger.d(e.getMessage()));
    }

    @Override
    public void startViewer() {
        Subscription subscribe = Observable.just(sourceManager.getAccount())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(account -> {
                    if (sourceManager.isOnline()) {
                        mView.onViewer();
                        return true;
                    }
                    return false;
                }).observeOn(Schedulers.io())
                .map(hasNet -> {
                    String handle = getViewHandler();
                    try {
                        AppLogger.d("正在准备开始直播,对端 cid 为:" + handle);
                        hasLiveStream = true;
                        int ret = appCmd.playVideo(handle);
                        AppLogger.d("准备开始直播返回的结果码为:" + ret);
                        if (ret != 0) {
                            appCmd.stopPlay(handle);
                            appCmd.playVideo(handle);
                            AppLogger.d("正在重试播放直播");
                        }
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
                        hasLiveStream = true;
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
                    feedRtcp.feed(rtcp);
                    if (mView != null) {
                        mView.onFlowSpeed(rtcp.bitRate);
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                    if (e instanceof TimeoutException) {
                        AppLogger.d("连接设备超时,即将退出!");
                        if (hasLiveStream) {
                            if (mView != null) {
                                mView.onVideoDisconnect(BAD_NET_WORK);
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

    public void cancelViewer() {
        stopViewer().subscribe(ret -> {
        }, AppLogger::e);
    }

    /**
     * stopViewer是被动的,dismiss是主动的,即stop虽然停止了直播,但不会清除播放状态
     * 这样当我们onPause时停止直播后可以在onResume中进行恢复,dismiss不仅会停止直播
     * 而且还会清除播放状态
     */
    protected Observable<Boolean> stopViewer() {
        return Observable.just(getViewHandler())
                .filter(handler -> hasLiveStream)
                .subscribeOn(Schedulers.io())
                .map(handler -> {
                    if (!TextUtils.isEmpty(handler)) {
                        try {
                            hasLiveStream = false;
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
                                mView.onVideoDisconnect(dis.code);
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
                .filter(dis -> TextUtils.equals(dis.remote, resolution.peer))
                .mergeWith(
                        RxBus.getCacheInstance().toObservable(RxEvent.NetConnectionEvent.class)
                                .filter(event -> !event.isOnLine && event.mobile == null && event.wifi == null).map(event -> {
                            JFGMsgVideoDisconn disconn = new JFGMsgVideoDisconn();
                            disconn.code = BAD_NET_WORK;//连接互联网不可用,
                            disconn.remote = getViewHandler();
                            return disconn;
                        }))
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .map(dis -> {
                    AppLogger.d("收到了断开视频的消息:" + dis.code);
                    feedRtcp.stop();
                    switch (dis.code) {
                        case STOP_VIERER_BY_SYSTEM:
                            break;
                        default:
                        case BAD_NET_WORK:
                            if (mView != null) {
                                mView.onVideoDisconnect(dis.code);
                            }
                    }
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
                        appCmd.stopPlay(mUUID);
                        hasLiveStream = false;
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }

    @Override
    protected String onResolveViewIdentify() {
        return mUUID;
    }

    @Override
    public void onScreenRotationChanged(boolean land) {
        if (mView != null) {
            mView.onSpeaker(mIsSpeakerOn);
            mView.onMicrophone(mIsMicrophoneOn);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        AppLogger.d("stop" + getViewHandler());
        if (getViewHandler() != null) {
            if (hasLiveStream) {
                stopViewer().subscribe(s -> setViewHandler(null), AppLogger::e);
            }
        }
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
        stopViewer().observeOn(AndroidSchedulers.mainThread())
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
    }

    @Override
    public void switchSpeaker() {
        setSpeaker(!mIsSpeakerOn)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(on -> {
                    if (mView != null) {
                        mView.onSpeaker(on);
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                });
    }

    @Override
    public void switchMicrophone() {
        setMicrophone(!mIsMicrophoneOn)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(on -> {
                    if (mView != null) {
                        mView.onMicrophone(on);
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                });
    }

    private Observable<Boolean> setMicrophone(boolean on) {
        return Observable.just(on)
                .observeOn(Schedulers.io())
                .map(s -> {
                    AppLogger.d("正在切换 setMicrophone :" + on);
                    switchSpeakAndMicroPhone(mIsSpeakerOn, on);
                    mIsMicrophoneOn = on;
                    return s;
                }).subscribeOn(Schedulers.io());
    }

    protected Observable<Boolean> setSpeaker(boolean on) {
        return Observable.just(on)
                .observeOn(Schedulers.io())
                .map(s -> {
                    AppLogger.d("正在切换 Speaker :" + on);
                    boolean success = switchSpeakAndMicroPhone(on, mIsMicrophoneOn);
                    mIsSpeakerOn = success && on;
                    return mIsSpeakerOn;
                }).subscribeOn(Schedulers.io());
    }

    protected boolean switchSpeakAndMicroPhone(boolean speaker, boolean microphone) {
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
        appCmd.setAudio(false, microphone, speaker);//开启设备的扬声器和麦克风
        appCmd.setAudio(true, speaker, microphone);//开启客户端的扬声器和麦克风
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
            return mIsSpeakerOn;
        } else if (type == 1) {
            return mIsMicrophoneOn;
        }
        return false;
    }

    @Override
    public void onFrameFailed() {
        Schedulers.io().createWorker().schedule(() -> {
            try {
                AppLogger.d("AAAAAAAAAAAA");
                appCmd.stopPlay(mUUID);
                hasLiveStream = false;
                feedRtcp.stop();
            } catch (JfgException e) {
                e.printStackTrace();
            }
        });
        AndroidSchedulers.mainThread().createWorker().schedule(() -> mView.onVideoDisconnect(BAD_FRAME_RATE));
    }

    @Override
    public void onFrameRate(boolean slow) {
        mView.onLoading(slow);
    }
}
