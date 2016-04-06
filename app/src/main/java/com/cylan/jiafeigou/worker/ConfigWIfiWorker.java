package com.cylan.jiafeigou.worker;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;

import cylan.log.DswLog;
import com.cylan.publicApi.Function;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-11-10
 * Time: 17:02
 */

public class ConfigWIfiWorker implements Runnable {

    private static final String TAG = "ConfigWIfiWorker";
    private int mWhat;
    private Handler mHandler;
    private ScanResult mScanResult;
    private WifiManager mWifiManager;

    public ConfigWIfiWorker(WifiManager wm, ScanResult result, Handler handler, int what) {
        this.mWifiManager = wm;
        this.mScanResult = result;
        this.mHandler = handler;
        this.mWhat = what;
    }

    @Override
    public void run() {
        boolean isEnable = false;
        for (int i = 0; i < 3; i++) {
            isEnable = Function.configWifi(mWifiManager, getWIfiConfig(mScanResult));
            DswLog.i(TAG+ "\t第" + i + "次连接 enable：" + isEnable);
            if (isEnable) {
                break;
            }
        }
        mHandler.obtainMessage(mWhat, isEnable).sendToTarget();
    }

    private WifiConfiguration getWIfiConfig(ScanResult sr) {
        if (sr.capabilities.contains("WPA") || sr.capabilities.contains("WEP")) {
            return Function.CreateWifiInfo(sr.SSID.replaceAll("\"", ""), "11111111", Function.JFG_WIFI_WPA);
        } else {
            return Function.CreateWifiInfo(sr.SSID.replaceAll("\"", ""), "", Function.JFG_WIFI_OPEN);
        }
    }

}
