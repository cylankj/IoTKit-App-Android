package com.cylan.jiafeigou.n.mvp.impl.bind;

import com.cylan.entity.jniCall.JFGResult;
import com.cylan.jiafeigou.misc.JResultEvent;
import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.n.engine.DataSource;
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
import rx.functions.Action1;
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
    public void start() {
        super.start();
        //超时
        if (bindResult == BindUtils.BIND_PREPARED) {
            bindResult = BindUtils.BIND_ING;
            if ((subscription == null || subscription.isUnsubscribed())) {
                subscription = bindResultSub();
            }
        }
    }

    /**
     * 绑定结果:通过{@link DataSource#OnResult(JFGResult)}
     * {@link com.cylan.jiafeigou.misc.JResultEvent#JFG_RESULT_BINDDEV}
     *
     * @return
     */
    private Subscription bindResultSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.BindDeviceEvent.class)
                .observeOn(Schedulers.newThread())
                .filter((RxEvent.BindDeviceEvent bindDeviceEvent) -> getView() != null && bindDeviceEvent.jfgResult.event == JResultEvent.JFG_RESULT_BINDDEV)
                .timeout(90, TimeUnit.SECONDS, Observable.just("timeout")
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .filter(s -> getView() != null)
                        .map(s -> {
                            bindResult = BindUtils.BIND_TIME_OUT;
                            getView().bindState(BindUtils.BIND_TIME_OUT);
                            AppLogger.e("timeout: " + s);
                            return null;
                        }))
                .filter(viceEvent -> getView() != null)
                .map((RxEvent.BindDeviceEvent result) -> {
                    bindResult = result.jfgResult.code;
                    getView().bindState(bindResult);
                    if (simulatePercent != null && result.jfgResult.code == 0) {
                        simulatePercent.boost();
                    }
                    AppLogger.i("bind success");
                    return null;
                })
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {

                    }
                }, throwable ->
                        AppLogger.e("err:" + throwable.getLocalizedMessage()));
    }

    @Override
    public void stop() {
        super.stop();
        if (simulatePercent != null)
            simulatePercent.stop();
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
