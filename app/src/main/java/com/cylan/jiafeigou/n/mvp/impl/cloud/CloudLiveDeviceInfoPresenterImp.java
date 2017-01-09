package com.cylan.jiafeigou.n.mvp.impl.cloud;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Pair;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveDeviceInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.BeanCloudInfo;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudLiveDeviceInfoPresenterImp extends AbstractPresenter<CloudLiveDeviceInfoContract.View> implements CloudLiveDeviceInfoContract.Presenter {

    public static final int SD_NORMAL = 0;
    public static final int SD_UNINSTALL = 1;
    public static final int SD_FAIL_RW = 2;

    private String uuid;

    public CloudLiveDeviceInfoPresenterImp(CloudLiveDeviceInfoContract.View view, String uuid) {
        super(view);
        this.uuid = uuid;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public int checkSdCard() {
        int sdCardState = SD_NORMAL;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            sdCardState = SD_NORMAL;
        } else if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED) ||
                Environment.getExternalStorageState().equals(Environment.MEDIA_BAD_REMOVAL)) {
            sdCardState = SD_UNINSTALL;
        } else {
            sdCardState = SD_FAIL_RW;
        }
        return sdCardState;
    }

    @Override
    public String getMobileType() {
        String type = "中国移动";
        TelephonyManager iPhoneManager = (TelephonyManager) getView().getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String iNumeric = iPhoneManager.getSimOperator();
        if (iNumeric.length() > 0) {
            if (iNumeric.equals("46000") || iNumeric.equals("46002")) {
                type = "中国移动";
            } else if (iNumeric.equals("46001")) {
                type = "中国联通";
            } else if (iNumeric.equals("46003")) {
                type = "中国电信";
            }
        } else {
            type = "未插入SIM卡";
        }
        return type;
    }

    @Override
    public String getWifiState() {
        String result = "未开启";
        WifiManager wifiManager = (WifiManager) getView().getContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            int wifiState = wifiManager.getWifiState();

            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    result = "未开启";
                    break;

                case WifiManager.WIFI_STATE_ENABLED:
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    result = wifiInfo.getSSID();
                    break;
            }
        } else {
            return result;
        }
        return result;
    }

    @Override
    public void saveCloudInfoBean(Object value, int id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    AppLogger.i("save start: " + id + " " + value);
                    BaseValue baseValue = new BaseValue();
                    baseValue.setId(id);
                    baseValue.setVersion(System.currentTimeMillis());
                    baseValue.setValue(o);
                    GlobalDataProxy.getInstance().update(uuid, baseValue, true);
                    AppLogger.i("save end: " + id + " " + value);
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }

}
