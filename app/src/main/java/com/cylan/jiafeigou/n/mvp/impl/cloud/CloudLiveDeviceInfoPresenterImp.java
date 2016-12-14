package com.cylan.jiafeigou.n.mvp.impl.cloud;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Pair;

import com.cylan.ex.JfgException;
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

    private BeanCloudInfo beanCloudInfo;

    public CloudLiveDeviceInfoPresenterImp(CloudLiveDeviceInfoContract.View view, BeanCloudInfo bean) {
        super(view);
        this.beanCloudInfo = bean;
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
    public void saveCloudInfoBean(BeanCloudInfo info, int id) {
        this.beanCloudInfo = info;
        Observable.just(new Pair<>(info, id))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Pair<BeanCloudInfo, Integer>>() {
                    @Override
                    public void call(Pair<BeanCloudInfo, Integer> beanCloudInfoIntegerPair) {
                        int id = beanCloudInfoIntegerPair.second;
                        RxEvent.JFGAttributeUpdate update = new RxEvent.JFGAttributeUpdate();
                        update.uuid = beanCloudInfo.deviceBase.uuid;
                        if (id == DpMsgMap.ID_2000003_BASE_ALIAS)
                            update.o = beanCloudInfoIntegerPair.first.deviceBase.alias;
                        else update.o = beanCloudInfoIntegerPair.first.getObject(id);
                        update.msgId = id;
                        update.version = System.currentTimeMillis();
                        RxBus.getCacheInstance().post(update);
                        if (id == DpMsgMap.ID_2000003_BASE_ALIAS) {
                            try {
                                JfgCmdInsurance.getCmd().setAliasByCid(beanCloudInfo.deviceBase.uuid,
                                        beanCloudInfo.deviceBase.alias);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                            AppLogger.i("update alias: " + new Gson().toJson(beanCloudInfo));
                            return;
                        }
                        try {
                            JfgCmdInsurance.getCmd().robotSetData(beanCloudInfo.deviceBase.uuid,
                                    DpUtils.getList(id,
                                            beanCloudInfoIntegerPair.first.getByte(id)
                                            , System.currentTimeMillis()));
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                        AppLogger.i("update camInfo: " + new Gson().toJson(beanCloudInfo));
                    }
                });
    }

    /**
     * 获取的到设备信息
     * @return
     */
    @Override
    public BeanCloudInfo getCloudInfoBean() {
        if (beanCloudInfo != null){
            return beanCloudInfo;
        }else {
            return new BeanCloudInfo();
        }
    }
}
