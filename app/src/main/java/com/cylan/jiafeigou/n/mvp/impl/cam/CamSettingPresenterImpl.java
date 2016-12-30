package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.content.Context;
import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.rx.RxUiEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import java.util.Locale;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamSettingPresenterImpl extends AbstractPresenter<CamSettingContract.View> implements
        CamSettingContract.Presenter {

//    private CompositeSubscription compositeSubscription;

    private BeanCamInfo camInfoBean;
    private String uuid;
    private static final int[] periodResId = {R.string.MON_1, R.string.TUE_1,
            R.string.WED_1, R.string.THU_1,
            R.string.FRI_1, R.string.SAT_1, R.string.SUN_1};
    private static final int[] autoRecordMode = {
            R.string.RECORD_MODE,
            R.string.RECORD_MODE_1,
            R.string.RECORD_MODE_2
    };

    public CamSettingPresenterImpl(CamSettingContract.View view, DeviceBean bean) {
        super(view);
        view.setPresenter(this);
        fillData(bean);
    }

    private void fillData(DeviceBean bean) {
        this.uuid = bean.uuid;
        camInfoBean = new BeanCamInfo();
        BaseBean baseBean = new BaseBean();
        baseBean.alias = bean.alias;
        baseBean.pid = bean.pid;
        baseBean.uuid = bean.uuid;
        baseBean.sn = bean.sn;
        camInfoBean.convert(baseBean, bean.dataList);
        getView().onCamInfoRsp(camInfoBean);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                fetchCamInfo(),
                unbindDevSub()
        };
    }

//    @Override
//    public void start() {
//        unSubscribe(compositeSubscription);
//        compositeSubscription = new CompositeSubscription();
//        compositeSubscription.add(fetchCamInfo());
//        compositeSubscription.add(unbindDevSub());
//    }
//
//    @Override
//    public void stop() {
//        unSubscribe(compositeSubscription);
//    }

    /**
     * 解绑设备
     *
     * @return
     */
    private Subscription unbindDevSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.UnBindDeviceEvent.class)
                .subscribeOn(Schedulers.newThread())
                .filter(new Func1<RxEvent.UnBindDeviceEvent, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.UnBindDeviceEvent unBindDeviceEvent) {
                        return getView() != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<RxEvent.UnBindDeviceEvent, Object>() {
                    @Override
                    public Object call(RxEvent.UnBindDeviceEvent unBindDeviceEvent) {
                        getView().unbindDeviceRsp(unBindDeviceEvent.jfgResult.code);
                        if (unBindDeviceEvent.jfgResult.code == 0) {
                            //清理这个订阅
                            RxBus.getCacheInstance().removeStickyEvent(RxEvent.UnBindDeviceEvent.class);
                        }
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("unbindDevSub"))
                .subscribe();
    }

    private Subscription fetchCamInfo() {
        //查询设备列表
        return RxBus.getCacheInstance().toObservableSticky(RxUiEvent.BulkDeviceListRsp.class)
                .subscribeOn(Schedulers.computation())
                .filter(new Func1<RxUiEvent.BulkDeviceListRsp, Boolean>() {
                    @Override
                    public Boolean call(RxUiEvent.BulkDeviceListRsp list) {
                        return getView() != null && list.allDevices != null && camInfoBean != null;
                    }
                })
                .flatMap(new Func1<RxUiEvent.BulkDeviceListRsp, Observable<DpMsgDefine.DpWrap>>() {
                    @Override
                    public Observable<DpMsgDefine.DpWrap> call(RxUiEvent.BulkDeviceListRsp list) {
                        for (DpMsgDefine.DpWrap wrap : list.allDevices) {
                            if (wrap.baseDpDevice != null
                                    && TextUtils.equals(wrap.baseDpDevice.uuid, camInfoBean.deviceBase.uuid)) {
                                return Observable.just(wrap);
                            }
                        }
                        return null;
                    }
                })
                .filter(new Func1<DpMsgDefine.DpWrap, Boolean>() {
                    @Override
                    public Boolean call(DpMsgDefine.DpWrap dpWrap) {
                        return dpWrap != null && dpWrap.baseDpDevice != null;
                    }
                })
                .flatMap(new Func1<DpMsgDefine.DpWrap, Observable<BeanCamInfo>>() {
                    @Override
                    public Observable<BeanCamInfo> call(DpMsgDefine.DpWrap dpWrap) {
                        BeanCamInfo info = new BeanCamInfo();
                        info.convert(dpWrap.baseDpDevice, dpWrap.baseDpMsgList);
                        camInfoBean = info;
                        AppLogger.i("BeanCamInfo: " + new Gson().toJson(info));
                        return Observable.just(info);
                    }
                })
                .retry(new RxHelper.RxException<>("fetchCamInfo"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BeanCamInfo>() {
                    @Override
                    public void call(BeanCamInfo camInfoBean) {
                        //刷新
                        getView().onCamInfoRsp(camInfoBean);
                        if (camInfoBean.deviceBase != null
                                && !TextUtils.isEmpty(camInfoBean.deviceBase.shareAccount)) {
                            getView().isSharedDevice();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("err: " + throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public String getDetailsSubTitle(Context context) {
        //sd卡状态
        if (camInfoBean.sdcardStorage != null) {
            if (camInfoBean.sdcardStorage.hasSdcard && camInfoBean.sdcardStorage.err != 0) {
                //sd初始化失败时候显示
                return context.getString(R.string.NO_SDCARD);
            }
        }
        return TextUtils.isEmpty(camInfoBean.deviceBase.alias) ?
                camInfoBean.deviceBase.uuid : camInfoBean.deviceBase.alias;
    }

    @Override
    public String getAlarmSubTitle(Context context) {
        if (!camInfoBean.cameraAlarmFlag || camInfoBean.cameraAlarmInfo == null) {
            return getView().getContext().getString(R.string.MAGNETISM_OFF);
        }
        int day = camInfoBean.cameraAlarmInfo.day;
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
        builder.append(parse2Time(camInfoBean.cameraAlarmInfo.timeStart));
        builder.append("-");
        builder.append(parse2Time(camInfoBean.cameraAlarmInfo.timeEnd));
        return builder.toString();
    }

    @Override
    public String getAutoRecordTitle(Context context) {
        if (camInfoBean.deviceAutoVideoRecord > 2 || camInfoBean.deviceAutoVideoRecord < 0) {
            camInfoBean.deviceAutoVideoRecord = 0;
        }
        return context.getString(autoRecordMode[camInfoBean.deviceAutoVideoRecord]);
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
                    String uuid = camInfoBean.deviceBase.uuid;
                    RxEvent.UnbindJFGDevice deletion = new RxEvent.UnbindJFGDevice();
                    deletion.uuid = uuid;
                    RxBus.getCacheInstance().post(deletion);
                    try {
                        JfgCmdInsurance.getCmd().unBindDevice(uuid);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    AppLogger.i("unbind uuid: " + uuid);
                }, (Throwable throwable) -> {
                    AppLogger.e("delete uuid failed: " + throwable.getLocalizedMessage());
                });
    }
}
