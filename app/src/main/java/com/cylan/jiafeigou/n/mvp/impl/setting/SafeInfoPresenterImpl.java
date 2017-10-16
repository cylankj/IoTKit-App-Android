package com.cylan.jiafeigou.n.mvp.impl.setting;

import android.content.Context;
import android.text.TextUtils;

import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.setting.SafeInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;

import java.io.IOException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class SafeInfoPresenterImpl extends AbstractPresenter<SafeInfoContract.View>
        implements SafeInfoContract.Presenter {
    private String uuid;
    private static final int[] periodResId = {R.string.MON_1, R.string.TUE_1,
            R.string.WED_1, R.string.THU_1,
            R.string.FRI_1, R.string.SAT_1, R.string.SUN_1};

    public SafeInfoPresenterImpl(SafeInfoContract.View view, String uuid) {
        super(view);
        this.uuid = uuid;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        DataSourceManager.getInstance().syncAllProperty(uuid, 204, 222);
    }

    @Override
    public <T extends DataPoint> void updateInfoReq(T value, long id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        BaseApplication.getAppComponent().getSourceManager().updateValue(uuid, value, (int) id);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }

    @Override
    public String getRepeatMode(Context context) {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        boolean f = device.$(DpMsgMap.ID_501_CAMERA_ALARM_FLAG, false);
        if (!f) {
            return getView().getContext().getString(R.string.MAGNETISM_OFF);
        }
        DpMsgDefine.DPAlarmInfo info = device.$(DpMsgMap.ID_502_CAMERA_ALARM_INFO, new DpMsgDefine.DPAlarmInfo());
        int day = info == null ? 0 : info.day;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (((day >> (7 - 1 - i)) & 0x01) == 1) {
                //hit
                builder.append(context.getString(periodResId[i]));
                builder.append(",");
            }
        }
        if (builder.length() > 1) {
            builder.replace(builder.toString().length() - 1, builder.toString().length(), "");
        }
        if (day == 127) {//全天
            builder.setLength(0);
            builder.append(context.getString(R.string.HOURS));
        } else if (day == 124) {//工作日
            builder.setLength(0);
            builder.append(context.getString(R.string.WEEKDAYS));
        }
        return builder.toString();
    }

    @Override
    public void getAIStrategy() {

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
    protected Subscription[] register() {
        return new Subscription[]{
                robotDataSync(),
                robotDeviceDataSync()
        };
    }


}
