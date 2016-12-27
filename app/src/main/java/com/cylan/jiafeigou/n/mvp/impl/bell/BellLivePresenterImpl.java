package com.cylan.jiafeigou.n.mvp.impl.bell;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
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
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.utils.BitmapUtil;
import com.cylan.utils.HandlerThreadUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-8-10.
 */
public class BellLivePresenterImpl extends AbstractPresenter<BellLiveContract.View> implements
        BellLiveContract.Presenter {

    private BeanBellInfo mBellInfo;
    private String mBellCid;
    private String mURL;
    private JFGDoorBellCaller mCaller;
    private CompositeSubscription mCompositeSubscription;
    private boolean isHold = false;
    private String mInHoldCallCid = null;
    private Subscription mRetrySubscription;

    private boolean isInViewer = false;

    public BellLivePresenterImpl(BellLiveContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void onPickup() {
        waitBellPictureReady(mURL, this::onWatchLive);
    }

    @Override
    public void onWatchLive() {
        try {
            if (mInHoldCallCid != null) JfgCmdInsurance.getCmd().stopPlay(mInHoldCallCid);
            mInHoldCallCid = String.copyValueOf(mBellCid.toCharArray());
            isHold = true;
            mView.onViewer();
            HandlerThreadUtils.postDelay(() -> {
                try {
                    JfgCmdInsurance.getCmd().playVideo(mInHoldCallCid);
                    mCompositeSubscription.add(bellRetrySubscription());
                } catch (JfgException e) {
                    e.printStackTrace();
                }
            }, 1000);
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDismiss() {
        try {
            JfgCmdInsurance.getCmd().stopPlay(mBellCid);
            mInHoldCallCid = null;
            isHold = false;
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMike(int on) {
        if (on == 1) {//on
            JfgCmdInsurance.getCmd().setAudio(false, true, true);//开启设备的扬声器和麦克风
            JfgCmdInsurance.getCmd().setAudio(true, true, true);//开启客户端的扬声器和麦克风
        } else {//off
            JfgCmdInsurance.getCmd().setAudio(true, false, false);
            JfgCmdInsurance.getCmd().setAudio(false, false, false);
        }
    }

    @Override
    public void onCapture() {
        JfgCmdInsurance.getCmd().screenshot(false, new CallBack<Bitmap>() {
            @Override
            public void onSucceed(Bitmap bitmap) {
                Toast.makeText(mView.getContext(), "截图成功", Toast.LENGTH_SHORT).show();
                String filePath = JConstant.MEDIA_PATH + File.separator + System.currentTimeMillis() + ".png";
                BitmapUtil.saveBitmap2file(bitmap, filePath);
            }

            @Override
            public void onFailure(String s) {
                Toast.makeText(mView.getContext(), "截图失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public BeanBellInfo getBellInfo() {
        return mBellInfo;
    }

    @Override
    public void setBellInfo(BeanBellInfo info) {
        mBellInfo = info;
        if (mBellInfo != null && mBellInfo.deviceBase != null) {
            mBellCid = mBellInfo.deviceBase.uuid;
        }
    }

    @Override
    public void processCall() {

    }

    @Override
    public void onBellCall(String callWay, Object extra, Object extra1) {
        switch (callWay) {
            case JConstant.BELL_CALL_WAY_LISTEN:
                if (isInViewer) return;
                mCaller = (JFGDoorBellCaller) extra;
                mBellCid = String.copyValueOf(mCaller.cid.toCharArray());
                mURL = String.copyValueOf(mCaller.url.toCharArray());
                Log.e(TAG, "onBellCall: " + mCaller.cid);
                if (isHold && TextUtils.equals(mInHoldCallCid, mBellCid)) {
                    onWatchLive();
                } else if (isHold) {
                    mView.onProcess(mBellCid);
                } else {
                    mView.onListen();
                    waitBellPictureReady(mURL, () -> mView.onPreviewPicture(mURL));
                }
                break;
            case JConstant.BELL_CALL_WAY_VIEWER:
                mBellInfo = (BeanBellInfo) extra1;
                mBellInfo.deviceBase = (BaseBean) extra;
                mBellCid = mBellInfo.deviceBase.uuid;
                isInViewer = true;
                onWatchLive();
                break;
        }
    }

    @Override
    public void onBellPaused() {
        if (isHold) {
            try {
                JfgCmdInsurance.getCmd().stopPlay(mInHoldCallCid);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start() {
        unSubscribe(mCompositeSubscription);
        mCompositeSubscription = new CompositeSubscription();
        mCompositeSubscription.add(resolutionNotifySub());
        mCompositeSubscription.add(flowNotifySub());
        mCompositeSubscription.add(videoDisconnectSub());
    }

    @Override
    public void stop() {
        unSubscribe(mCompositeSubscription);
        onBellPaused();
        unSubscribe(mRetrySubscription);
    }

    private Subscription resolutionNotifySub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                .filter(resolution -> TextUtils.equals(mInHoldCallCid, resolution.peer))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resolution -> {
                    unSubscribe(mRetrySubscription);
                    try {
                        mView.onResolution(resolution);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    AppLogger.e("resolution err: " + throwable.getLocalizedMessage());
                });
    }

    private Subscription flowNotifySub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rtcp -> {
                    mView.onFlowSpeedRefresh(rtcp.bitRate);
                });
    }

    private Subscription videoDisconnectSub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                .subscribeOn(Schedulers.newThread())
                .filter((JFGMsgVideoDisconn jfgMsgVideoDisconn) -> {
                    boolean notNull = getView() != null
                            && mBellInfo.deviceBase != null
                            && TextUtils.equals(mBellInfo.deviceBase.uuid, jfgMsgVideoDisconn.remote);
                    if (!notNull) {
                        AppLogger.e("err: " + mBellInfo);
                    }
                    return notNull;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((JFGMsgVideoDisconn jfgMsgVideoDisconn) -> {
                    getView().onLiveStop(jfgMsgVideoDisconn.code);
                }, (Throwable throwable) -> {
                    AppLogger.e("videoDisconnectSub:" + throwable.getLocalizedMessage());
                });
    }

    private Subscription bellRetrySubscription() {
        unSubscribe(mRetrySubscription);
        return mRetrySubscription = Observable.interval(15, 15, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .subscribe(aLong -> {
                    try {
                        JfgCmdInsurance.getCmd().stopPlay(mInHoldCallCid);
                        SystemClock.sleep(1000);
                        JfgCmdInsurance.getCmd().playVideo(mInHoldCallCid);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                });
    }

    interface L {
        void l();
    }

    private void waitBellPictureReady(String url, L l) {
        Glide.with(ContextUtils.getContext()).load(url).
                listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        SystemClock.sleep(200);
                        waitBellPictureReady(url, l);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (l != null) l.l();
                        return false;
                    }
                }).preload();
    }

}
