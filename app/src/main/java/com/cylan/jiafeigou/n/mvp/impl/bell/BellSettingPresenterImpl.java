package com.cylan.jiafeigou.n.mvp.impl.bell;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.JFGDoorBellDevice;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
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
        Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            JFGDPMsg request = new JFGDPMsg(DpMsgMap.ID_401_BELL_CALL_STATE, -1);
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            params.add(request);
            try {
                long seq = JfgCmdInsurance.getCmd().robotDelData(uuid, params, 0);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                AppLogger.e(e.getMessage());
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class).filter(rsp -> rsp.seq == seq).first().timeout(10, TimeUnit.SECONDS))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp.resultCode == 0) {//删除成功
                        mView.onClearBellRecordSuccess();
                        AppLogger.d("清空呼叫记录成功!");
                    } else {
                        mView.onClearBellRecordFailed();
                        AppLogger.d("清空呼叫记录失败");
                    }
                }, e -> {
                    mView.onClearBellRecordFailed();
                    AppLogger.d("清空呼叫记录失败!");
                });
    }

}
