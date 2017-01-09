package com.cylan.jiafeigou.n.mvp.impl.bell;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class BellSettingPresenterImpl extends BasePresenter<BellSettingContract.View>
        implements BellSettingContract.Presenter {

    @Override
    protected void onRegisterSubscription(CompositeSubscription subscriptions) {
        super.onRegisterSubscription(subscriptions);
        subscriptions.add(getUnbindDevSub());
    }

    @Override
    public void onSetContentView() {
        super.onSetContentView();
        mView.onDeviceSyncRsp(mSourceManager.getJFGDevice(mUUID));
    }

    /**
     * 门铃解绑
     *
     * @return
     */
    private Subscription getUnbindDevSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.UnBindDeviceEvent.class)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(unBindDeviceEvent -> {
                    mView.unbindDeviceRsp(unBindDeviceEvent.jfgResult.code);
                    if (unBindDeviceEvent.jfgResult.code == 0) {
                        //清理这个订阅
                        RxBus.getCacheInstance().removeStickyEvent(RxEvent.UnBindDeviceEvent.class);
                    }
                    return null;
                })
                .retry(new RxHelper.RxException<>("getUnbindDevSub"))
                .subscribe();
    }

    @Override
    public void unbindDevice() {
        post(() -> {
                    RxEvent.UnbindJFGDevice deletion = new RxEvent.UnbindJFGDevice();
                    deletion.uuid = mUUID;
                    RxBus.getCacheInstance().post(deletion);
                    try {
                        JfgCmdInsurance.getCmd().unBindDevice(mUUID);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    AppLogger.i("unbind uuid: " + mUUID);
                }
        );
    }
}
