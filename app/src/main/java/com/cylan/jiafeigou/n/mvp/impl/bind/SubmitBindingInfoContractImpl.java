package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGResult;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.n.engine.DataSourceService;
import com.cylan.jiafeigou.n.mvp.contract.bind.SubmitBindingInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ListUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.cylan.jiafeigou.utils.BindUtils.BIND_SUC;
import static com.cylan.jiafeigou.utils.BindUtils.BIND_TIME_OUT;

/**
 * Created by cylan-hunt on 16-11-12.
 */

public class SubmitBindingInfoContractImpl extends AbstractPresenter<SubmitBindingInfoContract.View>
        implements SubmitBindingInfoContract.Presenter, SimulatePercent.OnAction {

    private SimulatePercent simulatePercent;
    private CompositeSubscription subscription;
    private int bindResult;
    private long startTick;

    public SubmitBindingInfoContractImpl(SubmitBindingInfoContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
        simulatePercent = new SimulatePercent();
        simulatePercent.setOnAction(this);
        bindResult = BindUtils.BIND_PREPARED;
    }

    @Override
    public void startCounting() {
        if (simulatePercent != null)
            simulatePercent.start();
    }

    @Override
    public void endCounting() {
        if (simulatePercent != null)
            simulatePercent.stop();
    }

    @Override
    public int getBindState() {
        return bindResult;
    }

    @Override
    public void setBindState(int bindState) {
        this.bindResult = bindState;
    }

    @Override
    public void clean() {
        RxBus.getCacheInstance().removeStickyEvent(RxEvent.BindDeviceEvent.class);
    }

    @Override
    public void start() {
        super.start();
        Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
        if (startTick == 0) {//可能是覆盖绑定.
            startTick = System.currentTimeMillis();
            //1.可能是覆盖绑定,或者设备列表中已经有了该设备,并且在线状态.
            if (device != null) {
                //2.清空net状态
                device.setValue(201, new DpMsgDefine.DPNet());//先清空
            }
        }
        if (System.currentTimeMillis() - startTick > 90 * 1000) {
            //timeout
            mView.bindState(BIND_TIME_OUT);
            return;
        }
        //3.重新获取,
        DpMsgDefine.DPNet net = device == null ? null : device.$(201, new DpMsgDefine.DPNet());
        if (device != null && !TextUtils.isEmpty(uuid) && net != null && net.net > 0) {
            //4.net数据可能已经被更新了(重新进入该页面时候使用.)
            mView.bindState(BIND_SUC);
            endCounting();
            AppLogger.d("finish? ;" + net);
            return;
        }
        //超时
        if (bindResult == BindUtils.BIND_PREPARED) {
            bindResult = BindUtils.BIND_ING;
            if ((subscription == null || subscription.isUnsubscribed())) {
                subscription = new CompositeSubscription();
                AppLogger.d("add sub result");
                subscription.add(bindResultSub());
                subscription.add(bindResultSub1());
                subscription.add(robotDeviceDataSync());
            }
        }
        if (bindResult == BindUtils.BIND_ING) {
            if (simulatePercent != null) simulatePercent.resume();
        }
    }

    /**
     * 绑定结果:通过{@link DataSourceService#OnResult(JFGResult)}
     * {@link com.cylan.jiafeigou.misc.JResultEvent#JFG_RESULT_BINDDEV}
     *
     * @return
     */
    private Subscription bindResultSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.BindDeviceEvent.class)
                .observeOn(Schedulers.newThread())
                .filter((RxEvent.BindDeviceEvent bindDeviceEvent) -> getView() != null/** && TextUtils.equals(bindDeviceEvent.uuid, uuid)**/)
                .filter(viceEvent -> getView() != null && viceEvent != null)
                .observeOn(AndroidSchedulers.mainThread())
                .map((RxEvent.BindDeviceEvent result) -> {
                    getView().bindState(bindResult = result.bindResult);
                    AppLogger.i("bind result: " + result);
                    return null;
                })
                .doOnError(throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()))
                .subscribe();
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private Subscription robotDeviceDataSync() {
        long time = System.currentTimeMillis() - startTick;
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .subscribeOn(Schedulers.newThread())
                .timeout(90 * 1000L - time, TimeUnit.MILLISECONDS)
                .filter(jfgRobotSyncData -> (ListUtils.getSize(jfgRobotSyncData.dpList) > 0))
                .filter(ret -> uuid != null && TextUtils.equals(uuid, ret.uuid) && mView != null)
                .flatMap(ret -> Observable.from(ret.dpList))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msg -> {
                    try {
                        if (msg.id == 201)//网络
                        {
                            Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
                            if (device != null) {
                                DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
                                if (net.net > 0) {
                                    mView.bindState(0);
                                    endCounting();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    if (throwable instanceof TimeoutException) {
                        mView.bindState(BIND_TIME_OUT);
                    } else {
                        addSubscription(robotDeviceDataSync());
                    }
                    AppLogger.e("err: " + throwable.getLocalizedMessage());
                });
    }

    private Subscription bindResultSub1() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DevicesArrived.class)
                .observeOn(Schedulers.newThread())
                .flatMap(ret -> Observable.from(ret.devices))
                .filter(device -> getView() != null && TextUtils.equals(device.uuid, uuid))
                .filter(viceEvent -> getView() != null && viceEvent != null)
                .observeOn(AndroidSchedulers.mainThread())
                .map((Device result) -> {
                    DpMsgDefine.DPNet net = result.$(201, new DpMsgDefine.DPNet());
                    if (net.net > 0) {
                        getView().bindState(bindResult = BIND_SUC);
                        if (simulatePercent != null && bindResult == BIND_SUC) {
                            simulatePercent.boost();
                        }
                        AppLogger.i("bind success: " + result);
                    }
                    return null;
                })
                .doOnError(throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()))
                .subscribe();
    }

    @Override
    public void stop() {
        super.stop();
        if (bindResult == BindUtils.BIND_ING) {
            if (simulatePercent != null) simulatePercent.resume();
        } else {
            if (simulatePercent != null)
                simulatePercent.stop();
        }
        bindResult = BindUtils.BIND_PREPARED;
        unSubscribe(subscription);
    }

    @Override
    public void actionDone() {
        Subscription subscription = Observable.just(null)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe((Object integer) -> {
                    AppLogger.i("actionDone: " + integer);
                    getView().bindState(BIND_SUC);
                });
        addSubscription(subscription, "actionDone");
    }

    @Override
    public void actionPercent(int percent) {
        Subscription subscription = Observable.just(percent)
                .filter((Integer integer) -> (getView() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Integer integer) -> {
                    getView().onCounting(integer);
                });
        addSubscription(subscription, "actionPercent");
    }

}
