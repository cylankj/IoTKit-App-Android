package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.SdCardInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public class SdCardInfoPresenterImpl extends AbstractPresenter<SdCardInfoContract.View> implements SdCardInfoContract.Presenter {

    private long clearTimeFlag;//格式化sd卡,开始时间.

    public SdCardInfoPresenterImpl(SdCardInfoContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
    }

    @Override
    protected Subscription[] register() {
        if (needRegisterTimeout()) {
            addSubscription(clearCountTime(), "clearCountTime");
        }
        return new Subscription[]{
                onClearSdReqBack(),
                onClearSdResult()};
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
        clearTimeFlag = System.currentTimeMillis();
        addSubscription(clearCountTime(), "clearCountTime");
        Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        ArrayList<JFGDPMsg> ipList = new ArrayList<JFGDPMsg>();
                        JFGDPMsg mesg = new JFGDPMsg(DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD, 0);
                        mesg.packValue = DpUtils.pack(0);
                        ipList.add(mesg);
                        BaseApplication.getAppComponent().getCmd().robotSetData(uuid, ipList);
                        AppLogger.d("clear_execute:");
                    } catch (Exception e) {
                        AppLogger.e("err_sd: " + e.getLocalizedMessage());
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e("updateInfoReq_sd" + throwable.getLocalizedMessage());
                });
    }

    public boolean needRegisterTimeout() {
        return System.currentTimeMillis() - clearTimeFlag < 2 * 60 * 1000;
    }

    public Subscription clearCountTime() {
        return Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .timeout(2 * 60 * 1000L - System.currentTimeMillis() + clearTimeFlag, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Object o) -> {
                }, throwable -> {
                    if (throwable instanceof TimeoutException) {
                        AppLogger.d("两分钟超时时间到了!!!");
                        if (getView() != null) getView().clearSdResult(2);
                    }
                });
    }

    @Override
    public Subscription onClearSdReqBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class)
                .subscribeOn(Schedulers.newThread())
                .filter(ret -> mView != null && TextUtils.equals(ret.uuid, uuid))
                .map(ret -> ret.rets)
                .flatMap(Observable::from)
                .filter(msg -> msg.id == 218)
                .map(msg -> {
                    if (msg.ret == 0) {
                        History.getHistory().clearHistoryFile(uuid);
                        AppLogger.d("清空历史录像");
                    }
                    return msg;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.ret == 0) {
                        getView().clearSdResult(0);
                    } else {
                        getView().clearSdResult(1);
                    }
                }, AppLogger::e);
    }

    @Override
    public Subscription onClearSdResult() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .subscribeOn(Schedulers.io())
                .filter(ret -> TextUtils.equals(ret.uuid, uuid))
                .flatMap(deviceSyncRsp -> Observable.from(deviceSyncRsp.dpList))
                .filter(ret -> ret.id == 204 || ret.id == 222)
                .observeOn(AndroidSchedulers.mainThread())
                .map(jfgDpMsg -> {
                    if (jfgDpMsg.id == 204) {
                        getView().clearSdResult(0);
                        getView().initSdUseDetailRsp(null);
                    } else if (jfgDpMsg.id == 222) {
//                        DpMsgDefine.DPSdcardSummary sdcardSummary = null;
//                        try {
//                            sdcardSummary = DpUtils.unpackData(jfgDpMsg.packValue, DpMsgDefine.DPSdcardSummary.class);
//                            if (sdcardSummary == null || !sdcardSummary.hasSdcard)
//                                getView().showSdPopDialog();
//                        } catch (Exception e) {
//                            AppLogger.e("err: " + MiscUtils.getErr(e));
//                        }
                    }
                    return null;
                })
                .subscribe(o -> {
                }, e -> AppLogger.d(e.getMessage()));
    }

    /**
     * 获取到sd卡的容量
     *
     * @param uuid
     */
    @Override
    public void getSdCapacity(String uuid) {
        addSubscription(Observable.just(new DPEntity()
                .setMsgId(204)
                .setUuid(uuid)
                .setAction(DBAction.QUERY)
                .setOption(DBOption.SingleQueryOption.ONE_BY_TIME))
                .flatMap(entity -> BaseApplication.getAppComponent().getTaskDispatcher().perform(entity))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> getView().initSdUseDetailRsp(null), throwable -> {
                    AppLogger.e("err:" + MiscUtils.getErr(throwable));
                    getView().initSdUseDetailRsp(null);
                }), "getSdCapacity");
    }
}
