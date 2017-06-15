package com.cylan.jiafeigou.n.view.panorama;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.wrapper.BaseViewablePresenter;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.ver.AbstractVersion;
import com.cylan.jiafeigou.misc.ver.PanDeviceVersionChecker;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.base.module.PanoramaEvent.ERROR_CODE_HTTP_NOT_AVAILABLE;
import static com.cylan.jiafeigou.dp.DpUtils.unpackData;

/**
 * Created by yanzhendong on 2017/3/8.
 */
public class PanoramaPresenter extends BaseViewablePresenter<PanoramaCameraContact.View> implements PanoramaCameraContact.Presenter {

    private boolean isFirst = true;
    private Subscription subscribe;
    private boolean shouldRefreshRecord = true;
    private int battery;

    @Override
    public void onViewAttached(PanoramaCameraContact.View view) {
        super.onViewAttached(view);
        BasePanoramaApiHelper.getInstance().init(uuid);
    }

    @Override
    public void onStart() {
        super.onStart();
        Device device = sourceManager.getDevice(uuid);
        if (device != null) {
            mView.onShowProperty(device);
        }
    }

    @Override
    public boolean isApiAvailable() {
        RxEvent.PanoramaApiAvailable event = RxBus.getCacheInstance().getStickyEvent(RxEvent.PanoramaApiAvailable.class);
        return event != null && event.ApiType >= 0;
    }

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getApiMonitorSub());
        registerSubscription(newVersionRspSub());
        registerSubscription(getReportMsgSub());
        registerSubscription(getNetWorkMonitorSub());
    }

    private Subscription getNetWorkMonitorSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.NetConnectionEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    AppLogger.e("监听到网络状态发生变化");
                    if (event.mobile != null && event.mobile.isConnected()) {
                        mView.onRefreshConnectionMode(1);
                    } else if (event.wifi != null && event.wifi.isConnected()) {
                        mView.onRefreshConnectionMode(0);
                    } else {
                        mView.onRefreshConnectionMode(-1);
                    }
                }, e -> {
                });
    }

    private Subscription newVersionRspSub() {
        Subscription subscription = RxBus.getCacheInstance().toObservable(AbstractVersion.BinVersion.class)
                .subscribeOn(Schedulers.newThread())
                .subscribe(version -> {
                    version.setLastShowTime(System.currentTimeMillis());
                    PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + uuid, new Gson().toJson(version));
                    mView.onNewFirmwareRsp();
                    //必须手动断开,因为rxBus订阅不会断开
                    throw new RxEvent.HelperBreaker(version);
                }, AppLogger::e);
        AbstractVersion<PanDeviceVersionChecker.BinVersion> version = new PanDeviceVersionChecker();
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        version.setPortrait(new AbstractVersion.Portrait().setCid(uuid).setPid(device.pid));
        version.setShowCondition(() -> {
            Device d = BaseApplication.getAppComponent().getSourceManager().
                    getDevice(uuid);
            DpMsgDefine.DPNet dpNet = d.$(201, new DpMsgDefine.DPNet());
            //设备离线就不需要弹出来
            if (!JFGRules.isDeviceOnline(dpNet)) {
                return false;
            }
            //局域网弹出
            if (!MiscUtils.isDeviceInWLAN(uuid)) return false;
            return true;
        });
        version.startCheck();
        return subscription;
    }

    private Subscription getReportMsgSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter(msg -> TextUtils.equals(msg.uuid, uuid))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    AppLogger.e("收到设备同步消息:" + new Gson().toJson(result));
                    try {
                        for (JFGDPMsg msg : result.dpList) {
                            if (msg.id == 204) {
                                DpMsgDefine.DPSdStatus status = unpackData(msg.packValue, DpMsgDefine.DPSdStatus.class);
                                if (status != null && !status.hasSdcard) {//SDCard 不存在
                                    mView.onReportDeviceError(2004, true);
                                } else if (status != null && status.err != 0) {//SDCard 需要格式化
                                    mView.onReportDeviceError(2022, true);
                                }
                                shouldRefreshRecord = status != null && status.hasSdcard && status.err == 0;
                            } else if (msg.id == 205) {
                                Integer charge = unpackData(msg.packValue, int.class);
                                if (charge != null && charge == 1) {
                                    mView.onDeviceBatteryChanged(-1);
                                } else if (charge != null && charge == 0) {
                                    mView.onDeviceBatteryChanged(battery);
                                }
                            } else if (msg.id == 206) {
                                Integer battery = unpackData(msg.packValue, int.class);
                                if (battery != null) {
                                    mView.onDeviceBatteryChanged(this.battery = battery);
                                }
                            }
                        }
                    } catch (Exception e) {
                        AppLogger.e(e.getMessage());
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
    }

    private Subscription getApiMonitorSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.PanoramaApiAvailable.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    if (ret.ApiType < 0) {
                        mView.onRefreshControllerView(false);
                    } else {
                        mView.onRefreshControllerView(true);
                        checkAndInitRecord();
                    }
                }, AppLogger::e);
    }

    @Override
    public void makePhotograph() {
        mView.onRefreshControllerViewVisible(false);
        Subscription subscribe = BasePanoramaApiHelper.getInstance().snapShot()
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msgFileRsp -> {
                    if (msgFileRsp.ret == 0) {
                        mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_PICTURE, true);
                        mView.onRefreshControllerViewVisible(true);
                        if (msgFileRsp.files != null && msgFileRsp.files.size() > 0) {
                            if (BasePanoramaApiHelper.getInstance().getDeviceIp() != null) {
                                mView.onShowPreviewPicture(msgFileRsp.files.get(0));
                            } else {
                                mView.onReportDeviceError(ERROR_CODE_HTTP_NOT_AVAILABLE, true);
                            }
                        }
                    } else {
                        mView.onReportDeviceError(msgFileRsp.ret, false);
                    }
                    AppLogger.d("拍照返回结果为:" + new Gson().toJson(msgFileRsp));
                }, e -> {
                    AppLogger.e(e);
                    mView.onReportDeviceError(-1, false);//timeout
                });
        registerSubscription(subscribe);
    }

    @Override
    protected boolean shouldShowPreview() {
        return false;
    }

    @Override
    public void checkAndInitRecord() {
        if (subscribe != null && subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
        }
        subscribe = BasePanoramaApiHelper.getInstance().getRecStatus()
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(rsp -> {
                    if (rsp != null && rsp.ret == 0) {//检查录像状态
                        shouldRefreshRecord = true;
                        mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_VIDEO, false);
                        refreshVideoRecordUI(rsp.seconds, rsp.videoType);
                        DataSourceManager.getInstance().pushDeviceState(uuid);

                    } else {
                        DataSourceManager.getInstance().removeDeviceState(uuid);
                    }
                    AppLogger.d("初始化录像状态结果为:" + new Gson().toJson(rsp));
                    return rsp;
                })
                .observeOn(Schedulers.io())
                .flatMap(ret -> BasePanoramaApiHelper.getInstance().getResolution())
                .observeOn(AndroidSchedulers.mainThread())
                .map(ret -> {
                    if (ret != null && ret.ret == 0) {
                        mView.onSwitchSpeedMode(ret.resolution);//检查分辨率
                    }
                    return ret;
                })
                .observeOn(Schedulers.io())
                .flatMap(ret -> BasePanoramaApiHelper.getInstance().getSdInfo())
                .observeOn(AndroidSchedulers.mainThread())
                .map(ret -> {
                    if (ret != null && ret.sdIsExist == 0) {//SDCard 不存在
//                        mView.onReportDeviceError(2004);
                        shouldRefreshRecord = false;
                    } else if (ret != null && ret.sdcard_recogntion != 0) {//SDCard 需要格式化
//                        mView.onReportDeviceError(2022);
                        shouldRefreshRecord = false;
                    }
                    return ret;
                })
                .observeOn(Schedulers.io())
                .flatMap(ret -> BasePanoramaApiHelper.getInstance().getBattery())
                .observeOn(AndroidSchedulers.mainThread())
                .map(ret -> {
                    if (ret != null) {
                        mView.onDeviceBatteryChanged(this.battery = ret.battery);
                        if (ret.battery < 20 && isFirst) {//检查电量
                            isFirst = false;
                            Device device = sourceManager.getDevice(uuid);
                            DBOption.DeviceOption option = device.option(DBOption.DeviceOption.class);
                            if (option != null && option.lastLowBatteryTime < TimeUtils.getTodayStartTime()) {//新的一天
                                option.lastLowBatteryTime = System.currentTimeMillis();
                                device.setOption(option);
                                sourceManager.updateDevice(device);
                                mView.onBellBatteryDrainOut();
                            }
                        }
                    }
                    return ret;
                })
                .observeOn(Schedulers.io())
                .flatMap(ret -> BasePanoramaApiHelper.getInstance().getPowerLine())
                .observeOn(AndroidSchedulers.mainThread())
                .map(ret -> {
                    if (ret != null && ret.powerline == 1) {
                        mView.onDeviceBatteryChanged(-1);
                    }
                    return ret;
                })

                .subscribe(ret -> {
                }, e -> {
                    AppLogger.e(e.getMessage());
                });

        registerSubscription(subscribe);
    }

    @Override
    public void switchVideoResolution(@PanoramaCameraContact.View.SPEED_MODE int mode) {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().setResolution(mode)
                .timeout(30, TimeUnit.SECONDS)
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
    public void startVideoRecord(int type) {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().startRec(type)
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    AppLogger.d("开启视频录制返回结果为" + new Gson().toJson(rsp));
                    if (rsp.ret == 0) {
                        AppLogger.d("开启视频录制成功了");
                        shouldRefreshRecord = true;
                        mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_VIDEO, false);
                        refreshVideoRecordUI(0, type);
                        DataSourceManager.getInstance().pushDeviceState(uuid);
                    } else {
                        shouldRefreshRecord = false;
                        mView.onReportDeviceError(rsp.ret, false);
                    }
                }, e -> {
                    AppLogger.e(e);
                    shouldRefreshRecord = false;
                    mView.onReportDeviceError(-1, false);
                });
        registerSubscription(subscribe);
    }

    @Override
    public void stopVideoRecord(int type) {
        shouldRefreshRecord = false;
        Subscription subscribe = BasePanoramaApiHelper.getInstance().stopRec(type)
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    if (ret.ret == 0 && ret.files != null && ret.files.size() > 0) {//成功了
                        mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_VIDEO, true);
                        mView.onRefreshControllerViewVisible(true);
                        if (BasePanoramaApiHelper.getInstance().getDeviceIp() != null) {
                            mView.onShowPreviewPicture(ret.files.get(0));
                            DataSourceManager.getInstance().removeDeviceState(uuid);
                        } else {
                            mView.onReportDeviceError(ERROR_CODE_HTTP_NOT_AVAILABLE, true);
                        }
                    } else {//失败了
                        mView.onReportDeviceError(ret.ret, false);
                    }
                    AppLogger.d("停止直播返回结果为:" + new Gson().toJson(ret));
                }, e -> {
                    mView.onReportDeviceError(-1, false);
                    AppLogger.e(e);
                });
        registerSubscription(subscribe);
    }

    public void refreshVideoRecordUI(int offset, @PanoramaCameraContact.View.PANORAMA_RECORD_MODE int type) {
        Subscription subscribe = Observable.interval(0, 500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(count -> (int) (count / 2) + offset)
                .takeUntil(second -> {
                    if (shouldRefreshRecord) {
                        if (type == PanoramaCameraContact.View.PANORAMA_RECORD_MODE.MODE_SHORT && second >= 8) {
                            shouldRefreshRecord = false;
                            mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_VIDEO, true);
                            DataSourceManager.getInstance().removeDeviceState(uuid);
                        }
                        mView.onRefreshVideoRecordUI(second, type);
                    }
                    return !shouldRefreshRecord;
                })
                .subscribe(ret -> {
                }, AppLogger::e);
        registerSubscription(subscribe);
    }
}
