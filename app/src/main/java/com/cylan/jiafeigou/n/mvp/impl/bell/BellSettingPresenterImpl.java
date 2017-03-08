package com.cylan.jiafeigou.n.mvp.impl.bell;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.JFGDoorBellDevice;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class BellSettingPresenterImpl extends BasePresenter<BellSettingContract.View>
        implements BellSettingContract.Presenter {

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
    }

    @Override
    public void onStart() {
        super.onStart();
        JFGDoorBellDevice device = mSourceManager.getJFGDevice(mUUID);
        mView.onShowProperty(device);
    }


    @Override
    public void unbindDevice() {
        registerSubscription(Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .map((Object o) -> {
                    boolean result = DataSourceManager.getInstance().delRemoteJFGDevice(mUUID);
                    AppLogger.i("unbind uuid: " + mUUID + " " + result);
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .zipWith(RxBus.getCacheInstance().toObservable(RxEvent.UnBindDeviceEvent.class)
                                .subscribeOn(Schedulers.newThread())
                                .timeout(3000, TimeUnit.MILLISECONDS, Observable.just("unbind timeout")
                                        .subscribeOn(AndroidSchedulers.mainThread())
                                        .map(s -> {
                                            mView.unbindDeviceRsp(-1);
                                            return null;
                                        }))
                                .filter(s -> mView != null)
                                .observeOn(AndroidSchedulers.mainThread())
                                .filter(unbindEvent -> {
                                    if (unbindEvent.jfgResult.code != 0)
                                        mView.unbindDeviceRsp(unbindEvent.jfgResult.code);//失败
                                    return unbindEvent.jfgResult.code == 0;
                                }),
                        (Object o, RxEvent.UnBindDeviceEvent unbindEvent) -> {
                            mView.unbindDeviceRsp(0);//成功
                            DataSourceManager.getInstance().delLocalJFGDevice(mUUID);
                            return null;
                        })
                .subscribe());
    }

    @Override
    public void clearBellRecord(String uuid) {
        Subscription subscribe = Observable.just(new DPEntity()
                .setMsgId(DpMsgMap.ID_401_BELL_CALL_STATE)
                .setUuid(uuid)
                .setAction(DBAction.CLEARED))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(this::perform)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp.getResultCode() == 0) {//删除成功
                        mView.onClearBellRecordSuccess();
                        RxBus.getCacheInstance().post(new RxEvent.ClearDataEvent(DpMsgMap.ID_401_BELL_CALL_STATE));
                        AppLogger.d("清空呼叫记录成功!");
                    } else {
                        mView.onClearBellRecordFailed();
                        AppLogger.d("清空呼叫记录失败");
                    }
                }, e -> {
                    mView.onClearBellRecordFailed();
                    AppLogger.d(e.getMessage());
                    AppLogger.d("清空呼叫记录失败!");
                });
        registerSubscription(subscribe);
    }
}
