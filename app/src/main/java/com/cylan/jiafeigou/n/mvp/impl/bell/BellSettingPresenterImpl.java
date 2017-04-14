package com.cylan.jiafeigou.n.mvp.impl.bell;

import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.concurrent.TimeoutException;

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
        Device device = sourceManager.getDevice(mUUID);
        if (device != null) {
            mView.onShowProperty(device);
        }
    }


    @Override
    public void unbindDevice() {
        Subscription subscribe = Observable.just(new DPEntity()
                .setUuid(mUUID)
                .setAction(DBAction.UNBIND))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(this::perform)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> mView.unbindDeviceRsp(rsp.getResultCode()), e -> {
                    if (e instanceof TimeoutException) {
                        mView.unbindDeviceRsp(-1);
                    }
                    AppLogger.d(e.getMessage());
                    e.printStackTrace();
                }, () -> {
                });
        registerSubscription(subscribe);
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
