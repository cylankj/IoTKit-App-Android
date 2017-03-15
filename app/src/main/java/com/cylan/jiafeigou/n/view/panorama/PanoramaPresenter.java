package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.module.JFGCameraDevice;
import com.cylan.jiafeigou.base.wrapper.BaseViewablePresenter;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/8.
 */

public class PanoramaPresenter extends BaseViewablePresenter<PanoramaCameraContact.View> implements PanoramaCameraContact.Presenter {

    @Override
    public void onStart() {
        super.onStart();
        JFGCameraDevice device = mSourceManager.getJFGDevice(mUUID);
        if (device != null) {
            mView.onShowProperty(device);
        }
    }

    @Override
    public void makePhotograph() {
        Observable.just((JFGCameraDevice) mSourceManager.getJFGDevice(mUUID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(dev -> {
                    if (dev.sdcard_storage == null || !dev.sdcard_storage.hasSdcard) {
                        //没有存储设备
                        mView.onSDCardUnMounted();
                        return false;
                    }
                    return true;
                })
                .filter(dev -> {
                    if (dev.sdcard_storage.total - dev.sdcard_storage.used < 1000) {
                        //存储设备内存不足
                        mView.onSDCardMemoryFull();
                        return false;
                    }
                    return true;
                })
                .observeOn(Schedulers.io());
    }

    @Override
    public void startMakeLongVideo() {

    }

    @Override
    public void stopMakeMakeLongVideo() {

    }

    @Override
    public void startMakeShortVideo() {

    }

    @Override
    public void stopMakeShortVideo() {

    }
}
