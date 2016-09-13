package com.cylan.jiafeigou.n.mvp.model;

import android.net.wifi.ScanResult;

/**
 * Created by cylan-hunt on 16-9-13.
 */
public class BeanWifiList {
    public ScanResult result;
    public boolean checked;

    public BeanWifiList(ScanResult result) {
        this.result = result;
        this.checked = true;
    }

    public BeanWifiList() {
    }
}
