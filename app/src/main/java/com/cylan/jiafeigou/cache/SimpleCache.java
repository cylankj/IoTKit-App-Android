package com.cylan.jiafeigou.cache;

import android.net.wifi.ScanResult;

import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by cylan-hunt on 16-7-9.
 */
public class SimpleCache {

    private static SimpleCache instance;

    private SimpleCache() {
    }

    public static SimpleCache getInstance() {
        if (instance == null)
            instance = new SimpleCache();
        return instance;
    }

    public WeakReference<List<ScanResult>> getWeakScanResult() {
        return weakScanResult;
    }

    public WeakReference<List<TimeZoneBean>> timeZoneBeenList;

    public void setWeakScanResult(List<ScanResult> weakScanResult) {
        if (weakScanResult == null)
            return;
        this.weakScanResult = new WeakReference<>(weakScanResult);
    }

    private WeakReference<List<ScanResult>> weakScanResult;
}
