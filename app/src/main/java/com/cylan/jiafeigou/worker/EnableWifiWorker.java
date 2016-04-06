package com.cylan.jiafeigou.worker;

import android.net.wifi.WifiManager;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-11-20
 * Time: 13:58
 */

public class EnableWifiWorker implements Runnable {

    private int mNetId;
    private WifiManager mWm;

    public EnableWifiWorker(WifiManager wm, int netId) {
        this.mWm = wm;
        this.mNetId = netId;
    }

    @Override
    public void run() {
        mWm.enableNetwork(mNetId, true);
    }
}
