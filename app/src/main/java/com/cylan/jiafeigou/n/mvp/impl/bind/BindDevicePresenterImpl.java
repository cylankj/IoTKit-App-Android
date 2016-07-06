package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.net.wifi.ScanResult;

import com.cylan.jiafeigou.n.mvp.contract.bind.BindDeviceContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.superlog.SLog;

import java.util.ArrayList;
import java.util.List;

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
    public void scanDevices() {
        if (getView() != null && getView().getContext() != null)
            subscription = new ReactiveNetwork().observeWifiAccessPoints(getView().getContext(), false)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<List<ScanResult>, List<ScanResult>>() {
                        @Override
                        public List<ScanResult> call(List<ScanResult> resultList) {
                            if (resultList == null) {
                                return null;
                            }
                            List<ScanResult> newList = new ArrayList<>();
                            for (ScanResult scanResult : resultList) {
                                if (scanResult.SSID != null && scanResult.SSID.contains("DOG-")) {
                                    newList.add(scanResult);
                                }
                            }
                            return newList;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<ScanResult>>() {
                        @Override
                        public void call(List<ScanResult> resultList) {
                            if (resultList != null && getView() != null) {
                                getView().onDevicesRsp(resultList);
                            } else {
                                SLog.e("some thing wrong");
                            }
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

    }
}
