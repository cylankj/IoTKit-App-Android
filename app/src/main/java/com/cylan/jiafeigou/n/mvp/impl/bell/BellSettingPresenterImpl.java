package com.cylan.jiafeigou.n.mvp.impl.bell;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
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
        registerSubscription(getUnbindDevSub());
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
                    GlobalDataProxy.getInstance().deleteJFGDevice(mUUID);
                    AppLogger.i("unbind uuid: " + mUUID);
                }
        );
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
                        mView.onClearBellRecordFaild();
                        AppLogger.d("清空呼叫记录失败");
                    }
                }, e -> {
                    mView.onClearBellRecordFaild();
                    AppLogger.d("清空呼叫记录失败!");
                });
    }

}
