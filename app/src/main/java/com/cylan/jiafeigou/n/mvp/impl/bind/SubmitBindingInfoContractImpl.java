package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.text.TextUtils;

import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.mvp.contract.bind.SubmitBindingInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.utils.ListUtils;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by cylan-hunt on 16-11-12.
 */

public class SubmitBindingInfoContractImpl extends
        AbstractPresenter<SubmitBindingInfoContract.View>
        implements SubmitBindingInfoContract.Presenter,
        SimulatePercent.OnAction {

    private SimulatePercent simulatePercent;

    private Subscription bindSub;
    private UdpConstant.UdpDevicePortrait portrait;

    public SubmitBindingInfoContractImpl(SubmitBindingInfoContract.View view,
                                         UdpConstant.UdpDevicePortrait portrait) {
        super(view);
        view.setPresenter(this);
        this.portrait = portrait;
        simulatePercent = new SimulatePercent();
        simulatePercent.setOnAction(this);
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
    public void start() {
        unSubscribe(bindSub);
        bindSub = RxBus.getDefault().toObservableSticky(RxEvent.DeviceList.class)
                .filter(new Func1<RxEvent.DeviceList, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.DeviceList deviceList) {
                        return getView() != null
                                && deviceList != null
                                && !ListUtils.isEmpty(deviceList.jfgDevices);
                    }
                })
                .flatMap(new Func1<RxEvent.DeviceList, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(RxEvent.DeviceList deviceList) {
                        final int count = deviceList.jfgDevices.size();
                        for (int i = 0; i < count; i++) {
                            if (TextUtils.equals(portrait.cid, deviceList.jfgDevices.get(i).uuid)) {
                                //hit the binding cid
                                return Observable.just(true);
                            }
                        }
                        return Observable.just(false);
                    }
                })
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return aBoolean;
                    }
                })
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (simulatePercent != null)
                            simulatePercent.boost();
                    }
                });
    }

    @Override
    public void stop() {
        unSubscribe(bindSub);
        if (simulatePercent != null)
            simulatePercent.stop();
        portrait = null;
    }

    @Override
    public void actionDone() {
        Observable.just(null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object integer) {
                        getView().onSuccess();
                    }
                });
    }

    @Override
    public void actionPercent(int percent) {
        Observable.just(percent)
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return getView() != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        getView().onCounting(integer);
                    }
                });
    }

}
