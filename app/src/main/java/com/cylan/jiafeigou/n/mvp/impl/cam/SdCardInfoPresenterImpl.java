package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.mvp.contract.cam.SdCardInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    private boolean isClearSucc;

    public SdCardInfoPresenterImpl(SdCardInfoContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                onClearSdReqBack(),
                onClearSdResult()
        };
    }

    /**
     * 是否有SD卡
     *
     * @return
     */
    @Override
    public boolean getSdcardState() {
        DpMsgDefine.DPSdStatus sdStatus = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_204_SDCARD_STORAGE);
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
    public <T extends DataPoint> void updateInfoReq(T value, long id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        DataSourceManager.getInstance().updateValue(uuid, value, (int) id);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }

    @Override
    public void clearCountTime() {
        Observable.just(null)
                .delay(2, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Object o) -> {
                    if (getView() != null && !isClearSucc) getView().clearSdResult(2);
                });
    }

    @Override
    public Subscription onClearSdReqBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SdcardClearRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.SdcardClearRsp sdcardClearRsp) -> {
                    if (sdcardClearRsp != null) {
                        isClearSucc = true;
                        JFGDPMsgRet jfgdpMsgRet = sdcardClearRsp.arrayList.get(0);
                        if (jfgdpMsgRet.id == 218 && jfgdpMsgRet.ret == 0) {
                            //
                        } else {
                            getView().clearSdResult(1);
                        }
                    }
                });
    }

    @Override
    public Subscription onClearSdResult() {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RobotoGetDataRsp rsp) -> {
                    if (rsp != null) {
                        for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : rsp.map.entrySet()) {
                            if (entry.getValue() == null) continue;
                            for (JFGDPMsg dp : entry.getValue()) {
                                if (dp.id == 204) {
                                    getView().clearSdResult(0);
                                }
                            }
                        }
                    }
                });
    }

}
