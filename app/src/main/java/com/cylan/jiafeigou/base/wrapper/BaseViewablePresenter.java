package com.cylan.jiafeigou.base.wrapper;

import android.os.SystemClock;
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

import rx.Observable;
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

    protected Subscription mResolutionRetrySub;

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(
                getVideoDisconnectedSub(),
                getResolutionSub(),
                getVideoFlowRspSub()
        );
    }

    protected Subscription getVideoDisconnectedSub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                .subscribeOn(Schedulers.io())
                .filter(disconnectRsp -> TextUtils.equals(disconnectRsp.remote, onResolveViewIdentify()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onVideoDisconnected, Throwable::printStackTrace);
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
    }

    protected void onVideoFlowRsp(JFGMsgVideoRtcp flowRsp) {
        mView.onFlowSpeed(flowRsp.bitRate);
    }


    @Override
    public void startViewer() {
        if (mResolutionRetrySub != null && mResolutionRetrySub.isUnsubscribed()) {
            mResolutionRetrySub.unsubscribe();
        }
        mView.onViewer();
        mHasResolution = false;
        mInViewCallWay = mView.onResolveViewLaunchType();
        registerSubscription(getResolutionRetrySub());
    }

    protected Subscription getResolutionRetrySub() {
        return mResolutionRetrySub = Observable.interval(0, 10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(count -> {
                    try {
                        if (mHasResolution) {
                            unSubscribe(mResolutionRetrySub);
                            return;
                        }

                        if (!TextUtils.isEmpty(mInViewIdentify)) {
                            JfgCmdInsurance.getCmd().stopPlay(mInViewIdentify);
                            SystemClock.sleep(2000);
                        }
                        if (TextUtils.isEmpty(mInViewIdentify)) {
                            mInViewIdentify = onResolveViewIdentify();
                        }
                        AppLogger.d("正在进行第" + count + "次重试");
                        JfgCmdInsurance.getCmd().playVideo(mInViewIdentify);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);
    }

    /**
     * stopViewer是被动的,dismiss是主动的,即stop虽然停止了直播,但不会清除播放状态
     * 这样当我们onPause时停止直播后可以在onResume中进行恢复,dismiss不仅会停止直播
     * 而且还会清除播放状态
     */
    protected void stopViewer() {
        post(() -> {
            if (!TextUtils.isEmpty(mInViewIdentify)) {
                try {
                    mHasResolution = false;
                    JfgCmdInsurance.getCmd().stopPlay(mInViewIdentify);
                } catch (JfgException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    protected void onVideoResolution(JFGMsgVideoResolution resolution) {
        try {
            mHasResolution = true;
            unSubscribe(mResolutionRetrySub);
            mView.onResolution(resolution);
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
        if (!TextUtils.isEmpty(mInViewIdentify)) {
            startViewer();
        }
    }

    @Override
    public void dismiss() {
        stopViewer();
        mInViewIdentify = null;
        mHasResolution = true;
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
