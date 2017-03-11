package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.MiscUtils;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamSettingPresenterImpl extends AbstractPresenter<CamSettingContract.View> implements
        CamSettingContract.Presenter {

    private JFGDevice device;
    private static final int[] periodResId = {R.string.MON_1, R.string.TUE_1,
            R.string.WED_1, R.string.THU_1,
            R.string.FRI_1, R.string.SAT_1, R.string.SUN_1};
    private static final int[] autoRecordMode = {
            R.string.RECORD_MODE,
            R.string.RECORD_MODE_1,
            R.string.RECORD_MODE_2
    };

    public CamSettingPresenterImpl(CamSettingContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
        device = DataSourceManager.getInstance().getRawJFGDevice(uuid);
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
        return RxBus.getCacheInstance().toObservable(RxEvent.ParseResponseCompleted.class)
                .filter((RxEvent.ParseResponseCompleted jfgRobotSyncData) -> (
                        getView() != null && TextUtils.equals(uuid, jfgRobotSyncData.uuid)
                ))
                .observeOn(AndroidSchedulers.mainThread())
                .map((RxEvent.ParseResponseCompleted update) -> {
                    getView().deviceUpdate(DataSourceManager.getInstance().getRawJFGDevice(uuid));
                    return null;
                })
                .retry(new RxHelper.RxException<>("robotDataSync"))
                .subscribe();
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private Subscription robotDeviceDataSync() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter((RxEvent.DeviceSyncRsp jfgRobotSyncData) -> (
                        getView() != null && TextUtils.equals(uuid, jfgRobotSyncData.uuid)
                ))
                .observeOn(AndroidSchedulers.mainThread())
                .map((RxEvent.DeviceSyncRsp update) -> {
                    getView().deviceUpdate(DataSourceManager.getInstance().getRawJFGDevice(uuid));
                    return null;
                })
                .retry(new RxHelper.RxException<>("robotDeviceDataSync"))
                .subscribe();
    }

    @Override
    public String getDetailsSubTitle(Context context) {
        //sd卡状态
        DpMsgDefine.DPSdStatus status = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_204_SDCARD_STORAGE);
        if (status != null) {
            if (status.hasSdcard && status.err != 0) {
                //sd初始化失败时候显示
                return context.getString(R.string.SD_INIT_ERR, status.err);
            }
        }
        Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
        return device != null && TextUtils.isEmpty(device.alias) ?
                device.uuid : (device != null ? device.alias : "");
    }

    @Override
    public String getAlarmSubTitle(Context context) {
        DpMsgDefine.DPPrimary<Boolean> flag = DataSourceManager.getInstance().getValue(uuid, (long) DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
        boolean f = MiscUtils.safeGet(flag, false);
        if (!f) {
            return getView().getContext().getString(R.string.MAGNETISM_OFF);
        }
        DpMsgDefine.DPAlarmInfo info = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_502_CAMERA_ALARM_INFO);
        int day = info == null ? 0 : info.day;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (((day >> (7 - 1 - i)) & 0x01) == 1) {
                //hit
                builder.append(context.getString(periodResId[i]));
                builder.append(",");
            }
        }
        if (builder.length() > 1)
            builder.replace(builder.length() - 1, builder.length(), "");
        if (day == 127) {//全天
            builder.setLength(0);
            builder.append(context.getString(R.string.HOURS));
        } else if (day == 124) {//工作日
            builder.setLength(0);
            builder.append(context.getString(R.string.WEEKDAYS));
        }
        builder.append(info == null ? "" : parse2Time(info.timeStart));
        builder.append("-");
        builder.append(info == null ? "" : parse2Time(info.timeEnd));
        return builder.toString();
    }

    @Override
    public String getAutoRecordTitle(Context context) {
        int deviceAutoVideoRecord = MiscUtils.safeGet(DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD), 0);
        if (deviceAutoVideoRecord > 2 || deviceAutoVideoRecord < 0) {
            deviceAutoVideoRecord = 0;
        }
        DpMsgDefine.DPSdStatus sdStatus = MiscUtils.safeGet_(DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_204_SDCARD_STORAGE), DpMsgDefine.DPSdStatus.empty);
        if (sdStatus == null || !sdStatus.hasSdcard || sdStatus.err != 0)
            return "";
        return context.getString(autoRecordMode[deviceAutoVideoRecord]);
    }

    @Override
    public <T extends DataPoint> void updateInfoReq(T value, long id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    AppLogger.i("save start: " + id + " " + value);
                    try {
                        DataSourceManager.getInstance().updateValue(uuid, value, (int) id);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err:" + e.getLocalizedMessage());
                    }
                    AppLogger.i("save end: " + id + " " + value);
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }

    public static String parse2Time(int value) {
        return String.format(Locale.getDefault(), "%02d", value >> 8)
                + String.format(Locale.getDefault(), ":%02d", (((byte) value << 8) >> 8));
    }

    @Override
    public void unbindDevice() {
        addSubscription(Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .map((Object o) -> {
                    boolean result = DataSourceManager.getInstance().delRemoteJFGDevice(uuid);
                    AppLogger.i("unbind remote action uuid: " + uuid + " " + result);
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .zipWith(RxBus.getCacheInstance().toObservable(RxEvent.UnBindDeviceEvent.class)
                                .subscribeOn(Schedulers.newThread())
                                .timeout(3000, TimeUnit.MILLISECONDS, Observable.just("unbind timeout")
                                        .subscribeOn(AndroidSchedulers.mainThread())
                                        .map(s -> {
                                            getView().unbindDeviceRsp(-1);
                                            return null;
                                        }))
                                .filter(event -> getView() != null && event != null)
                                .observeOn(AndroidSchedulers.mainThread())
                                .filter(unbindEvent -> {
                                    if (unbindEvent.jfgResult.code != 0)
                                        getView().unbindDeviceRsp(unbindEvent.jfgResult.code);//失败
                                    return unbindEvent.jfgResult.code == 0;
                                })
                                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage())),
                        (Object o, RxEvent.UnBindDeviceEvent unbindEvent) -> {
                            getView().unbindDeviceRsp(0);//成功
                            DataSourceManager.getInstance().delLocalJFGDevice(uuid);
                            return null;
                        })
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .subscribe());
    }
}
