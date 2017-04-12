package com.cylan.jiafeigou.n.mvp.impl.bell;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellDetailContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_207_DEVICE_VERSION;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class BellDetailSettingPresenterImpl extends BasePresenter<BellDetailContract.View>
        implements BellDetailContract.Presenter {

    private CompositeSubscription subscription;

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(checkNewVersionBack());
    }

    @Override
    public void onStart() {
        super.onStart();
        Device device = mSourceManager.getJFGDevice(mUUID);
        if (device != null) {
            mView.onShowProperty(device);
        }
    }

    @Override
    public <T extends DataPoint> void updateInfoReq(String uuid, T value, long id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    AppLogger.i("save start: " + id + " " + value);
                    try {
                        DataSourceManager.getInstance().updateValue(uuid, value, (int) id);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                    AppLogger.i("save end: " + id + " " + value);
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }

    @Override
    public void checkNewVersion(String uuid) {
        //检测是否有新的固件
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(o -> {
                    Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
                    DpMsgDefine.DPPrimary<String> sVersion = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_207_DEVICE_VERSION, null);
                    try {
                        long req = JfgCmdInsurance.getCmd().checkDevVersion(device.pid, uuid, device.$(ID_207_DEVICE_VERSION, ""));
                        AppLogger.d("Bell_checkNewVersion:" + req);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e("Bell_checkNewVersion:" + e.getLocalizedMessage());
                    }
                }, AppLogger::e);
    }

    @Override
    public Subscription checkNewVersionBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckDevVersionRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.CheckDevVersionRsp checkDevVersionRsp) -> {
                    if (checkDevVersionRsp != null) {
                        mView.checkResult(checkDevVersionRsp);
                    }
                }, AppLogger::e);
    }

}
