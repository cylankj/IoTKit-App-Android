package com.cylan.jiafeigou.misc.bind;

import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.BindUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 17-6-9.
 */

public abstract class AbstractTask implements ISubmit<AbstractTask.SResult>, SimulatePercent.OnAction {

    /**
     * 一般来说 一次只有一个设备在绑定.
     */
    protected SubmitListener submitListener;
    protected SResult submitResult;
    private SimulatePercent simulatePercent;

    @Override
    public SResult getSubmitResult() {
        return submitResult;
    }

    @Override
    public void startTask(String uuid) {
        SResult result = new SResult();
        result.uuid = uuid;
        this.submitResult = result;
        simulatePercent = new SimulatePercent();
        simulatePercent.setOnAction(this);
        updateResult(BindUtils.BIND_PREPARED);
        sendBindInfoObservable(uuid);
    }

    private void updateResult(int state) {
        if (submitResult != null) submitResult.state = state;
    }

    private Observable<SResult> observableResult() {
        return Observable.interval(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .flatMap(aLong -> {
                    if (2 * aLong * 1000 > getTimeout()) {
                        //超时了
                        if (simulatePercent != null) simulatePercent.stop();
                        updateResult(BindUtils.BIND_TIME_OUT);
                        if (submitListener != null)
                            submitListener.onSubmitErr(BindUtils.BIND_TIME_OUT);
                        throw new IllegalArgumentException("超时了");
                    }
                    boolean s = successCondition();
                    if (s) {
                        if (simulatePercent != null) simulatePercent.boost();
                        updateResult(BindUtils.BIND_SUC);
                        throw new RxEvent.HelperBreaker("成功了");
                    }
                    updateResult(BindUtils.BIND_ING);
                    return Observable.just(submitResult);
                });
    }

    protected abstract long getTimeout();

    protected abstract boolean successCondition();

    protected abstract Func1<String, Boolean> sendInfo();

    private Observable<Boolean> sendBindInfoObservable(String uuid) {
        return Observable.just(uuid)
                .subscribeOn(Schedulers.io())
                .map(sendInfo())
                .filter(ret -> ret);
    }

    @Override
    public void cancelTask() {
        this.submitResult = null;
        this.submitListener = null;
    }

    @Override
    public void setTaskListener(SubmitListener listener) {
        this.submitListener = listener;
    }

    @Override
    public void actionDone() {
        if (submitListener != null) submitListener.onSubmitSuccess();
    }

    @Override
    public void actionPercent(int percent) {
        if (submitListener != null) submitListener.onSubmitProgress(percent);
    }

    public static final class SResult extends SubmitResult {

    }
}
