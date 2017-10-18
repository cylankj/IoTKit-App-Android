package com.cylan.jiafeigou.misc;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cylan-hunt on 16-7-9.
 */
public class ScanResultListFilter {

    /**
     * filter the  bad one
     *
     * @param list
     * @return
     */
    public static ArrayList<ScanResult> extractPretty(List<ScanResult> list) {
        ArrayList<ScanResult> results = new ArrayList<>();
        if (list == null) {
            return results;
        }
        for (ScanResult result : list) {
            if (TextUtils.isEmpty(result.SSID)
                    || TextUtils.equals(result.SSID, "<unknown ssid>")
                    || TextUtils.equals(result.SSID, "0x")) {
                continue;
            }
            results.add(result);
        }
        return results;
    }

    public static ArrayList<ScanResult> extractPretty(List<ScanResult> list, boolean withDog) {
        if (withDog) {
            return extractPretty(list);
        }
        ArrayList<ScanResult> results = new ArrayList<>();
        if (list == null) {
            return results;
        }
        for (ScanResult result : list) {
            final String ssid = result.SSID.replace("\"", "");//不能过滤 blank
            if (JFGRules.isCylanDevice(ssid)) {
                continue;
            }
            if (TextUtils.isEmpty(result.SSID)
                    || TextUtils.equals(result.SSID, "<unknown ssid>")
                    || TextUtils.equals(result.SSID, "0x")) {
                continue;
            }
            if (result.frequency > 4900 && result.frequency < 5900) {
                continue;
            }
            results.add(result);
            int level = WifiManager.calculateSignalLevel(result.level, 5);
            Log.d("TABLES", "TABLES:" + ssid + " " + level);
        }
        return results;

    }

    public static List<ScanResult> extractJFG(List<ScanResult> resultList, String... filters) {
        if (filters == null) {
            return resultList;
        }
        List<ScanResult> scanResultList = new ArrayList<>();
        if (resultList == null) {
            return scanResultList;
        }
        List<String> filterList = Arrays.asList(filters);
        for (ScanResult result : resultList) {
            if (filterList.contains(result.SSID)) {
                scanResultList.add(result);
            }
        }
        return scanResultList;
    }

}
