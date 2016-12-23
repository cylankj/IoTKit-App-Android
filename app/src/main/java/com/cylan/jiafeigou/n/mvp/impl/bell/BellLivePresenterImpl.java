package com.cylan.jiafeigou.n.mvp.impl.bell;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jfgapp.interfases.CallBack;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.utils.BitmapUtil;

import java.io.File;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-8-10.
 */
public class BellLivePresenterImpl extends AbstractPresenter<BellLiveContract.View> implements
        BellLiveContract.Presenter {

    private BeanBellInfo mBellInfo;

    private CompositeSubscription mCompositeSubscription;

    public BellLivePresenterImpl(BellLiveContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void onPickup() {
        try {
            JfgCmdInsurance.getCmd().playVideo(mBellInfo.deviceBase.uuid);
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDismiss() {
        try {
            JfgCmdInsurance.getCmd().stopPlay(mBellInfo.deviceBase.uuid);
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
    }

    @Override
    public void start() {
        unSubscribe(mCompositeSubscription);
        mCompositeSubscription = new CompositeSubscription();
        mCompositeSubscription.add(resolutionNotifySub());
        mCompositeSubscription.add(flowNotifySub());
    }

    @Override
    public void stop() {
        onDismiss();
        unSubscribe(mCompositeSubscription);

    }

    private Subscription resolutionNotifySub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                .filter(resolution -> TextUtils.equals(resolution.peer, mBellInfo.deviceBase.uuid))
//                .throttleFirst(1, TimeUnit.SECONDS)//滤波器
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resolution -> {
                    Log.e(TAG, "resolutionNotifySub: ssssssssssssssssssss");
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
}
