package com.cylan.jiafeigou.n.mvp.impl.mag;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.telephony.TelephonyManager;

import com.cylan.jiafeigou.n.mvp.contract.mag.MagLiveInformationContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/9/19
 * 描述：
 */
public class MagLiveInformationPresenterImp extends AbstractPresenter<MagLiveInformationContract.View> implements MagLiveInformationContract.Presenter {

    public static final int SD_NORMAL = 0;
    public static final int SD_UNINSTALL = 1;
    public static final int SD_FAIL_RW = 2;


    public MagLiveInformationPresenterImp(MagLiveInformationContract.View view) {
        super(view);
        view.setPresenter(this);
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
}
