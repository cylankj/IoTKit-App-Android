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
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yzd on 16-12-30.
 */

public abstract class BaseViewablePresenter<V extends ViewableView> extends BasePresenter<V> implements ViewablePresenter {
    protected boolean mIsSpeakerOn = false;
    protected String mViewLaunchType;

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
    }

    @Override
    public void startViewer() {
        Subscription subscribe = Observable.create((Observable.OnSubscribe<String>) subscriber -> {
            try {
                AppLogger.e("正在准备开始直播,对端 cid 为:" + getViewHandler());
                JfgCmdInsurance.getCmd().playVideo(getViewHandler());
                subscriber.onNext(getViewHandler());
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                AppLogger.e("准备开始直播失败!");
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(s -> {
                    mView.onViewer();
                    return s;
                })
                .flatMap(s -> Observable.merge(
                        RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                                .subscribeOn(Schedulers.io())
                                .filter(rsp -> TextUtils.equals(rsp.peer, s)).timeout(30, TimeUnit.SECONDS)
                                .first().map(RxEvent.LiveResponse::new),
                        RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .map(dis -> {
                                    AppLogger.e("视频连接断开了: remote:" + dis.remote + "code:" + dis.code);
                                    mView.onVideoDisconnect(dis.code);
                                    return new RxEvent.LiveResponse(dis);
                                })
                ).first())
                .filter(response -> response.success)
                .map(response -> response.resolution)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(rsp -> {
                    try {
                        AppLogger.d("接收到分辨率消息,准备播放直播");
                        mView.onResolution(rsp);
                        mViewLaunchType = onResolveViewIdentify();
                        RxBus.getCacheInstance().post(new RxEvent.CallAnswered(true));//发送一条 CallAnswer 消息表明自己成功连接了
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
                                                    case -1000000://dismiss 掉的,属于正常关闭
                                                        break;
                                                    default:
                                                        mView.onVideoDisconnect(dis.code);
                                                }
                                                return dis;
                                            })
                            );
                })
                .doOnUnsubscribe(() -> AppLogger.e("直播链取消订阅了"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rtcp -> {
                    AppLogger.e("流量信息更新");
                    mView.onFlowSpeed(rtcp.bitRate);
                }, e -> {
                    if (e instanceof TimeoutException) {
                        AppLogger.e("连接设备超时,即将退出!");
                        mView.onConnectDeviceTimeOut();
                    }
                });
        registerSubscription(subscribe);
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
                            JfgCmdInsurance.getCmd().stopPlay(handler);
                            JFGMsgVideoDisconn disconn = new JFGMsgVideoDisconn();
                            disconn.remote = getViewHandler();
                            disconn.code = -1000000;
                            RxBus.getCacheInstance().post(disconn);//结束 startView 的订阅链
                            AppLogger.e("正在发送停止直播消息");
                            return true;
                        } catch (JfgException e) {
                            e.printStackTrace();
                            AppLogger.e("停止直播失败");
                        }
                    }
                    return false;
                });
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
        if (getViewHandler() != null) {
            stopViewer().subscribe();
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

    protected Observable<Boolean> setSpeaker(boolean on) {
        return Observable.just(on).map(s -> {
            AppLogger.e("正在切换 Speaker :" + on);
            JfgCmdInsurance.getCmd().setAudio(false, on, on);//开启设备的扬声器和麦克风
            JfgCmdInsurance.getCmd().setAudio(true, on, on);//开启客户端的扬声器和麦克风
            return s;
        }).subscribeOn(Schedulers.io());
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
