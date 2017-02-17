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
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yzd on 16-12-30.
 */

public abstract class BaseViewablePresenter<V extends ViewableView> extends BasePresenter<V> implements ViewablePresenter {

    protected String mInViewIdentify = null;
    protected String mInViewCallWay = null;
    protected boolean mIsSpeakerOn = false;

    protected boolean mHasResolution = false;

    protected Subscription mConnectDeviceTimeOut;

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
//        registerSubscription(
//                getVideoDisconnectedSub(),
//                getResolutionSub(),
//                getVideoFlowRspSub()
//        );
    }

    protected Subscription getConnectDeviceTimeOutSub() {
        AppLogger.e("getConnectDeviceTimeOutSub");
        return mConnectDeviceTimeOut = Observable.just(null)
                .delay(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    mView.onConnectDeviceTimeOut();
                    AppLogger.e("onConnectDeviceTimeOut");
                });
    }

    protected Subscription getVideoDisconnectedSub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                .subscribeOn(Schedulers.io())
                .filter(disconnectRsp -> TextUtils.equals(disconnectRsp.remote, onResolveViewIdentify()))
                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(this::onVideoDisconnected, Throwable::printStackTrace);
                .subscribe(jfgMsgVideoDisconn -> {
                    AppLogger.d("视频连接断开了");
                    onVideoDisconnected(jfgMsgVideoDisconn);
                }, Throwable::printStackTrace);
    }


    protected Subscription getResolutionSub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                .subscribeOn(Schedulers.io())
                .filter(videoResolutionRsp -> TextUtils.equals(onResolveViewIdentify(), videoResolutionRsp.peer))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videoResolutionRsp -> {
                    mHasResolution = true;
                    onVideoResolution(videoResolutionRsp);
                }, Throwable::printStackTrace);
    }

    protected Subscription getVideoFlowRspSub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onVideoFlowRsp, Throwable::printStackTrace);
    }

    protected void onVideoDisconnected(JFGMsgVideoDisconn disconnect) {
        AppLogger.e("onVideoDisconnected remote:" + disconnect.remote + ": code:" + disconnect.code);
        mView.onConnectDeviceTimeOut();
    }

    protected void onVideoFlowRsp(JFGMsgVideoRtcp flowRsp) {
        mView.onFlowSpeed(flowRsp.bitRate);
    }


    @Override
    public void startViewer() {
//        mView.onViewer();
//        mInViewCallWay = mView.onResolveViewLaunchType();
//        if (TextUtils.isEmpty(mInViewIdentify))
//            mInViewIdentify = onResolveViewIdentify();
//        try {
//            if (!mHasResolution) {
//                AppLogger.e("startView" + mInViewIdentify);
//                JfgCmdInsurance.getCmd().playVideo(mInViewIdentify);
//            }
//        } catch (JfgException e) {
//            e.printStackTrace();
//        }
//        registerSubscription(getConnectDeviceTimeOutSub());

        Observable.create((Observable.OnSubscribe<String>) subscriber -> {
            try {
                mView.onViewer();
                AppLogger.e("StartView:" + getViewHandler());
                JfgCmdInsurance.getCmd().playVideo(getViewHandler());
                subscriber.onNext(getViewHandler());
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                AppLogger.e("StartVideo 失败!");
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(peer -> RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                        .filter(rsp -> TextUtils.equals(rsp.peer, peer))
                        .first()
                        .timeout(30, TimeUnit.SECONDS).map(rsp -> {
                            AppLogger.e("连接设备超时!!!");
                            mView.onConnectDeviceTimeOut();
                            return rsp;
                        }))
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(rsp -> {
                    try {
                        AppLogger.e("接收到分辨率消息");
                        mView.onResolution(rsp);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                            .takeUntil(
                                    RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                                            .filter(dis -> TextUtils.equals(dis.remote, rsp.peer))
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .map(dis -> {
                                                AppLogger.e("视频连接断开了,错误码为:" + dis.code);
                                                switch (dis.code) {

                                                    default://dismiss 掉的,属于正常关闭

                                                }
                                                return dis;
                                            })
                            );
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rtcp -> {
                    AppLogger.e("onFlow");
                    mView.onFlowSpeed(rtcp.bitRate);
                }, e -> {
                    if (e instanceof TimeoutException) {
                        AppLogger.e("出错了, aaaaaaa");
                    }
                });
    }

    /**
     * stopViewer是被动的,dismiss是主动的,即stop虽然停止了直播,但不会清除播放状态
     * 这样当我们onPause时停止直播后可以在onResume中进行恢复,dismiss不仅会停止直播
     * 而且还会清除播放状态
     */
    protected void stopViewer() {
        if (!TextUtils.isEmpty(mInViewIdentify)) {
            try {
                mHasResolution = false;
                AppLogger.d("stopViewer");
                JfgCmdInsurance.getCmd().stopPlay(mInViewIdentify);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }

        Observable.create((Observable.OnSubscribe<String>) subscriber -> {
            try {
                JfgCmdInsurance.getCmd().stopPlay(getViewHandler());
                subscriber.onNext(getViewHandler());
                subscriber.onCompleted();
                AppLogger.e("停止直播成功");
            } catch (JfgException e) {
                e.printStackTrace();
                subscriber.onError(e);
                AppLogger.e("停止直播失败");
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe(s -> {
                    JFGMsgVideoDisconn disconn = new JFGMsgVideoDisconn();
                    disconn.code = -1000000;
                    RxBus.getCacheInstance().post(disconn);//结束 startView 的订阅链
                }, Throwable::printStackTrace);
    }


    protected void onVideoResolution(JFGMsgVideoResolution resolution) {
        try {
            unSubscribe(mConnectDeviceTimeOut);
            mView.onResolution(resolution);
            mView.onSpeaker(mIsSpeakerOn);
            mHasResolution = true;
            setSpeaker(mIsSpeakerOn);
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String onResolveViewIdentify() {
        return mUUID;
    }

    @Override
    public void onScreenRotationChanged(boolean land) {
        mView.onSpeaker(mIsSpeakerOn);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopViewer();
    }

    @Override
    public void onStart() {
        super.onStart();
//        if (!TextUtils.isEmpty(mInViewIdentify) && mHasResolution) { //已有分辨率消息说明已经接通,则恢复
//            startViewer();
//        }
        if (getViewHandler() != null) {
            startViewer();
        }
    }

    protected String getViewHandler() {

    }

    @Override
    public void dismiss() {
        post(() -> {
            stopViewer();
            mInViewIdentify = null;
        });
        mView.onDismiss();
    }

    @Override
    public void switchSpeaker() {
        mView.onSpeaker(mIsSpeakerOn = !mIsSpeakerOn);
        setSpeaker(mIsSpeakerOn);
    }

    protected void setSpeaker(boolean on) {
        post(() -> {
            if (on) {//当前是开启状态
                JfgCmdInsurance.getCmd().setAudio(false, true, true);//开启设备的扬声器和麦克风
                JfgCmdInsurance.getCmd().setAudio(true, true, true);//开启客户端的扬声器和麦克风
            } else {//当前是关闭状态，则开启
                JfgCmdInsurance.getCmd().setAudio(true, false, false);
                JfgCmdInsurance.getCmd().setAudio(false, false, false);
            }
        });
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
