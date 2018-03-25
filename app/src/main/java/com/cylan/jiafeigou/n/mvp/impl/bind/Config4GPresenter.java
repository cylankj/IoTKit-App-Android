package com.cylan.jiafeigou.n.mvp.impl.bind;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.mvp.contract.bind.Config4GContract;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.APObserver;
import com.cylan.jiafeigou.utils.BindHelper;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.WifiUtils;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by yanzhendong on 2018/3/25.
 */

public class Config4GPresenter extends BasePresenter<Config4GContract.View> implements Config4GContract.Presenter {

    @Inject
    public Config4GPresenter(Config4GContract.View view) {
        super(view);
    }


    @Override
    public void performSIMCheckerAndGoNext() {
        Subscription subscribe = APObserver.scanDogWiFi()
                .map(result -> {
                    if (result != null && result.size() > 0) {
                        String shortCid = BindUtils.filterCylanDeviceShortCid(WifiUtils.getSSID(ContextUtils.getContext()));
                        for (APObserver.ScanResult scanResult : result) {
                            if (scanResult.getUuid().endsWith(shortCid) && !DataSourceManager.getInstance().getDevice(scanResult.getUuid()).available()) {
                                UdpConstant.UdpDevicePortrait devicePortrait = new UdpConstant.UdpDevicePortrait();
                                devicePortrait.mac = scanResult.getMac();
                                devicePortrait.net = scanResult.getNet();
                                devicePortrait.pid = scanResult.getOs();
                                devicePortrait.version = scanResult.getVersion();
                                devicePortrait.uuid = scanResult.getUuid();
                                devicePortrait.bindFlag = 0;//不强绑
                                PreferencesUtils.putString(JConstant.BINDING_DEVICE, new Gson().toJson(devicePortrait));
                                return scanResult;
                            }
                        }
                    }
                    return null;
                })
                .filter(result -> result != null)
                .flatMap(scanResult -> BindHelper.sendServerConfig(scanResult.getUuid(), scanResult.getMac(), JFGRules.getLanguageType()))
                .map(success -> {
                    MiscUtils.recoveryWiFi();
                    return success;
                })
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(applyLoading(false, R.string.LOADING))
                .subscribe(result -> {
                    if (result == null) {
                        mView.onSIMCheckerFailed();
                    } else {
                        mView.onSIMCheckerSuccess();
                    }
                }, error -> {
                    error.printStackTrace();
                    AppLogger.e(error);
                    mView.onSIMCheckerFailed();
                });
        addStopSubscription(subscribe);
    }
}
