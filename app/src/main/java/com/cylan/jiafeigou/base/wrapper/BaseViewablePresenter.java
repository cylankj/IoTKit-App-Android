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
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.base.view.ViewablePresenter;
import com.cylan.jiafeigou.base.view.ViewableView;
import com.cylan.jiafeigou.cache.SimpleCache;
import com.cylan.jiafeigou.misc.ApFilter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.live.IFeedRtcp;
import com.cylan.jiafeigou.misc.live.LiveFrameRateMonitor;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.utils.JfgUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
                .subscribe(load -> {
                    AppLogger.d("正在加载中" + load.slow);
                    if (load.slow && !sourceManager.isOnline() && liveStreamAction.hasStarted && !ApFilter.isAPMode(uuid)) {
                        AppLogger.d("无网络连接");
                        JFGMsgVideoDisconn disconn = new JFGMsgVideoDisconn();
                        disconn.code = BAD_NET_WORK;
                        disconn.remote = getViewHandler();
                        RxBus.getCacheInstance().post(disconn);

                    } else {
                        mView.onLoading(load.slow);
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
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
                .filter(isOnline -> {
                    if (!isOnline && !NetUtils.isNetworkAvailable(mView.getAppContext())) {
                        mView.onVideoDisconnect(BAD_NET_WORK);
                        liveStreamAction.reset();
                        return false;
                    }
                    return !liveStreamAction.hasStarted;
                })
                .map(account -> {
                    feedRtcp.stop();//清空之前的状态
                    feedRtcp.setMonitorListener(this);
                    mView.onViewer();
                    if (shouldShowPreview()) {
                        File file = new File(PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, ""));
                        mView.onShowVideoPreviewPicture(file.toString());
                    }
                    return getViewHandler();
                })
                .observeOn(Schedulers.io())
                .map(handle -> {
                    try {
                        AppLogger.d("正在准备开始直播,对端 cid 为:" + handle);
                        if (disconnectBeforePlay()) {
                            appCmd.stopPlay(handle);
                            AppLogger.d("播放前先发送断开消息!");
                        }
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
                        liveStreamAction.hasResolution = true;
                        if (mView != null) {
                            AppLogger.d("接收到分辨率消息,准备播放直播");
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
                    liveStreamAction.hasResolution = true;
                    if (!liveStreamAction.hasLiveError) {
                        feedRtcp.feed(rtcp);
                    }
                    if (mView != null) {
                        mView.onFlowSpeed(rtcp.bitRate);
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                    feedRtcp.stop();
                    feedRtcp.setMonitorListener(null);
                    e.printStackTrace();
                    if (e instanceof TimeoutException) {
                        AppLogger.d("连接设备超时,即将退出!");
                        liveStreamAction.reset();
                        try {
                            appCmd.stopPlay(getViewHandler());
                        } catch (JfgException e1) {
                            e1.printStackTrace();
                        }
//                        if (liveStreamAction.hasStarted) {
//                            if (mView != null) {
//                                mView.onVideoDisconnect(BAD_FRAME_RATE);
//                            }
//                        } else {
                        if (mView != null) {
                            mView.onConnectDeviceTimeOut();
                        }
//                        }
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

    protected boolean disconnectBeforePlay() {
        return false;
    }

    /**
     * stopViewer是被动的,dismiss是主动的,即stop虽然停止了直播,但不会清除播放状态
     * 这样当我们onPause时停止直播后可以在onResume中进行恢复,dismiss不仅会停止直播
     * 而且还会清除播放状态
     */
    protected Observable<Boolean> stopViewer() {
        AppLogger.e("stopViewer");
        if (!liveStreamAction.hasStarted) return Observable.empty();
        liveStreamAction.reset();
        feedRtcp.setMonitorListener(null);
        return Observable.just(getViewHandler())
                .subscribeOn(Schedulers.io())
                .map(handler -> {
                    if (!TextUtils.isEmpty(handler)) {
                        try {
                            JFGMsgVideoDisconn disconn = new JFGMsgVideoDisconn();
                            disconn.remote = getViewHandler();
                            disconn.code = STOP_VIERER_BY_SYSTEM;
                            RxBus.getCacheInstance().post(disconn);//结束 startView 的订阅链
                            AppLogger.d("正在发送停止直播消息:" + getViewHandler());
                            long start = System.currentTimeMillis();
                            byte[] screenshot = appCmd.screenshot(false);
                            appCmd.stopPlay(handler);
                            long end = System.currentTimeMillis();
                            AppLogger.e("花费时间:" + (end - start));
                            if (screenshot != null) {
                                int w = ((JfgAppCmd) BaseApplication.getAppComponent().getCmd()).videoWidth;
                                int h = ((JfgAppCmd) BaseApplication.getAppComponent().getCmd()).videoHeight;
                                removeLastPreview();
                                Bitmap bitmap = JfgUtils.byte2bitmap(w, h, screenshot);
                                String filePath = JConstant.MEDIA_PATH + File.separator + "." + uuid + System.currentTimeMillis();
                                PreferencesUtils.putString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, filePath);
                                Schedulers.io().createWorker().schedule(() -> BitmapUtils.saveBitmap2file(bitmap, filePath));
                                AppLogger.e("截图文件地址:" + filePath);
                            }
                            return true;
                        } catch (JfgException e) {
                            e.printStackTrace();
                            AppLogger.d("停止直播失败");
                        }
                    }
                    return false;
                });
    }

    private void removeLastPreview() {
        final String pre = PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid);
        if (TextUtils.isEmpty(pre)) return;
        try {
            if (SimpleCache.getInstance().getPreviewKeyList() != null) {
                List<String> list = new ArrayList<>(SimpleCache.getInstance().getPreviewKeyList());
                for (String key : list) {
                    if (!TextUtils.isEmpty(key) && key.contains(uuid)) {
                        SimpleCache.getInstance().removeCache(key);
                    }
                }
            }
        } catch (Exception e) {
        }
        Observable.just("go")
                .subscribeOn(Schedulers.io())
                .subscribe(ret -> FileUtils.deleteFile(pre), AppLogger::e);
    }

    protected Observable<RxEvent.LiveResponse> handleVideoResponse(String peer) {
        return Observable.merge(
                RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                        .subscribeOn(Schedulers.io())
                        .filter(rsp -> TextUtils.equals(rsp.peer, peer))
                        .map(RxEvent.LiveResponse::new),
                RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(dis -> {
                            AppLogger.d("视频连接断开了: remote:" + dis.remote + "code:" + dis.code);
                            if (mView != null) {
                                switch (dis.code) {
                                    case STOP_VIERER_BY_SYSTEM:
                                    case BAD_FRAME_RATE:
                                        break;
                                    default:
                                        mView.onVideoDisconnect(dis.code);
                                }
                            }
                            liveStreamAction.reset();
                            return new RxEvent.LiveResponse(dis, false);
                        })
        )
                .first()
                .timeout(30, TimeUnit.SECONDS)
                .map(rsp -> {
                    RxBus.getCacheInstance().post(new RxEvent.CallResponse(true));//发送一条 CallAnswer 消息表明不需要再等待门铃超时了
                    return rsp;
                });
    }

    protected Observable<JFGMsgVideoDisconn> handlerVideoDisconnect(JFGMsgVideoResolution resolution) {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                .observeOn(AndroidSchedulers.mainThread())
                .first(dis -> {
                    AppLogger.d("收到了断开视频的消息:" + dis.code);
                    liveStreamAction.reset();
                    feedRtcp.stop();
                    if (dis.code != STOP_VIERER_BY_SYSTEM) {
                        mView.onVideoDisconnect(dis.code);
                    }
                    return true;
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
    public void onResume() {
        super.onResume();
        feedRtcp.setMonitorListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getViewHandler() != null) {
            if (liveStreamAction.hasResolution) {
                stopViewer().subscribe(s -> setViewHandler(null), AppLogger::e);
            }
        }
    }

    protected void setViewHandler(String handler) {
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
        JFGMsgVideoDisconn disconn = new JFGMsgVideoDisconn();
        disconn.code = BAD_FRAME_RATE;
        RxBus.getCacheInstance().post(disconn);
        Schedulers.io().createWorker().schedule(() -> {
            if (TextUtils.isEmpty(getViewHandler())) return;
            try {
                appCmd.stopPlay(getViewHandler());
            } catch (Exception e) {
                AppLogger.e(e.getMessage());
            }
        });
    }

    @Override
    public void onFrameRate(boolean slow) {
        RxBus.getCacheInstance().post(new RxEvent.VideoLoadingEvent(slow));
    }
}
