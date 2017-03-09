package com.cylan.jiafeigou.n.mvp.impl.bell;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.cylan.jfgapp.interfases.CallBack;
import com.cylan.jiafeigou.base.wrapper.BaseCallablePresenter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.BitmapUtils;

import java.io.File;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-8-10.
 */
public class BellLivePresenterImpl extends BaseCallablePresenter<BellLiveContract.View> implements
        BellLiveContract.Presenter {

    public BellLivePresenterImpl() {
    }

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getDeviceUnBindSub());
    }

    private Subscription getDeviceUnBindSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceUnBindedEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(event -> TextUtils.equals(event.uuid, mUUID))
                .subscribe(event -> {
                    mView.onDeviceUnBind();
                }, Throwable::printStackTrace);
    }

    @Override
    public void capture() {
        JfgCmdInsurance.getCmd().screenshot(false, new CallBack<Bitmap>() {
            @Override
            public void onSucceed(Bitmap bitmap) {
                String filePath = JConstant.MEDIA_PATH + File.separator + System.currentTimeMillis() + ".png";
                BitmapUtils.saveBitmap2file(bitmap, filePath);
                mView.onTakeSnapShotSuccess(bitmap);

            }

            @Override
            public void onFailure(String s) {
                mView.onTakeSnapShotFailed();
            }
        });
    }
}
