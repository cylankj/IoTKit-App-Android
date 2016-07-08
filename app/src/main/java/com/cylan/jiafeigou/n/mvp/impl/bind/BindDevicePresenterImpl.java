package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.net.wifi.ScanResult;
import android.text.TextUtils;

import com.cylan.jiafeigou.n.mvp.contract.bind.BindDeviceContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.superlog.SLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-6.
 */
public class BindDevicePresenterImpl extends AbstractPresenter<BindDeviceContract.View> implements BindDeviceContract.Presenter {

    private Subscription subscription;

    public BindDevicePresenterImpl(BindDeviceContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void scanDevices(final String... filters) {
        if (getView() == null || getView().getContext() == null)
            return;
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        subscription = new ReactiveNetwork()
                .observeWifiAccessPoints(getView().getContext().getApplicationContext(),
                        false)
                .subscribeOn(Schedulers.io())
                .map(new Func1<List<ScanResult>, Result>() {
                    @Override
                    public Result call(List<ScanResult> resultList) {
                        if (resultList == null) {
                            return null;
                        }
                        Result result = new Result();
                        result.state = resultList.size() == 0 ? BindDeviceContract.STATE_NO_RESULT : BindDeviceContract.STATE_HAS_RESULT;
                        if (result.state == BindDeviceContract.STATE_NO_RESULT) {
                            return result;
                        }
                        List<ScanResult> newList = new ArrayList<>();
                        for (ScanResult scanResult : resultList) {
                            if (!TextUtils.isEmpty(scanResult.SSID)) {
                                newList.add(scanResult);
                            }
                        }
                        //没有设备
                        if (newList.size() == 0)
                            result.state = BindDeviceContract.STATE_NO_JFG_DEVICE;
                        result.resultList = newList;
                        return result;
                    }
                })
                .debounce(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Result>() {
                    @Override
                    public void call(Result result) {
                        checkNull();
                        //没有wifi列表
                        if (result == null
                                || result.state == BindDeviceContract.STATE_NO_RESULT) {
                            getView().onNoListError();
                            return;
                        }
                        //有wifi列表，但没有狗设备
                        if (result.state == BindDeviceContract.STATE_NO_JFG_DEVICE) {
                            getView().onNoJFGDevices();
                            return;
                        }
                        //有设备了
                        getView().onDevicesRsp(result.resultList);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        SLog.e("good: " + throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        unSubscribe(subscription);
    }

    private static class Result {

        List<ScanResult> resultList;
        int state;
    }
}
