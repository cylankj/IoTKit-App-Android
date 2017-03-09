package com.cylan.jiafeigou.base.wrapper;

import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.view.ViewablePresenter;
import com.cylan.jiafeigou.base.view.ViewableView;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JfgCmdInsurance.getCmd;

/**
 * Created by yzd on 16-12-30.
 */

public abstract class BaseViewablePresenter<V extends ViewableView> extends BasePresenter<V> implements ViewablePresenter {
    protected boolean mIsMicrophoneOn = false;
    protected boolean hasResolution = false;
    protected boolean mIsSpeakerOn = false;

    protected String mViewLaunchType;


    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
    }

    @Override
    public void startViewer() {
        Subscription subscribe = Observable.just(NetUtils.isNetworkAvailable(mView.getAppContext()))
                .observeOn(AndroidSchedulers.mainThread())
                .filter(hasNet -> {
                    if (hasNet) {
                        mView.onViewer();
                        return true;
                    } else {
                        mView.onVideoDisconnect(ViewableView.BAD_NET_WORK);
                        return false;
                    }
                }).observeOn(Schedulers.io())
                .map(hasNet -> {
                    String handle = getViewHandler();
                    try {
                        AppLogger.d("正在准备开始直播,对端 cid 为:" + handle);
                        int ret = JfgCmdInsurance.getCmd().playVideo(handle);
                        if (ret != 0) {
                            JfgCmdInsurance.getCmd().stopPlay(handle);
                            JfgCmdInsurance.getCmd().playVideo(handle);
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
                        hasResolution = true;
                        mView.onResolution(rsp);
                        mViewLaunchType = onResolveViewIdentify();
                        RxBus.getCacheInstance().post(new BaseCallablePresenter.Notify(false));//发送一条 Notify 消息表明不需要再查询预览图了
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                            .takeUntil(handleVideoRTCP(rsp));
                })
                .doOnUnsubscribe(() -> AppLogger.d("直播链取消订阅了"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rtcp -> {
                    AppLogger.d("流量信息更新:" + rtcp.bitRate / 8 + "KB/S");
                    mView.onFlowSpeed(rtcp.bitRate);
                }, e -> {
                    if (e instanceof TimeoutException) {
                        AppLogger.d("连接设备超时,即将退出!");
                        if (hasResolution) {
                            mView.onVideoDisconnect(ViewableView.BAD_NET_WORK);
                        } else {
                            mView.onConnectDeviceTimeOut();
                        }
                    }
                });
        registerSubscription(subscribe);
    }

    public void cancelViewer() {
        stopViewer().subscribe();
    }

    /**
     * stopViewer是被动的,dismiss是主动的,即stop虽然停止了直播,但不会清除播放状态
     * 这样当我们onPause时停止直播后可以在onResume中进行恢复,dismiss不仅会停止直播
     * 而且还会清除播放状态
     */
    protected Observable<Boolean> stopViewer() {
        return Observable.just(getViewHandler())
                .subscribeOn(Schedulers.io())
                .map(handler -> {
                    if (!TextUtils.isEmpty(handler)) {
                        try {
                            getCmd().stopPlay(handler);
                            JFGMsgVideoDisconn disconn = new JFGMsgVideoDisconn();
                            disconn.remote = getViewHandler();
                            disconn.code = -1000000;
                            RxBus.getCacheInstance().post(disconn);//结束 startView 的订阅链
                            AppLogger.d("正在发送停止直播消息");
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
                            mView.onVideoDisconnect(dis.code);
                            return new RxEvent.LiveResponse(dis, false);
                        })
        ).first().map(rsp -> {
            RxBus.getCacheInstance().post(new RxEvent.CallResponse(true));//发送一条 CallAnswer 消息表明不需要再等待门铃超时了
            return rsp;
        });
    }

    protected Observable<JFGMsgVideoDisconn> handleVideoRTCP(JFGMsgVideoResolution resolution) {
        return
                RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                        .filter(dis -> TextUtils.equals(dis.remote, resolution.peer))
                        .mergeWith(
                                RxBus.getCacheInstance().toObservable(RxEvent.OnlineStatusRsp.class)
                                        .filter(event -> !event.state).map(event -> {
                                    JFGMsgVideoDisconn disconn = new JFGMsgVideoDisconn();
                                    disconn.code = ViewableView.BAD_NET_WORK;
                                    disconn.remote = getViewHandler();
                                    return disconn;
                                }))
                        .first()
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(dis -> {
                            AppLogger.e(dis.code + "AAA");
                            switch (dis.code) {
                                case ViewableView.BAD_NET_WORK:
                                    mView.onVideoDisconnect(dis.code);
                                    break;
                                case -100000:
                                    break;
                            }
                            return dis;
                        });
    }

    @Override
    protected String onResolveViewIdentify() {
        return mUUID;
    }

    @Override
    public void onScreenRotationChanged(boolean land) {
        mView.onSpeaker(mIsSpeakerOn);
        mView.onMicrophone(mIsMicrophoneOn);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getViewHandler() != null) {
            stopViewer().subscribe();
            setViewHandler(null);
        }
    }

    protected void setViewHandler(String handler) {
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    protected String getViewHandler() {
        return onResolveViewIdentify();
    }

    @Override
    public void dismiss() {
        stopViewer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    setViewHandler(null);
                    mView.onDismiss();
                }, e -> {

                });
    }

    @Override
    public void switchSpeaker() {
        setSpeaker(mIsSpeakerOn = !mIsSpeakerOn)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(on -> mView.onSpeaker(on), Throwable::printStackTrace);
    }

    @Override
    public void switchMicrophone() {
        setMicrophone(mIsMicrophoneOn = !mIsMicrophoneOn).observeOn(AndroidSchedulers.mainThread())
                .subscribe(on -> mView.onMicrophone(on), Throwable::printStackTrace);
    }

    private Observable<Boolean> setMicrophone(boolean on) {
        return Observable.just(on).map(s -> {
            AppLogger.d("正在切换 Speaker :" + on);
            mIsMicrophoneOn = on;
            switchSpeakAndMicroPhone();
            return s;
        }).subscribeOn(Schedulers.io());
    }

    protected Observable<Boolean> setSpeaker(boolean on) {
        return Observable.just(on).map(s -> {
            AppLogger.d("正在切换 Speaker :" + on);
            mIsSpeakerOn = on;
            switchSpeakAndMicroPhone();
            return s;
        }).subscribeOn(Schedulers.io());
    }

    protected void switchSpeakAndMicroPhone() {
        getCmd().setAudio(true, mIsSpeakerOn, mIsMicrophoneOn);//开启客户端的扬声器和麦克风
        getCmd().setAudio(false, mIsMicrophoneOn, mIsSpeakerOn);//开启设备的扬声器和麦克风
    }


    @Override
    public SurfaceView getViewerInstance() {
        SurfaceView surfaceView = (SurfaceView) VideoViewFactory.CreateRendererExt(false, mView.getAppContext(), true);
        surfaceView.setId("IVideoView".hashCode());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        surfaceView.setLayoutParams(params);
        return surfaceView;
    }
}
