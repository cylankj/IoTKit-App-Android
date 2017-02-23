package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.content.Context;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.Locale;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamSettingPresenterImpl extends AbstractPresenter<CamSettingContract.View> implements
        CamSettingContract.Presenter {

//    private CompositeSubscription compositeSubscription;

    //    private BeanCamInfo camInfoBean;
    private String uuid;
    private static final int[] periodResId = {R.string.MON_1, R.string.TUE_1,
            R.string.WED_1, R.string.THU_1,
            R.string.FRI_1, R.string.SAT_1, R.string.SUN_1};
    private static final int[] autoRecordMode = {
            R.string.RECORD_MODE,
            R.string.RECORD_MODE_1,
            R.string.RECORD_MODE_2
    };

    public CamSettingPresenterImpl(CamSettingContract.View view, String uuid) {
        super(view);
        view.setPresenter(this);
        this.uuid = uuid;
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                robotDataSync(),
                unbindDevSub()
        };
    }

    @Override
    public void start() {
        super.start();
        JFGDevice device = GlobalDataProxy.getInstance().fetch(uuid);
        if (device != null && TextUtils.isEmpty(device.shareAccount)) {
            getView().onInfoUpdate(GlobalDataProxy.getInstance().fetchLocal(uuid, DpMsgMap.ID_201_NET));
            getView().onInfoUpdate(GlobalDataProxy.getInstance().fetchLocal(uuid, DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY));
            getView().onInfoUpdate(GlobalDataProxy.getInstance().fetchLocal(uuid, DpMsgMap.ID_209_LED_INDICATOR));
            getView().onInfoUpdate(GlobalDataProxy.getInstance().fetchLocal(uuid, DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE));
            getView().onInfoUpdate(GlobalDataProxy.getInstance().fetchLocal(uuid, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG));
        } else {
            //分享设备
            getView().isSharedDevice();
        }
    }

    /**
     * 解绑设备
     *
     * @return
     */
    private Subscription unbindDevSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.UnBindDeviceEvent.class)
                .subscribeOn(Schedulers.newThread())
                .filter((RxEvent.UnBindDeviceEvent unBindDeviceEvent) -> {
                    return getView() != null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map((RxEvent.UnBindDeviceEvent unBindDeviceEvent) -> {
                    getView().unbindDeviceRsp(unBindDeviceEvent.jfgResult.code);
                    if (unBindDeviceEvent.jfgResult.code == 0) {
                        //清理这个订阅
                        RxBus.getCacheInstance().removeStickyEvent(RxEvent.UnBindDeviceEvent.class);
                    }
                    return null;
                })
                .retry(new RxHelper.RxException<>("unbindDevSub"))
                .subscribe();
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private Subscription robotDataSync() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DataPoolUpdate.class)
                .filter((RxEvent.DataPoolUpdate jfgRobotSyncData) -> (
                        getView() != null && TextUtils.equals(uuid, jfgRobotSyncData.uuid)
                ))
                .observeOn(AndroidSchedulers.mainThread())
                .map((RxEvent.DataPoolUpdate update) -> {
                    getView().onInfoUpdate(update.value);
                    return null;
                })
                .retry(new RxHelper.RxException<>("robotDataSync"))
                .subscribe();
    }

    @Override
    public String getDetailsSubTitle(Context context) {
        //sd卡状态
        DpMsgDefine.DPSdStatus status = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_204_SDCARD_STORAGE, null);
        if (status != null) {
            if (status.hasSdcard && status.err != 0) {
                //sd初始化失败时候显示
                return context.getString(R.string.SD_INIT_ERR, status.err);
            }
        }
        JFGDevice device = GlobalDataProxy.getInstance().fetch(uuid);
        return device != null && TextUtils.isEmpty(device.alias) ?
                device.uuid : (device != null ? device.alias : "");
    }

    @Override
    public String getAlarmSubTitle(Context context) {
        boolean flag = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_501_CAMERA_ALARM_FLAG, false);
        if (!flag) {
            return getView().getContext().getString(R.string.MAGNETISM_OFF);
        }
        DpMsgDefine.DPAlarmInfo info = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_502_CAMERA_ALARM_INFO, null);
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
        int deviceAutoVideoRecord = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD, 0);
        if (deviceAutoVideoRecord > 2 || deviceAutoVideoRecord < 0) {
            deviceAutoVideoRecord = 0;
        }
        return context.getString(autoRecordMode[deviceAutoVideoRecord]);
    }

    public static String parse2Time(int value) {
        return String.format(Locale.getDefault(), "%02d", value >> 8)
                + String.format(Locale.getDefault(), ":%02d", (((byte) value << 8) >> 8));
    }

//    @Override
//    public BeanCamInfo getCamInfoBean() {
//        if (camInfoBean == null)
//            camInfoBean = new BeanCamInfo();
//        return camInfoBean;
//    }

    @Override
    public void updateInfoReq(Object value, long id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    AppLogger.i("save start: " + id + " " + value);
                    BaseValue baseValue = new BaseValue();
                    baseValue.setId(id);
                    baseValue.setVersion(System.currentTimeMillis());
                    baseValue.setValue(o);
                    GlobalDataProxy.getInstance().update(uuid, baseValue, true);
                    AppLogger.i("save end: " + id + " " + value);
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }

    @Override
    public void unbindDevice() {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe((Object o) -> {
                    boolean result = GlobalDataProxy.getInstance().remove(uuid);
                    try {
                        JfgCmdInsurance.getCmd().unBindDevice(uuid);
                        GlobalDataProxy.getInstance().deleteJFGDevice(uuid);
                    } catch (JfgException e) {
                        AppLogger.e("" + e.getLocalizedMessage());
                    }
                    AppLogger.i("unbind uuid: " + uuid + " " + result);
                }, (Throwable throwable) -> {
                    AppLogger.e("delete uuid failed: " + throwable.getLocalizedMessage());
                });
    }
}
