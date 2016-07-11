package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.net.wifi.ScanResult;

import com.cylan.jiafeigou.cache.SimpleCache;
import com.cylan.jiafeigou.misc.ScanResultListFilter;
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

    private ScanResultListFilter scanResultListFilter;

    public BindDevicePresenterImpl(BindDeviceContract.View view) {
        super(view);
        view.setPresenter(this);
        scanResultListFilter = new ScanResultListFilter();
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
                .debounce(200, TimeUnit.MILLISECONDS)
                .map(new Func1<List<ScanResult>, Result>() {
                    @Override
                    public Result call(List<ScanResult> resultList) {
                        resultList = new ArrayList<>(scanResultListFilter.extractPretty(resultList));
                        SimpleCache.getInstance().setWeakScanResult(resultList);
                        Result result = new Result();
                        result.errState = resultList.size() == 0 ? BindDeviceContract.STATE_NO_RESULT : BindDeviceContract.STATE_HAS_RESULT;
                        if (result.errState == BindDeviceContract.STATE_NO_RESULT) {
                            return result;
                        }
                        List<ScanResult> newList = new ArrayList<>(scanResultListFilter.extractJFG(resultList, filters));
                        newList = resultList;
                        //没有设备
                        if (newList.size() == 0)
                            result.errState = BindDeviceContract.STATE_NO_JFG_DEVICE;
                        result.jfgList = newList;
                        return result;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Result>() {
                    @Override
                    public void call(Result result) {
                        if (getView() == null) {
                            return;
                        }
                        //没有wifi列表
                        if (result == null
                                || result.errState == BindDeviceContract.STATE_NO_RESULT) {
                            getView().onNoListError();
                            return;
                        }
                        //有wifi列表，但没有狗设备
                        if (result.errState == BindDeviceContract.STATE_NO_JFG_DEVICE) {
                            getView().onNoJFGDevices();
                            return;
                        }
                        //有设备了
                        if (getView() != null)
                            getView().onDevicesRsp(result.jfgList);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        SLog.e("good: " + throwable.toString());
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

        List<ScanResult> jfgList;
        int errState;
    }
}
