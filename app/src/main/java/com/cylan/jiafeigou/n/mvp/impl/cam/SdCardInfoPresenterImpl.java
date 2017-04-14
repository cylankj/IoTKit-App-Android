package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.SdCardInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public class SdCardInfoPresenterImpl extends AbstractPresenter<SdCardInfoContract.View> implements SdCardInfoContract.Presenter {

    private boolean isClearSucc;
    private boolean isClearFin;
    private long req;

    public SdCardInfoPresenterImpl(SdCardInfoContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                onClearSdReqBack(),
                onClearSdResult(),
                getSdCapacityBack(),
        };
    }

    @Override
    public void start() {
        super.start();
        getSdCapacity(uuid);
    }

    /**
     * 是否有SD卡
     *
     * @return
     */
    @Override
    public boolean getSdcardState() {
        DpMsgDefine.DPSdStatus sdStatus = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid).$(204, new DpMsgDefine.DPSdStatus());
        //sd卡状态
        if (sdStatus != null) {
            if (!sdStatus.hasSdcard && sdStatus.err != 0) {
                //sd初始化失败
                return false;
            }
        }
        if (sdStatus != null && !sdStatus.hasSdcard) {
            return false;
        }
        return true;
    }

    @Override
    public void updateInfoReq() {
        Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        ArrayList<JFGDPMsg> ipList = new ArrayList<JFGDPMsg>();
                        JFGDPMsg mesg = new JFGDPMsg(DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD, 0);
                        mesg.packValue = DpUtils.pack(0);
                        ipList.add(mesg);
                        BaseApplication.getAppComponent().getCmd().robotSetData(uuid, ipList);
                        AppLogger.d("clear_excute:");
                    } catch (Exception e) {
                        AppLogger.e("err_sd: " + e.getLocalizedMessage());
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e("updateInfoReq_sd" + throwable.getLocalizedMessage());
                });
    }

    @Override
    public void clearCountTime() {
        addSubscription(Observable.just(null)
                .delay(2, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Object o) -> {
                    if (getView() != null && !isClearSucc) getView().clearSdResult(2);
                }, AppLogger::e));
    }

    @Override
    public Subscription onClearSdReqBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SdcardClearReqRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.SdcardClearReqRsp sdcardClearRsp) -> {
                    if (sdcardClearRsp != null) {
                        JFGDPMsgRet jfgdpMsgRet = sdcardClearRsp.arrayList.get(0);
                        if (jfgdpMsgRet.id == 218) {
                            if (jfgdpMsgRet.ret == 0) {
                                isClearSucc = true;
                                isClearFin = true;
                            } else {
                                getView().clearSdResult(1);
                            }
                        }
                    }
                }, AppLogger::e);
    }

    @Override
    public Subscription onClearSdResult() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<RxEvent.DeviceSyncRsp, Observable<Object>>() {
                    @Override
                    public Observable<Object> call(RxEvent.DeviceSyncRsp rsp) {
                        if (rsp != null && rsp.dpList.size() > 0) {
                            for (JFGDPMsg dp : rsp.dpList) {
                                try {
                                    if (dp.id == 203 && TextUtils.equals(uuid, rsp.uuid)) {
                                        DpMsgDefine.DPSdStatus sdStatus = DpUtils.unpackData(dp.packValue, DpMsgDefine.DPSdStatus.class);
                                        return Observable.just(sdStatus);
                                    } else if (dp.id == 222 && TextUtils.equals(uuid, rsp.uuid)) {
                                        DpMsgDefine.DPSdcardSummary isPopSd = DpUtils.unpackData(dp.packValue, DpMsgDefine.DPSdcardSummary.class);
                                        return Observable.just(isPopSd);
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
                    if (o != null) {
                        if (o instanceof DpMsgDefine.DPSdStatus) {
                            //清空SD卡提示
                            if (isClearFin) {
                                getView().clearSdResult(0);
                                DpMsgDefine.DPSdStatus sdStatus = (DpMsgDefine.DPSdStatus) o;
                                sdStatus.used = 0L;
                                getView().initSdUseDetail(sdStatus);
                                isClearFin = false;
                            }

                        } else if (o instanceof DpMsgDefine.DPSdcardSummary) {
                            //SD卡已被拔出提示
                            DpMsgDefine.DPSdcardSummary sdcardSummary = (DpMsgDefine.DPSdcardSummary) o;
                            if (!sdcardSummary.hasSdcard)
                                getView().showSdPopDialog();
                        }
                    }
                }, e -> AppLogger.d(e.getMessage()));
    }

    /**
     * 获取到sd卡的容量
     *
     * @param uuid
     */
    @Override
    public void getSdCapacity(String uuid) {
        Observable.just(uuid)
                .subscribeOn(Schedulers.newThread())
                .subscribe(s -> {
                    if (!TextUtils.isEmpty(s)) {
                        try {
                            ArrayList<JFGDPMsg> dpID = new ArrayList<JFGDPMsg>();
                            JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_204_SDCARD_STORAGE, System.currentTimeMillis());
                            dpID.add(msg);
                            req = BaseApplication.getAppComponent().getCmd().robotGetData(uuid, dpID, 1, false, 0);
                            AppLogger.d("getSdCapacity:" + req);
                        } catch (JfgException e) {
                            AppLogger.e("getSdCapacity_err:" + e.getLocalizedMessage());
                            e.printStackTrace();
                        }
                    }
                }, AppLogger::e);
    }

    @Override
    public Subscription getSdCapacityBack() {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<RobotoGetDataRsp, Observable<DpMsgDefine.DPSdStatus>>() {
                    @Override
                    public Observable<DpMsgDefine.DPSdStatus> call(RobotoGetDataRsp robotoGetDataRsp) {
                        AppLogger.d("sd_version:" + robotoGetDataRsp.seq + "identify:" + robotoGetDataRsp.identity);
                        if (robotoGetDataRsp.map.size() != 0) {
                            if (req != robotoGetDataRsp.seq || !uuid.equals(robotoGetDataRsp.identity)) {
                                return Observable.just(null);
                            }
                            for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : robotoGetDataRsp.map.entrySet()) {
                                try {
                                    if (entry.getKey() == 204) {
                                        ArrayList<JFGDPMsg> value = entry.getValue();
                                        JFGDPMsg jfgdpMsg = value.get(0);
                                        DpMsgDefine.DPSdStatus sysMesg = DpUtils.unpackData(jfgdpMsg.packValue, DpMsgDefine.DPSdStatus.class);
                                        return Observable.just(sysMesg);
                                    }
                                } catch (Exception e) {
                                    AppLogger.e("getSdCapacityBack:" + e.getLocalizedMessage());
                                    return Observable.just(null);
                                }
                            }
                        }
                        return Observable.just(null);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sdStatus -> {
                    if (sdStatus != null && getView() != null) getView().initSdUseDetail(sdStatus);
                }, AppLogger::e);
    }

}
