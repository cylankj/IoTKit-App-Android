package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.mvp.contract.bind.Config4GContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.APObserver;
import com.cylan.jiafeigou.utils.BindHelper;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.WifiUtils;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2018/3/25.
 */

public class Config4GPresenter extends BasePresenter<Config4GContract.View> implements Config4GContract.Presenter {
    private APObserver.ScanResult scanResult;

    @Inject
    public Config4GPresenter(Config4GContract.View view) {
        super(view);
    }


    @Override
    public void performSIMCheckerAndGoNext() {
        String shortCid = BindUtils.filterCylanDeviceShortCid(WifiUtils.getSSID(ContextUtils.getContext()));
        Subscription subscribe = APObserver.scanDogWiFiRaw()
                .filter(result -> {
                    boolean isExceptUuid = result.getUuid().endsWith(shortCid);
                    if (!isExceptUuid) {
                        return false;
                    }
                    if (scanResult == null) {
                        scanResult = result;
                    }
                    scanResult.setUuid(TextUtils.isEmpty(result.getUuid()) ? scanResult.getUuid() : result.getUuid());
                    scanResult.setIp(TextUtils.isEmpty(result.getIp()) ? scanResult.getIp() : result.getIp());
                    scanResult.setMac(TextUtils.isEmpty(result.getMac()) ? scanResult.getMac() : result.getMac());
                    scanResult.setNet(result.getNet() == 0 ? scanResult.getNet() : result.getNet());
                    scanResult.setOs(result.getOs() == 0 ? scanResult.getOs() : result.getOs());
                    scanResult.setPort(result.getPort() == 0 ? scanResult.getPort() : result.getPort());
                    scanResult.setVersion(TextUtils.isEmpty(result.getVersion()) ? scanResult.getVersion() : result.getVersion());
                    scanResult.setUpdateTime(result.getUpdateTime() == 0 ? scanResult.getUpdateTime() : result.getUpdateTime());
                    boolean scanCompleted = scanResult.getNet() == 2 && !TextUtils.isEmpty(scanResult.getUuid());
                    if (scanCompleted) {
                        UdpConstant.UdpDevicePortrait devicePortrait = new UdpConstant.UdpDevicePortrait();
                        devicePortrait.mac = scanResult.getMac();
                        devicePortrait.net = scanResult.getNet();
                        devicePortrait.pid = scanResult.getOs();
                        devicePortrait.version = scanResult.getVersion();
                        devicePortrait.uuid = scanResult.getUuid();
                        devicePortrait.bindFlag = 0;//不强绑
                        PreferencesUtils.putString(JConstant.BINDING_DEVICE, new Gson().toJson(devicePortrait));
                    }
                    return scanCompleted;
                })
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .flatMap(scanResult -> BindHelper.sendServerConfig(scanResult.getUuid(), scanResult.getMac(), JFGRules.getLanguageType()))
                .flatMap(ret -> BindHelper.sendWiFiConfig(scanResult.getUuid(), "", "", "", 0))
                .map(success -> {
                    MiscUtils.recoveryWiFi();
                    return success;
                })
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(applyLoading(false, R.string.CAMERA4G_INSERTSIM_DETECTING))
                .subscribe(result -> {
                    if (scanResult != null) {
                        switch (scanResult.getNet()) {
                            case 2: {
                                if (mView != null) {
                                    mView.onSIMCheckerSuccess(scanResult);
                                }
                            }
                            break;
                            case -1: {

                            }
                            default: {
                                if (mView != null) {
                                    mView.onSIMCheckerFailed(scanResult);
                                }
                            }
                        }
                    }
                }, error -> {
                    error.printStackTrace();
                    AppLogger.e(error);
                    if (mView != null) {
                        mView.onSIMCheckerFailed(scanResult);
                    }
                });
        addStopSubscription(subscribe);
    }

    @Override
    public void performSendApnTableAndGoNext() {
        Subscription subscribe = Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            String localIp = NetUtils.getReadableIp();
            //需要说明,http_server映射的路径是 /data/data/com.cylan.jiafeigou/files/.200000000086
            String localUrl = "http://" + localIp + ":8765/" + getApnTablePath();
            AppLogger.d("ip:" + localIp + ",localUrl" + localUrl);
            try {
                for (int i = 0; i < 2; i++) {
                    int success = Command.getInstance().sendLocalMessage(scanResult.getIp(), scanResult.getPort(), new UdpConstant.UdpFirmwareUpdate(localUrl, uuid, scanResult.getIp(), 8765).toBytes());
                    subscriber.onNext(0L);
                    subscriber.onCompleted();
                }
            } catch (JfgException e) {
                AppLogger.e("发送升级包失败?" + MiscUtils.getErr(e));
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(i -> RxBus.getCacheInstance().toObservable(RxEvent.FMsgEvent.class))
                .timeout(1, TimeUnit.MINUTES, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (mView != null) {
                        if (result == null) {
                            mView.onApnUpdateTimeout();
                        } else {
                            mView.onApnUpdateSuccess();
                        }
                    }
                }, error -> {
                    error.printStackTrace();
                    AppLogger.e(MiscUtils.getErr(error));
                    if (mView != null) {
                        mView.onApnUpdateError();
                    }
                });
        addDestroySubscription(subscribe);
    }

    private String getApnTablePath() {
        return "apn.zip";
    }
}
