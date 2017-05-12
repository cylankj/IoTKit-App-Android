package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.module.PanoramaEvent;
import com.cylan.jiafeigou.base.wrapper.BaseViewablePresenter;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/8.
 */
public class PanoramaPresenter extends BaseViewablePresenter<PanoramaCameraContact.View> implements PanoramaCameraContact.Presenter {
    private boolean httpApiInitFinish;

    @Override
    public void onStart() {
        super.onStart();
        BasePanoramaApiHelper.getInstance().init(uuid);
        Device device = sourceManager.getDevice(uuid);
        if (device != null) {
            mView.onShowProperty(device);
        }
    }

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getNetWorkChangedSub());
        registerSubscription(getFetchDeviceInformationSub());
    }

    private Subscription getFetchDeviceInformationSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.FetchDeviceInformation.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    this.httpApiInitFinish = ret.success;
                    if (ret.success && liveStreamAction.hasResolution) {
                        mView.onEnableControllerView();
                    }
                    if (!ret.success) {
                        mView.onDisableControllerView();
                    }
                }, AppLogger::e);
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
                }, AppLogger::e);
    }

    @Override
    public void makePhotograph() {
        Subscription subscribe = checkSDCard()
                .flatMap(info -> BasePanoramaApiHelper.getInstance().snapShot())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msgFileRsp -> {
                    if (msgFileRsp.ret == 0) {
                        mView.onMakePhotoGraphSuccess();
                        if (msgFileRsp.files != null && msgFileRsp.files.size() > 0) {
                            mView.onShowPreviewPicture(BasePanoramaApiHelper.getInstance().getDeviceIp() + "/images/" + msgFileRsp.files.get(0));
                        }
                    } else {
                        mView.onMakePhotoGraphError(msgFileRsp.ret);
                    }
                    AppLogger.d("拍照返回结果为:" + new Gson().toJson(msgFileRsp));
                }, e -> {
                    AppLogger.e(e);
                    mView.onMakePhotoGraphFailed();
                });
        registerSubscription(subscribe);
    }

    @Override
    protected boolean shouldShowPreview() {
        return false;
    }

    @Override
    public void checkAndInitRecord() {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().getRecStatus()
                .observeOn(AndroidSchedulers.mainThread())
                .map(rsp -> {
                    if (rsp.ret == 0) {
                        mView.onStartVideoRecordSuccess(rsp.videoType);
                        refreshVideoRecordUI(rsp.seconds, rsp.videoType);
                    }
                    AppLogger.d("初始化录像状态结果为:" + new Gson().toJson(rsp));
                    return rsp;
                })
                .flatMap(ret -> BasePanoramaApiHelper.getInstance().getResolution())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp.ret == 0) {
                        mView.onSwitchSpeedMode(rsp.resolution);
                    }
                }, AppLogger::e);

        registerSubscription(subscribe);
    }

    @Override
    public void switchVideoResolution(@PanoramaCameraContact.View.SPEED_MODE int mode) {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().getHttpApi().flatMap(api -> api.setResolution(mode))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    AppLogger.d("切换模式返回结果为" + new Gson().toJson(ret));
                    if (ret.ret == 0) {
                        mView.onSwitchSpeedMode(mode);
                    } else {
                        AppLogger.d("切换模式失败了");
                    }
                }, e -> {
                    AppLogger.e(e);
                });
        registerSubscription(subscribe);
    }

    @Override
    public boolean isHttpApiInitFinished() {
        return httpApiInitFinish;
    }

    @Override
    public void startVideoRecord(int type) {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().startRec(type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    AppLogger.d("开启视频录制返回结果为" + new Gson().toJson(rsp));
                    if (rsp.ret == 0) {
                        AppLogger.d("开启视频录制成功了");
                        mView.onStartVideoRecordSuccess(type);
                        refreshVideoRecordUI(0, type);
                    } else {
                        mView.onStartVideoRecordError(type, rsp.ret);
                    }
                }, e -> {
                    AppLogger.e(e);
                    mView.onStartVideoRecordError(type, -88888);
                });
        registerSubscription(subscribe);
    }

    @Override
    public void stopVideoRecord(int type) {
        RxBus.getCacheInstance().post(new RecordFinish());
        Subscription subscribe = BasePanoramaApiHelper.getInstance().stopRec(type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    if (ret.ret == 0 && ret.files != null && ret.files.size() > 0) {//成功了
                        mView.onShowPreviewPicture(BasePanoramaApiHelper.getInstance().getDeviceIp() + "/thumb/" + ret.files.get(0).replaceAll("mp4", "thumb"));
                        mView.onStopVideoRecordSuccess(type);
                    } else {//失败了
                        mView.onStopVideoRecordError(type, ret.ret);
                    }
                    AppLogger.d("停止直播返回结果为:" + new Gson().toJson(ret));
                }, e -> {
                    mView.onStopVideoRecordError(type, -888888);
                    AppLogger.e(e);
                });
        registerSubscription(subscribe);
    }

    public void refreshVideoRecordUI(int offset, @PanoramaCameraContact.View.PANORAMA_RECORD_MODE int type) {
        Subscription subscribe = Observable.interval(0, 500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(count -> (int) (count / 2) + offset)
                .takeUntil(second -> {
                    boolean finish = type == PanoramaCameraContact.View.PANORAMA_RECORD_MODE.MODE_SHORT && second >= 8;
                    if (finish) {
                        mView.onStopVideoRecordSuccess(type);
                    }
                    return finish;
                })
                .takeUntil(RxBus.getCacheInstance().toObservable(RecordFinish.class))
                .subscribe(second -> mView.onRefreshVideoRecordUI(second, type), AppLogger::e);
        registerSubscription(subscribe);
    }

    private Observable<PanoramaEvent.MsgSdInfoRsp> checkSDCard() {
        return BasePanoramaApiHelper.getInstance().getSdInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(info -> {
                    AppLogger.d("检查 SD卡结果为" + new Gson().toJson(info));
                    if (info.sdIsExist == 0) {
                        mView.onSDCardUnMounted();
                        return false;
                    }
                    if (info.sdcard_recogntion != 0) {
                        mView.onSDCardError(info.sdcard_recogntion);
                        return false;
                    }
                    return true;
                })
                .observeOn(Schedulers.io());
    }

    private Observable<PanoramaEvent.MsgBatteryRsp> checkBattery() {
        return BasePanoramaApiHelper.getInstance().getBattery()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(battery -> {
                    AppLogger.d("检查设备电量返回结果为:" + battery);
                    if (battery.battery < 5) {
                        mView.onDeviceBatteryLow();
                        return false;
                    }
                    return true;
                });
    }
}
