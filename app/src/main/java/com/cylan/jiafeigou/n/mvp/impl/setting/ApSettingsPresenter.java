package com.cylan.jiafeigou.n.mvp.impl.setting;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.n.mvp.contract.setting.ApSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.NetUtils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 17-9-7.
 */

public class ApSettingsPresenter extends AbstractPresenter<ApSettingContract.View>
        implements ApSettingContract.Presenter {
    public ApSettingsPresenter(ApSettingContract.View view) {
        super(view);
    }

    @Override
    public void setPresenter(Object presenter) {

    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void monitorHotSpot() {
        //还有另外一种方式，直接ping此设备
        final String mac = getDevice().$(202, "");
        Log.d(TAG, "monitorHotSpot ,mac:" + mac);
        if (TextUtils.isEmpty(mac)) {
            return;
        }
        if (hasSubscroption("monitorHotSpot")) {
            return;
        }
        Subscription subscription = Observable.interval(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .flatMap(aLong -> {
                    if (aLong * 2 >= 90) {
                        throw new RxEvent.HelperBreaker("timeout");
                    }
                    ArrayList<NetUtils.ClientScanResult> list = NetUtils.getClientList(true, 1000);
                    if (ListUtils.isEmpty(list)) {
                        return Observable.just(false);
                    }
                    for (NetUtils.ClientScanResult ret : list) {
                        if (TextUtils.equals(mac.toLowerCase(), ret.getHWAddr().toLowerCase())) {
                            return Observable.just(true);
                        }
                    }
                    return Observable.just(false);
                })
                .filter(aBoolean -> {
                    if (aBoolean) {
                        AppLogger.d("设备已经连上热点");
                        //更新设备的网络
                        DpMsgDefine.DPNet net = new DpMsgDefine.DPNet(1, mView.getHotSpotName());
                        ArrayList<JFGDPMsg> arrayList = new ArrayList<>();
                        arrayList.add(new JFGDPMsg(201L, System.currentTimeMillis(), DpUtils.pack(net)));
                        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheSyncDataEvent(true,
                                uuid, arrayList));
                        throw new RxEvent.HelperBreaker("good");
                    }
                    return aBoolean;
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                }, throwable -> {
                    if (throwable instanceof RxEvent.HelperBreaker) {
                        final String content = throwable.getLocalizedMessage();
                        if (TextUtils.equals(content, "good")) {
                            mView.success();
                        } else if (TextUtils.equals(content, "timeout")) {
                            mView.timeout();
                        }
                    }
                });
        addSubscription(subscription, "monitorHotSpot");
    }
}
