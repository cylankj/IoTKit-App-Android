package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGResult;
import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.n.engine.DataSourceService;
import com.cylan.jiafeigou.n.mvp.contract.bind.SubmitBindingInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-12.
 */

public class SubmitBindingInfoContractImpl extends AbstractPresenter<SubmitBindingInfoContract.View>
        implements SubmitBindingInfoContract.Presenter, SimulatePercent.OnAction {

    private SimulatePercent simulatePercent;
    private Subscription subscription;
    private int bindResult;

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
        //超时
        if (bindResult == BindUtils.BIND_PREPARED) {
            bindResult = BindUtils.BIND_ING;
            if ((subscription == null || subscription.isUnsubscribed())) {
                subscription = bindResultSub();
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
                .filter((RxEvent.BindDeviceEvent bindDeviceEvent) -> getView() != null && TextUtils.equals(bindDeviceEvent.uuid, uuid))
                .timeout(90, TimeUnit.SECONDS, Observable.just("timeout")
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .filter(s -> getView() != null)
                        .map(s -> {
                            getView().bindState(bindResult = BindUtils.BIND_TIME_OUT);
                            AppLogger.e("timeout: " + s);
                            return null;
                        }))
                .filter(viceEvent -> getView() != null && viceEvent != null)
                .map((RxEvent.BindDeviceEvent result) -> {
                    getView().bindState(bindResult = result.bindResult);
                    if (simulatePercent != null && bindResult == 0) {
                        simulatePercent.boost();
                    }
                    AppLogger.i("bind success");
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
        Observable.just(null)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe((Object integer) -> {
                    AppLogger.i("actionDone: " + integer);
                    getView().bindState(BindUtils.BIND_SUC);
                });
    }

    @Override
    public void actionPercent(int percent) {
        Observable.just(percent)
                .filter((Integer integer) -> (getView() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Integer integer) -> {
                    getView().onCounting(integer);
                });
    }

}
