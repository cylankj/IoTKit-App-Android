package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JResultEvent;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class DeviceInfoDetailPresenterImpl extends AbstractPresenter<CamInfoContract.View>
        implements CamInfoContract.Presenter {

    //    private BeanCamInfo beanCamInfo;
    private String uuid;
    private long requst;

    public DeviceInfoDetailPresenterImpl(CamInfoContract.View view, String uuid) {
        super(view);
        this.uuid = uuid;
        view.setPresenter(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                checkNewSoftVersionBack(),
                clearSdcardBack()
        };
    }

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
    public void checkNewSoftVersion() {
        // TODO 检测是否有新的固件
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        JFGDevice device = GlobalDataProxy.getInstance().fetch(uuid);
                        String sVersion = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_207_DEVICE_VERSION, "");
                        try {
                            JfgCmdInsurance.getCmd().checkDevVersion(device.pid, uuid, sVersion);
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
                    ArrayList<JFGDPMsg> ipList = new ArrayList<JFGDPMsg>();
                    JFGDPMsg mesg = new JFGDPMsg(DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD, 0);
                    ipList.add(mesg);
                    try {
                        requst = JfgCmdInsurance.getCmd().robotSetData(uuid, ipList);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public Subscription clearSdcardBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SdcardClearRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.SdcardClearRsp respone) -> {
                    if (respone != null && respone.seq == requst) {
                        if (respone.arrayList.get(0).ret == 0) {
                            getView().clearSdResult(0);
                        } else {
                            getView().clearSdResult(1);
                        }
                    }
                });
    }

    @Override
    public void updateAlias(JFGDevice device) {
        Observable.just(device)
                .map(device1 -> {
                    GlobalDataProxy.getInstance().updateJFGDevice(device);
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
                        throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
    }
}
