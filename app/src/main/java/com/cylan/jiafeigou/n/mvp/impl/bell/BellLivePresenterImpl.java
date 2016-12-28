package com.cylan.jiafeigou.n.mvp.impl.bell;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cylan.entity.jniCall.JFGDoorBellCaller;
import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jfgapp.interfases.CallBack;
import com.cylan.jiafeigou.base.BasePresenter;
import com.cylan.jiafeigou.base.JFGView;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.utils.BitmapUtil;

import java.io.File;

/**
 * Created by cylan-hunt on 16-8-10.
 */
public class BellLivePresenterImpl extends BasePresenter<BellLiveContract.View> implements
        BellLiveContract.Presenter {

    private BeanBellInfo mBellInfo = null;
    private String mBellCid = null;
    private String mURL = null;
    private String mInLiveCid = null;

    private boolean mIsSpeakerOn = false;//麦克风是否打开标志
    private boolean mIsInViewMode = false;//是否是查看门铃模式
    private boolean isInLive = false;//是否正在直播中


    public BellLivePresenterImpl() {
    }


    @Override
    public void onPickup() {
        waitBellPictureReady(mURL, this::onWatchLive);
    }

    public void onWatchLive() {
        try {
            if (mInLiveCid != null) JfgCmdInsurance.getCmd().stopPlay(mInLiveCid);
            mInLiveCid = mBellCid;
            isInLive = true;
            mView.onViewer();
            onStartResolutionRetry();
            JfgCmdInsurance.getCmd().playVideo(mInLiveCid);
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String onResolveViewIdentify() {
        return mInLiveCid + "";
    }

    @Override
    public void onDismiss() {
        mInLiveCid = null;
        isInLive = false;
        stopLive();
    }

    @Override
    public void onSwitchSpeaker() {
        mView.onSpeaker(mIsSpeakerOn = !mIsSpeakerOn);
        if (mIsSpeakerOn) {//当前是开启状态
            JfgCmdInsurance.getCmd().setAudio(false, true, true);//开启设备的扬声器和麦克风
            JfgCmdInsurance.getCmd().setAudio(true, true, true);//开启客户端的扬声器和麦克风
        } else {//当前是关闭状态，则开启
            JfgCmdInsurance.getCmd().setAudio(true, false, false);
            JfgCmdInsurance.getCmd().setAudio(false, false, false);
        }
    }

    @Override
    public void onCapture() {
        JfgCmdInsurance.getCmd().screenshot(false, new CallBack<Bitmap>() {
            @Override
            public void onSucceed(Bitmap bitmap) {
                Toast.makeText(mView.getViewContext(), "截图成功", Toast.LENGTH_SHORT).show();
                String filePath = JConstant.MEDIA_PATH + File.separator + System.currentTimeMillis() + ".png";
                BitmapUtil.saveBitmap2file(bitmap, filePath);
            }

            @Override
            public void onFailure(String s) {
                Toast.makeText(mView.getViewContext(), "截图失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public BeanBellInfo getBellInfo() {
        return mBellInfo;
    }

    @Override
    public void onBellCall(String callWay, Object extra, Object extra1) {
        switch (callWay) {
            case JConstant.BELL_CALL_WAY_LISTEN:
                if (mIsInViewMode) return;//当正在查看门铃时忽略门铃呼叫
                mIsSpeakerOn = true;//接听门铃默认打开麦克风
                mIsInViewMode = false;
                JFGDoorBellCaller caller = (JFGDoorBellCaller) extra;
                mBellCid = caller.cid;
                mURL = caller.url;
                if (isInLive && TextUtils.equals(mInLiveCid, mBellCid)) {
                    onWatchLive();
                } else if (isInLive) {
                    mView.showAlert("有新访客", "有新的访客" + mBellCid, "接听", "忽略");
                } else {
                    mView.onListen();
                    waitBellPictureReady(mURL, () -> mView.onPreviewPicture(mURL));
                }
                break;
            case JConstant.BELL_CALL_WAY_VIEWER:
                mIsSpeakerOn = false;//查看门铃默认关闭麦克风
                mIsInViewMode = true;//当前正在查看门铃模式中
                mBellInfo = (BeanBellInfo) extra1;
                mBellInfo.deviceBase = (BaseBean) extra;
                mBellCid = mBellInfo.deviceBase.uuid;
                onWatchLive();
                break;
        }
        mView.onSpeaker(mIsSpeakerOn);
    }

    public void stopLive() {
        if (isInLive) {
            try {
                JfgCmdInsurance.getCmd().stopPlay(mInLiveCid);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        stopLive();
    }

    @Override
    public void onViewAction(int action, Object extra) {
        switch (action) {
            case JFGView.VIEW_ACTION_OK: {

            }
            break;

            case JFGView.VIEW_ACTION_CANCEL: {

            }
            break;
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
    protected void onVideoDisconnected(JFGMsgVideoDisconn disconnect) {

    }

    @Override
    protected int onResolveViewFeatures() {
        return BasePresenter.FEATURE_VIDEO_FLOW_RSP | BasePresenter.FEATURE_VIDEO_RESOLUTION;
    }

    @Override
    public void onScreenRotationChanged(boolean land) {
        mView.onSpeaker(mIsSpeakerOn);
    }

    private void waitBellPictureReady(String url, JFGView.Action action) {
        Glide.with(ContextUtils.getContext()).load(url).
                listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        SystemClock.sleep(200);
                        waitBellPictureReady(url, action);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (action != null) action.actionDone();
                        return false;
                    }
                }).preload();
    }

}
