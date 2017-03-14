package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JResultEvent;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class DeviceInfoDetailPresenterImpl extends AbstractPresenter<CamInfoContract.View>
        implements CamInfoContract.Presenter {

    //    private BeanCamInfo beanCamInfo;
    private long requst;

    public DeviceInfoDetailPresenterImpl(CamInfoContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                checkNewSoftVersionBack(),
                robotDeviceDataSync(),
                clearSdcardReqBack(),
                onClearSdReqBack()
        };
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
                    getView().deviceUpdate(DataSourceManager.getInstance().getJFGDevice(uuid));
                    return null;
                })
                .retry(new RxHelper.RxException<>("robotDeviceDataSync"))
                .subscribe();
    }

    @Override
    public <T extends DataPoint> void updateInfoReq(T value, long id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        com.cylan.jiafeigou.base.module.DataSourceManager.getInstance().updateValue(uuid, value, (int) id);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }

    @Override
    public void checkNewSoftVersion() {
        // TODO 检测是否有新的固件
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
                        DpMsgDefine.DPPrimary<String> sVersion = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_207_DEVICE_VERSION);
                        try {
                            JfgCmdInsurance.getCmd().checkDevVersion(device.pid, uuid, MiscUtils.safeGet(sVersion, ""));
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public Subscription checkNewSoftVersionBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckDevVersionRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.CheckDevVersionRsp checkDevVersionRsp) -> {
                    if (checkDevVersionRsp != null)
                        getView().checkDevResult(checkDevVersionRsp);
                });
    }

    @Override
    public void clearSdcard() {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe((Object o) -> {
                    try {
                        ArrayList<JFGDPMsg> ipList = new ArrayList<JFGDPMsg>();
                        JFGDPMsg mesg = new JFGDPMsg(DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD, 0);
                        mesg.packValue = DpUtils.pack(0);
                        ipList.add(mesg);
                        requst = JfgCmdInsurance.getCmd().robotSetData(uuid, ipList);
                    } catch (Exception e) {
                        AppLogger.e("format sd： " + e.getLocalizedMessage());
                    }
                });
    }

    @Override
    public Subscription clearSdcardReqBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SdcardClearFinishRsp.class)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<RxEvent.SdcardClearFinishRsp, Observable<DpMsgDefine.DPSdStatus>>() {
                    @Override
                    public Observable<DpMsgDefine.DPSdStatus> call(RxEvent.SdcardClearFinishRsp rsp) {
                        if (rsp != null && rsp.arrayList.size() >0){
                            for (JFGDPMsg dp : rsp.arrayList) {
                                try {
                                    if (dp.id == 203 && TextUtils.equals(uuid,rsp.s)) {
                                        DpMsgDefine.DPSdStatus sdStatus = DpUtils.unpackData(dp.packValue, DpMsgDefine.DPSdStatus.class);
                                        return Observable.just(sdStatus);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return Observable.just(null);
                                }
                            }
                        }
                        return Observable.just(null);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    if (o != null){
                        //清空SD卡提示
                        getView().clearSdResult(0);
                    }
                });
    }

    @Override
    public Subscription onClearSdReqBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SdcardClearReqRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.SdcardClearReqRsp sdcardClearRsp) -> {
                    if (sdcardClearRsp != null) {
                        JFGDPMsgRet jfgdpMsgRet = sdcardClearRsp.arrayList.get(0);
                        if (jfgdpMsgRet.id == 218 && jfgdpMsgRet.ret == 0) {
                            //
                        } else {
                            getView().clearSdResult(1);
                        }
                    }
                });
    }

    public void updateAlias(Device device) {
        addSubscription(Observable.just(device)
                .map(device1 -> {
                    DataSourceManager.getInstance().updateJFGDevice(device);
                    try {
                        JfgCmdInsurance.getCmd().setAliasByCid(device.uuid, device.alias);
                        AppLogger.d("update alias suc");
                    } catch (JfgException e) {
                        AppLogger.e("err: set up remote alias failed: " + new Gson().toJson(device));
                    }
                    return null;
                })
                .timeout(1, TimeUnit.SECONDS, Observable.just("setAliasTimeout")
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .map(s -> {
                            getView().setAliasRsp(-1);
                            AppLogger.e("timeout: " + s);
                            return null;
                        }))
                .flatMap(dev -> RxBus.getCacheInstance().toObservable(RxEvent.SetAlias.class)
                        .filter(setAlias -> setAlias.result.event == JResultEvent.JFG_RESULT_SET_DEVICE_ALIAS
                                && setAlias.result.code == 0))
                .filter(s -> getView() != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(setAlias -> getView().setAliasRsp(JError.ErrorOK),
                        throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage())));
    }
}
