package com.cylan.jiafeigou.base.wrapper;

import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.view.ViewablePresenter;
import com.cylan.jiafeigou.base.view.ViewableView;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

/**
 * Created by yzd on 16-12-30.
 */

public abstract class BaseViewablePresenter<V extends ViewableView> extends BasePresenter<V> implements ViewablePresenter {

    protected String mInViewIdentify = null;
    protected String mInViewCallWay = null;
    protected boolean mIsSpeakerOn = false;

    @Override
    protected long onResolveViewFeatures() {
        return BasePresenter.FEATURE_VIDEO_FLOW_RSP | BasePresenter.FEATURE_VIDEO_RESOLUTION;
    }

    @Override
    public void startViewer() {
        try {
            if (mInViewIdentify != null) JfgCmdInsurance.getCmd().stopPlay(mInViewIdentify);
            mInViewCallWay = mView.onResolveViewLaunchType();
            mInViewIdentify = onResolveViewIdentify();
            mView.onViewer();
            listenResolution();
            JfgCmdInsurance.getCmd().playVideo(mInViewIdentify);
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    /**
     * stopViewer是被动的,dismiss是主动的,即stop虽然停止了直播,但不会清除播放状态
     * 这样当我们onPause时停止直播后可以在onResume中进行恢复,dismiss不仅会停止直播
     * 而且还会清除播放状态
     */
    protected void stopViewer() {
        if (!TextUtils.isEmpty(mInViewIdentify)) {
            try {
                JfgCmdInsurance.getCmd().stopPlay(mInViewIdentify);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onVideoResolution(JFGMsgVideoResolution resolution) {
        try {
            mView.onResolution(resolution);
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onVideoFlowRsp(JFGMsgVideoRtcp flowRsp) {
        mView.onFlowSpeed(flowRsp.bitRate);
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
    public void dismiss() {
        stopViewer();
        mInViewIdentify = null;
        mView.onDismiss();
    }

    @Override
    public void switchSpeaker() {
        mView.onSpeaker(mIsSpeakerOn = !mIsSpeakerOn);
        setSpeaker(mIsSpeakerOn);
    }

    protected void setSpeaker(boolean on) {
        if (on) {//当前是开启状态
            JfgCmdInsurance.getCmd().setAudio(false, true, true);//开启设备的扬声器和麦克风
            JfgCmdInsurance.getCmd().setAudio(true, true, true);//开启客户端的扬声器和麦克风
        } else {//当前是关闭状态，则开启
            JfgCmdInsurance.getCmd().setAudio(true, false, false);
            JfgCmdInsurance.getCmd().setAudio(false, false, false);
        }
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
