package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.SettingTip;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamSettingPresenterImpl extends AbstractPresenter<CamSettingContract.View> implements
        CamSettingContract.Presenter {
    private Device device;

    private static final int[] autoRecordMode = {
            R.string.RECORD_MODE,
            R.string.RECORD_MODE_1,
            R.string.RECORD_MODE_2
    };

    public CamSettingPresenterImpl(CamSettingContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
        device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                robotDataSync(),
                robotDeviceDataSync()
        };
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{ConnectivityManager.CONNECTIVITY_ACTION};
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityStatus status = ReactiveNetwork.getConnectivityStatus(context);
            Observable.just(status)
                    .throttleFirst(500, TimeUnit.MILLISECONDS)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(connectivityStatus ->
                                    getView().onNetworkChanged(connectivityStatus != null && connectivityStatus.state >= 0),
                            throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
        }
    }

    @Override
    public void start() {
        super.start();
        getView().deviceUpdate(device);
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private Subscription robotDataSync() {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .filter((RobotoGetDataRsp jfgRobotSyncData) -> (
                        getView() != null && TextUtils.equals(uuid, jfgRobotSyncData.identity)
                ))
                .observeOn(AndroidSchedulers.mainThread())
                .map((RobotoGetDataRsp update) -> {
                    getView().deviceUpdate(BaseApplication.getAppComponent().getSourceManager().getDevice(uuid));
                    return null;
                })
                .subscribe(ret -> {
                }, throwable -> AppLogger.e("err: " + MiscUtils.getErr(throwable)));
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private Subscription robotDeviceDataSync() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter(jfgRobotSyncData -> (
                        ListUtils.getSize(jfgRobotSyncData.dpList) > 0 &&
                                getView() != null && TextUtils.equals(uuid, jfgRobotSyncData.uuid)
                ))
                .flatMap(ret -> Observable.from(ret.dpList))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .subscribe(msg -> {
                    try {
                        mView.deviceUpdate(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, throwable -> AppLogger.e("err: " + MiscUtils.getErr(throwable)));
    }

    @Override
    public void updateSettingTips(SettingTip settingTip) {
        PreferencesUtils.putString(JConstant.KEY_DEVICE_SETTING_SHOW_RED + uuid, new Gson().toJson(settingTip));
    }

    @Override
    public SettingTip getSettingTips() {
        try {
            String content = PreferencesUtils.getString(JConstant.KEY_DEVICE_SETTING_SHOW_RED + uuid);
            if (TextUtils.isEmpty(content)) return new SettingTip();
            return new Gson().fromJson(content, SettingTip.class);
        } catch (Exception e) {
            return new SettingTip();
        }
    }

    @Override
    public String getDetailsSubTitle(Context context, boolean hasSdcard, int err) {
        //sd卡状态
        if (hasSdcard && err != 0) {
            //sd初始化失败时候显示
            return context.getString(R.string.SD_INIT_ERR, err);
        }
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        return device != null && TextUtils.isEmpty(device.alias) ?
                device.uuid : (device != null ? device.alias : "");
    }

    //    一：关闭移动侦测
//1.关闭移动侦测后，下方显示：关闭
//
//    二：打开移动侦测
//1.当设备报警开启周一至周日，时间段为0:00-23:59 下方应显示：每天 全天提示
//3.当设备报警开启周一至周五，时间段为0:00-00:00 下方应显示：工作日 00:00-次日0:00
//            4.当设备报警天数开启周六、周日，时间段为01:00-16:00 下方应显示：周末 01:00-16:00
//            5.当设备报警天数开启周六、周日，时间段为0:00-23:59 下方应显示：工作日 全天提示
//6.当设备报警天数开启周一、周五、周六，时间段为02:00-19:00 下方应显示：周一、周五、周六 02:00-19:00
//            7.当设备报警天数开启周一、周五、周六，时间段为0:00-23:59 下方应显示：周一、周五、周六 全天提示
//
//    天数总结：
//            1.周一至周日显示：每天
//2.周一至周五显示 ：工作日
//3.周六日显示：周末
//4.周一、周六显示：每周一、周六
//
//    时间总结：
//            1.0:00-23：59 显示：全天提示
//2.0：00-0:23:28 显示：0：00-0:23:28
//            3.0:00-0:00显示0:00-次日0:00
//            4:12:01-12:00显示：12:01-次日12:00
    @Override
    public String getAlarmSubTitle(Context context) {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        boolean f = device.$(DpMsgMap.ID_501_CAMERA_ALARM_FLAG, false);
        DpMsgDefine.DPAlarmInfo info = device.$(DpMsgMap.ID_502_CAMERA_ALARM_INFO, new DpMsgDefine.DPAlarmInfo());
        return MiscUtils.getChaosTime(context, info, f);
    }


    @Override
    public String getAutoRecordTitle(Context context) {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        int deviceAutoVideoRecord = device.$(DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD, -1);
        DpMsgDefine.DPSdStatus sdStatus = device.$(DpMsgMap.ID_204_SDCARD_STORAGE, new DpMsgDefine.DPSdStatus());
        if (sdStatus == null || !sdStatus.hasSdcard || sdStatus.err != 0)
            return "";
        if (deviceAutoVideoRecord > 2 || deviceAutoVideoRecord < 0) {
            deviceAutoVideoRecord = 0;
        }
        int auto = device.$(ID_303_DEVICE_AUTO_VIDEO_RECORD, -1);
        if (auto < 0)
            return "";
        return context.getString(autoRecordMode[deviceAutoVideoRecord]);
    }

    @Override
    public <T extends DataPoint> void updateInfoReq(T value, long id) {
        Subscription subscription = Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    AppLogger.i("save initSubscription: " + id + " " + value);
                    try {
                        BaseApplication.getAppComponent().getSourceManager().updateValue(uuid, value, (int) id);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err:" + e.getLocalizedMessage());
                    }
                    AppLogger.i("save end: " + id + " " + value);
                }, (Throwable throwable) -> AppLogger.e(throwable.getLocalizedMessage()));
        addSubscription(subscription, "updateInfoReq" + id);
    }

    @Override
    public <T extends DataPoint> void updateInfoReq(List<T> value) {
        Subscription subscription = Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    AppLogger.i("save initSubscription: " + " " + value);
                    try {
                        BaseApplication.getAppComponent().getSourceManager().updateValue(uuid, value);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err:" + e.getLocalizedMessage());
                    }
                    AppLogger.i("save end:" + value);
                }, (Throwable throwable) -> AppLogger.e(throwable.getLocalizedMessage()));
        addSubscription(subscription, "updateInfoReq_ex");
    }

    @Override
    public void unbindDevice() {
        Subscription subscribe = Observable.just(new DPEntity()
                .setUuid(uuid)
                .setAction(DBAction.UNBIND))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(item -> BaseApplication.getAppComponent().getTaskDispatcher().perform(item))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> mView.unbindDeviceRsp(rsp.getResultCode()), e -> {
                    if (e instanceof TimeoutException) {
                        mView.unbindDeviceRsp(-1);
                    }
                    AppLogger.e("err: " + MiscUtils.getErr(e));
                }, () -> {
                });
        addSubscription(subscribe, "unbindDevice");
    }
}
