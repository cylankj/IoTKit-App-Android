package com.cylan.jiafeigou.n.view.panorama;

import android.text.TextUtils;

import com.cylan.entity.JfgEvent;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.BaseDeviceInformationFetcher;
import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.PanoramaEvent;
import com.cylan.jiafeigou.base.wrapper.BaseViewablePresenter;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
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

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.base.module.PanoramaEvent.ERROR_CODE_HTTP_NOT_AVAILABLE;
import static com.cylan.jiafeigou.dp.DpUtils.pack;
import static com.cylan.jiafeigou.dp.DpUtils.unpackData;

/**
 * Created by yanzhendong on 2017/3/8.
 */
public class PanoramaPresenter extends BaseViewablePresenter<PanoramaCameraContact.View> implements PanoramaCameraContact.Presenter {

    private boolean isFirst = true;
    private Subscription subscribe;
    //    private boolean shouldRefreshRecord = false;
    private volatile int battery;
    private volatile boolean charge = false;
    private boolean notifyBatteryLow = true;
    private volatile boolean isRecording = false;
    private volatile boolean isRtmpLive = false;
    private volatile boolean hasSDCard;

    @Override
    public boolean isApiAvailable() {
        RxEvent.PanoramaApiAvailable event = RxBus.getCacheInstance().getStickyEvent(RxEvent.PanoramaApiAvailable.class);
        return event != null && event.ApiType >= 0;
    }

    @Override
    public void cameraLiveRtmpCtrl(int livePlatform, String url, int enable) {
        Observable.just("cameraLiveRtmpCtrl")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        ArrayList<JFGDPMsg> params = new ArrayList<>();
                        JFGDPMsg msg = new JFGDPMsg(516, 0, DpUtils.pack(new DpMsgDefine.DPCameraLiveRtmpCtrl(url, enable)));
                        params.add(msg);
                        return BaseApplication.getAppComponent().getCmd().robotSetData(uuid, params);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(MiscUtils.getErr(e));
                    }
                    return -1L;
                })
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(JfgEvent.RobotoSetDataRsp.class).filter(robotoSetDataRsp -> robotoSetDataRsp.seq == seq))
                .map(rsp -> {
                    if (rsp != null && rsp.dataList != null && rsp.dataList.size() > 0) {
                        JFGDPMsgRet msgRet = rsp.dataList.get(0);
                        return msgRet.ret;
                    }
                    return -1;
                })
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp == null) {
                        // TODO: 2017/9/9 超时了
                        AppLogger.w("发送516超时了");
                    } else {
                        mView.onSendCameraRtmpResponse(rsp);
                    }
                }, e -> {
                    AppLogger.e(MiscUtils.getErr(e));
                });
    }

    @Override
    public void onViewAttached(PanoramaCameraContact.View view) {
        super.onViewAttached(view);
        Device device = DataSourceManager.getInstance().getDevice(uuid);

        DpMsgDefine.DPSdStatus status = device.$(204, new DpMsgDefine.DPSdStatus());

        hasSDCard = status.hasSdcard;
    }

    @Override
    public void onStart() {
        super.onStart();
        BaseDeviceInformationFetcher.getInstance().init(uuid);
        DataSourceManager.getInstance().syncAllProperty(uuid);
    }

    @Override
    protected boolean disconnectBeforePlay() {
        return true;
    }

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
//        registerSubscription(getApiMonitorSub());
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, newVersionRspSub());
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, getReportMsgSub());
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, getNetWorkMonitorSub());
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, getDeviceRecordStateSub());
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, makeNewMsgSub());
    }

    private Subscription getDeviceRecordStateSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceRecordStateChanged.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    AppLogger.w("设备录像状态发生了变化");
                    PanoramaEvent.MsgVideoStatusRsp deviceState = (PanoramaEvent.MsgVideoStatusRsp) sourceManager.getDeviceState(uuid);
                    if (deviceState != null && deviceState.ret == 0 && deviceState.videoType == 2) {//只处理长路像的情况
                        if (!isRecording) {
                            mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_VIDEO, false, true);
                            if (deviceState.videoType != 3) {
                                isRecording = true;
                                mView.onRefreshVideoRecordUI(deviceState.seconds, deviceState.videoType);
                                refreshVideoRecordUI(deviceState.seconds, deviceState.videoType);
                            }
                        }
                        AppLogger.w("有录像状态:" + new Gson().toJson(deviceState));
                    } else if (deviceState == null) {
//                        if (shouldRefreshRecord) {
//                            shouldRefreshRecord = false;
                        AppLogger.w("无录像状态:" + new Gson().toJson(deviceState));
                        if (isRecording) {
                            RxBus.getCacheInstance().post(PanoramaCameraContact.View.RecordFinishEvent.INSTANCE);
                            mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_VIDEO, true, false);
                        }
//                        mView.onRefreshControllerViewVisible(true);
//                        }
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
    }

    private Subscription getNetWorkMonitorSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.NetConnectionEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    AppLogger.w("监听到网络状态发生变化");
                    BaseDeviceInformationFetcher.getInstance().init(uuid);
                    if (event.mobile != null && event.mobile.isConnected()) {
                        mView.onRefreshConnectionMode(event.mobile.getType());
                    } else if (event.wifi != null && event.wifi.isConnected()) {
                        mView.onRefreshConnectionMode(event.wifi.getType());
                    } else {
                        liveStreamAction.reset();
                        mView.onRefreshConnectionMode(-1);
                    }
                }, e -> {
                });
    }

    private Subscription newVersionRspSub() {
        Subscription subscription = RxBus.getCacheInstance().toObservable(AbstractVersion.BinVersion.class)
                .subscribeOn(Schedulers.io())
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
                    AppLogger.w("收到设备同步消息:" + new Gson().toJson(result));
                    try {
                        for (JFGDPMsg msg : result.dpList) {
                            //屏蔽掉204 消息
                            if (msg.id == 222) {//? 204 或者 222?
                                DpMsgDefine.DPSdcardSummary sdcardSummary = null;
                                try {
                                    sdcardSummary = unpackData(msg.packValue, DpMsgDefine.DPSdcardSummary.class);
                                } catch (Exception e) {
//                                    DpMsgDefine.DPSdStatusInt statusInt = unpackData(msg.packValue, DpMsgDefine.DPSdStatusInt.class);
//                                    status = new DpMsgDefine.DPSdStatus();
//                                    status.total = statusInt.total;
//                                    status.used = statusInt.used;
//                                    status.err = statusInt.err;
//                                    status.hasSdcard = statusInt.hasSdcard == 1;
                                }
                                AppLogger.w("204:" + new Gson().toJson(sdcardSummary));
                                if (sdcardSummary != null && !sdcardSummary.hasSdcard && hasSDCard) {//SDCard 不存在
                                    mView.onReportDeviceError(2004, true);
                                } else if (sdcardSummary != null && sdcardSummary.errCode != 0) {//SDCard 需要格式化
//                                    mView.onReportDeviceError(2022, true);//只有SD 卡不存在才弹
                                }
                                hasSDCard = sdcardSummary != null && sdcardSummary.hasSdcard;
//                                shouldRefreshRecord = status != null && status.hasSdcard && status.err == 0;
                            } else if (msg.id == 204) {
                                // TODO: 2017/8/17 AP 模式下发的是204 消息,需要特殊处理
                                Device device = DataSourceManager.getInstance().getDevice(uuid);
//                                if (JFGRules.isAPDirect(uuid, device.$(202, ""))) {
                                DpMsgDefine.DPSdStatus status = unpackData(msg.packValue, DpMsgDefine.DPSdStatus.class);
                                if (status != null && !status.hasSdcard && hasSDCard) {//SDCard 不存在
                                    mView.onReportDeviceError(2004, true);
                                } else if (status != null && status.err != 0) {//SDCard 需要格式化
//                                    mView.onReportDeviceError(2022, true);
                                }
                                hasSDCard = status != null && status.hasSdcard;
//                                }
                            } else if (msg.id == 205) {
                                charge = unpackData(msg.packValue, boolean.class);
                                if (charge) {
                                    mView.onDeviceBatteryChanged(-1);
                                } else {
                                    mView.onDeviceBatteryChanged(battery);
                                }
                                AppLogger.w("charge:" + charge);
                            } else if (msg.id == 206) {
                                Integer battery = unpackData(msg.packValue, int.class);
                                if (battery != null) {
                                    mView.onDeviceBatteryChanged(this.battery = battery);
                                }
                                if (this.battery <= 20 && notifyBatteryLow) {
                                    mView.onBellBatteryDrainOut();
                                    notifyBatteryLow = false;
                                } else if (this.battery > 20) {
                                    notifyBatteryLow = true;
                                }
                                AppLogger.w("battery:" + battery);
                            } else if (msg.id == 201) {
                                DpMsgDefine.DPNet dpNet = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPNet.class);

                                if (dpNet != null && dpNet.net > 0) {
                                    mView.onDeviceOnLine();
                                }
                            } else if (msg.id == 517) {
                                DpMsgDefine.DPCameraLiveRtmpStatus dpCameraLiveRtmpStatus = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPCameraLiveRtmpStatus.class);
                                if (dpCameraLiveRtmpStatus != null) {
                                    AppLogger.w("收到 rtmp 消息,url is: " + dpCameraLiveRtmpStatus.url
                                            + ",liveType is:" + dpCameraLiveRtmpStatus.liveType
                                            + ",flag is:" + dpCameraLiveRtmpStatus.flag
                                            + ",timestamp is:" + dpCameraLiveRtmpStatus.timestamp
                                            + ",error is:" + dpCameraLiveRtmpStatus.error
                                    );

                                    if (dpCameraLiveRtmpStatus.error != 0) {
                                        // TODO: 2017/9/9 出错了
                                        isRtmpLive = false;
                                    } else if (dpCameraLiveRtmpStatus.flag != 2) {
                                        // TODO: 2017/9/9 直播还未开始
                                        isRtmpLive = false;
                                    } else {

                                    }

                                }
                            }
                        }
                    } catch (Exception e) {
                        AppLogger.e(MiscUtils.getErr(e));
                    }
                }, e -> {
                    AppLogger.e(MiscUtils.getErr(e));
                });
    }

    @Override
    public void makePhotograph() {
        mView.onRefreshControllerViewVisible(false);
        Subscription subscribe = BasePanoramaApiHelper.getInstance().snapShot(uuid)
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msgFileRsp -> {
                    if (msgFileRsp.ret == 0) {
                        mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_PICTURE, true, false);
                        mView.onRefreshControllerViewVisible(true);
                        if (msgFileRsp.files != null && msgFileRsp.files.size() > 0) {
                            mView.onShowPreviewPicture(null);
                            if (BasePanoramaApiHelper.getInstance().getDeviceIp() == null) {
                                mView.onReportDeviceError(ERROR_CODE_HTTP_NOT_AVAILABLE, true);
                            }
                        }
                    } else {
                        if (msgFileRsp.ret == 2004) {
                            hasSDCard = false;
                        }
                        mView.onReportDeviceError(msgFileRsp.ret, false);
                    }
                    AppLogger.w("拍照返回结果为:" + new Gson().toJson(msgFileRsp));
                }, e -> {
                    AppLogger.e(e);
                    mView.onReportDeviceError(-1, false);//timeout
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, subscribe);
    }

    @Override
    protected boolean shouldShowPreview() {
        return false;
    }

    @Override
    public void checkAndInitRecord() {
        if (subscribe != null && !subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
        }

        subscribe = BasePanoramaApiHelper.getInstance().getUpgradeStatus(uuid)
                .onErrorResumeNext(Observable.just(null))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> {
                    boolean isUpgrade = ret != null && ret.upgradeStatus == 1;
                    if (isUpgrade) {
                        cancelViewer();
                        mView.onVideoDisconnect(-3);
                    }
                    return !isUpgrade;
                })
                .observeOn(Schedulers.io())
                .flatMap(ret -> BasePanoramaApiHelper.getInstance().getRecStatus(uuid).onErrorResumeNext(Observable.just(null)))
                .observeOn(AndroidSchedulers.mainThread())
                .map(rsp -> {
                    if (rsp != null && rsp.ret == 0 && rsp.videoType != 3) {//检查录像状态
//                        if (!shouldRefreshRecord) {
//                            shouldRefreshRecord = true;
//                        mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_VIDEO, false, true);
                        refreshVideoRecordUI(rsp.seconds, rsp.videoType);
//                            DataSourceManager.getInstance().pushDeviceState(uuid, rsp);
//                        }
                    }
                    AppLogger.w("初始化录像状态结果为:" + new Gson().toJson(rsp));
                    return rsp;
                })
                .observeOn(Schedulers.io())
                .flatMap(ret -> BasePanoramaApiHelper.getInstance().getResolution(uuid))
                .observeOn(AndroidSchedulers.mainThread())
                .map(ret -> {
                    if (ret != null && ret.ret == 0) {
                        mView.onSwitchSpeedMode(ret.resolution);//检查分辨率
                    }
                    return ret;
                })
                .observeOn(Schedulers.io())
                .flatMap(ret -> BasePanoramaApiHelper.getInstance().getPowerLine(uuid))
                .observeOn(AndroidSchedulers.mainThread())
                .map(ret -> {
                    if (ret != null && ret.powerline == 1) {
                        charge = true;
                        mView.onDeviceBatteryChanged(-1);
                    } else {
                        charge = false;
                    }
                    return ret;
                })
                .observeOn(Schedulers.io())
                .flatMap(ret -> BasePanoramaApiHelper.getInstance().getBattery(uuid).observeOn(AndroidSchedulers.mainThread())
                        .map(bat -> {
                            if (bat != null) {
                                this.battery = bat.battery;
                                Device device = sourceManager.getDevice(uuid);
                                DPEntity property = device.getProperty(206);
                                if (property == null) {
                                    property = device.getEmptyProperty(206);
                                }
                                property.setValue(new DpMsgDefine.DPPrimary<>(this.battery), pack(this.battery), property.getVersion());
                                if (bat.battery <= 20 && isFirst) {//检查电量
                                    isFirst = false;
                                    DBOption.DeviceOption option = device.option(DBOption.DeviceOption.class);
                                    if (option != null && option.lastLowBatteryTime < TimeUtils.getTodayStartTime()) {//新的一天
                                        option.lastLowBatteryTime = System.currentTimeMillis();
                                        device.setOption(option);
                                        sourceManager.updateDevice(device);
                                        mView.onBellBatteryDrainOut();
                                    }
                                }
                                if (ret != null && ret.powerline != 1) {
                                    mView.onDeviceBatteryChanged(this.battery);
                                }

                            }
                            return bat;
                        })

                )
                .onErrorResumeNext(throwable -> {
                    AppLogger.e(throwable.getMessage());
                    return Observable.just(null);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    mView.onDeviceInitFinish();//初始化成功,可以播放视频了
                }, e -> {
                    AppLogger.e(MiscUtils.getErr(e));
                });

        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, subscribe);
    }

    @Override
    public void switchVideoResolution(@PanoramaCameraContact.View.SPEED_MODE int mode) {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().setResolution(uuid, mode)
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    AppLogger.w("切换模式返回结果为" + new Gson().toJson(ret));
                    if (ret.ret == 0) {
                        mView.onSwitchSpeedMode(mode);
                    } else {
                        AppLogger.w("切换模式失败了");
                    }
                }, e -> {
                    AppLogger.e(MiscUtils.getErr(e));
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, subscribe);
    }

    @Override
    public void startVideoRecord(int type) {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().startRec(uuid, type)
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> mView.onRefreshControllerView(false, false))
                .subscribe(rsp -> {
                    AppLogger.w("开启视频录制返回结果为" + new Gson().toJson(rsp));
                    if (rsp.ret == 0) {
                        AppLogger.w("开启视频录制成功了");
//                        if (!shouldRefreshRecord) {
//                            shouldRefreshRecord = true;

                        refreshVideoRecordUI(0, type);
//                            DataSourceManager.getInstance().pushDeviceState(uuid, msgVideoStatusRsp);
//                        }
                    } else {
//                        shouldRefreshRecord = false;
                        if (rsp.ret == 2004) {
                            hasSDCard = false;
                        }
                        mView.onReportDeviceError(rsp.ret, false);
                    }
                }, e -> {
                    AppLogger.e(MiscUtils.getErr(e));
//                    shouldRefreshRecord = false;
                    mView.onReportDeviceError(-1, false);
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, subscribe);
    }

    @Override
    public void stopVideoRecord(int type) {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().stopRec(uuid, type)
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> RxBus.getCacheInstance().post(PanoramaCameraContact.View.RecordFinishEvent.INSTANCE))
                .doOnTerminate(() -> mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_VIDEO, true, false))
                .subscribe(ret -> {
                    if (ret.ret == 0 && ret.files != null && ret.files.size() > 0) {//成功了
                        mView.onShowPreviewPicture(null);
                        if (BasePanoramaApiHelper.getInstance().getDeviceIp() == null) {
                            mView.onReportDeviceError(ERROR_CODE_HTTP_NOT_AVAILABLE, true);
                        }
                    } else {//失败了
                        if (ret.ret == 2004) {
                            hasSDCard = false;
                        }
                        mView.onReportDeviceError(ret.ret, false);
                    }
                    AppLogger.d("停止直播返回结果为:" + new Gson().toJson(ret));
                }, e -> {
                    mView.onReportDeviceError(-1, false);
                    AppLogger.e(MiscUtils.getErr(e));
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, subscribe);
    }

    @Override
    public void formatSDCard() {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().sdFormat(uuid)
                .timeout(120, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    if (ret != null && ret.sdIsExist && ret.sdcard_recogntion == 0) {
                        mView.onSDFormatResult(1);
                    } else {
                        mView.onSDFormatResult(-1);
                    }
                }, e -> {
                    AppLogger.e(MiscUtils.getErr(e));
                    mView.onSDFormatResult(-1);
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, subscribe);
    }

    public void refreshVideoRecordUI(int offset, @PanoramaCameraContact.View.PANORAMA_RECORD_MODE int type) {
        Subscription subscribe = Observable.interval(0, 500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(count -> (int) (count / 2) + offset)
                .takeUntil(RxBus.getCacheInstance().toObservable(PanoramaCameraContact.View.RecordFinishEvent.class))
                .skipLast(1)
                .doOnSubscribe(() -> RxBus.getCacheInstance().post(PanoramaCameraContact.View.RecordFinishEvent.INSTANCE))
                .doOnTerminate(() -> isRecording = false)
                .subscribe(second -> {
                    isRecording = true;
                    mView.onRefreshVideoRecordUI(second, type);
                }, AppLogger::e);
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, subscribe);
    }

    private Subscription makeNewMsgSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .subscribeOn(Schedulers.io())
                .flatMap(ret -> Observable.from(ret.dpList))
                .filter(ret -> filterNewMsgId(ret.id))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    mView.onShowNewMsgHint();
                }, AppLogger::e);
    }

    private boolean filterNewMsgId(long id) {
        return id == 505 || id == 222 || id == 512;
    }

}
