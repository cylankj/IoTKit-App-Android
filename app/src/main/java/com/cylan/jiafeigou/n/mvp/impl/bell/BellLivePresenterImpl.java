package com.cylan.jiafeigou.n.mvp.impl.bell;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

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
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.utils.BitmapUtil;

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
    private boolean isPaused = false;

    private Subscription mRetryBellLiveSubscription;

    public BellLivePresenterImpl(BellLiveContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void onPickup() {
        retryBellLive();
    }

    @Override
    public void onDismiss() {
        try {
            JfgCmdInsurance.getCmd().stopPlay(mBellCid);
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
                mCaller = (JFGDoorBellCaller) extra;
                mBellCid = mCaller.cid;
                mURL = mCaller.url;
                if (isPaused) {
                    mView.onViewer();
                    onPickup();
                }
                if (isHold) {
                    mView.onProcess(mBellCid);
                } else {
                    mView.onListen(mURL);
                }
                break;
            case JConstant.BELL_CALL_WAY_VIEWER:
                mBellInfo = (BeanBellInfo) extra1;
                mBellInfo.deviceBase = (BaseBean) extra;
                mBellCid = mBellInfo.deviceBase.uuid;
                mView.onViewer();
                onPickup();
                break;
        }
    }

    @Override
    public void onBellPaused() {
        onDismiss();
        isPaused = true;
        isHold = true;
    }

    @Override
    public void start() {
        unSubscribe(mCompositeSubscription);
        mCompositeSubscription = new CompositeSubscription();
        mCompositeSubscription.add(resolutionNotifySub());
        mCompositeSubscription.add(flowNotifySub());
        mCompositeSubscription.add(videoDisconnectSub());
        mCompositeSubscription.add(holdInProcessCall());
    }

    @Override
    public void stop() {
        unSubscribe(mCompositeSubscription);
        onBellPaused();
    }

    private Subscription resolutionNotifySub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resolution -> {
                    Log.e(TAG, "resolutionNotifySub: ssssssssssssssssssss");
                    try {
                        unSubscribe(mRetryBellLiveSubscription);
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

    private Subscription holdInProcessCall() {
        return RxBus.getCacheInstance().toObservable(RxEvent.BellCallEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bellCallEvent -> {
//                    mView.onInProcessCall(bellCallEvent.caller);
                });
    }

    private void retryBellLive() {
        mRetryBellLiveSubscription = Observable.interval(0, 2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    try {
                        Log.e(TAG, "onPickup: " + mBellCid);
                        JfgCmdInsurance.getCmd().playVideo(mBellCid);
                        isHold = true;
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                });
    }

}
