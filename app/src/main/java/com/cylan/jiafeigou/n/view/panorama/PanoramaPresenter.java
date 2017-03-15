package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.module.JFGCameraDevice;
import com.cylan.jiafeigou.base.wrapper.BaseViewablePresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;

import rx.Observable;
import rx.Subscription;
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
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getNetWorkChangedSub());
    }

    private Subscription getNetWorkChangedSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.NetConnectionEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event.mobile != null && event.mobile.isConnected()) {
                        //移动网络,提醒用户注意流量
                        mView.onNetWorkChangedToMobile();
                    } else if (event.wifi != null && event.wifi.isConnected()) {
                        //wifi 网络,关闭流量提醒
                        mView.onNetWorkChangedToWiFi();
                    }
                });

    }

    @Override
    public void makePhotograph() {
//        verifySDCard(mSourceManager.getJFGDevice(mUUID))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(dev -> {
//                  mView.onMakePhotoGraphPreview();
//                });
        mView.onMakePhotoGraphPreview();
    }

    @Override
    public void startMakeLongVideo() {
        verifySDCard(mSourceManager.getJFGDevice(mUUID))
                .observeOn(Schedulers.io());
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

    private Observable<JFGCameraDevice> verifySDCard(JFGCameraDevice device) {
        return Observable.just(device)
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
                });
    }

    private Observable<JFGCameraDevice> verifyBattery(JFGCameraDevice device) {
        return Observable.just(device)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(dev -> {
                    if (dev.battery == null || dev.battery.value < 5) {
                        //电量低于5%
                        mView.onDeviceBatteryLow();
                        return false;
                    }
                    return true;
                });
    }


}
