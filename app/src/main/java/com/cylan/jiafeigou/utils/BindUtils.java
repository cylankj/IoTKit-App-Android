package com.cylan.jiafeigou.utils;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.utils.NetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by cylan-hunt on 16-8-24.
 */
public class BindUtils {
    private static final String INVALID_SSID_0 = "<unknown ssid>";
    private static final String INVALID_SSID_1 = "0x";

    public static final Pattern DOG_REG = Pattern.compile("DOG-\\d{6}");
    public static final Pattern DOG_ML_REG = Pattern.compile("DOG-ML-\\d{6}");

    public static List<ScanResult> transformDogList(List<ScanResult> resultList) {
        if (resultList == null || resultList.size() == 0)
            return new ArrayList<>();//return an empty list is better than null
        List<ScanResult> results = new ArrayList<>();
        for (ScanResult result : resultList) {
            if (DOG_REG.matcher(removeDoubleQuotes(result.SSID)).find()) {
                results.add(result);
            }
        }
        return results;
    }

    public static List<ScanResult> transformDogList(List<ScanResult> resultList, Pattern pattern) {
        if (resultList == null || resultList.size() == 0)
            return new ArrayList<>();//return an empty list is better than null
        List<ScanResult> results = new ArrayList<>();
        for (ScanResult result : resultList) {
            if (pattern == null) {
                if (TextUtils.equals(INVALID_SSID_0, result.SSID)
                        || TextUtils.equals(INVALID_SSID_1, result.SSID))
                    continue;
            }
            if (pattern != null
                    && pattern.matcher(removeDoubleQuotes(result.SSID)).find()) {
                results.add(result);
            }
        }
        return results;
    }

    public static List<ScanResult> transformBellList(List<ScanResult> resultList) {
        if (resultList == null || resultList.size() == 0)
            return new ArrayList<>();//return an empty list is better than null
        List<ScanResult> results = new ArrayList<>();
        for (ScanResult result : resultList) {
            if (DOG_ML_REG.matcher(removeDoubleQuotes(result.SSID)).find()) {
                results.add(result);
            }
        }
        return results;
    }


    public static String removeDoubleQuotes(String string) {
        if (TextUtils.isEmpty(string)) return "";
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    public static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    public static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP = 1;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_EAP = 3;

    public static String getDigitsString(String string) {
        if (TextUtils.isEmpty(string))
            return "";
        return string.replaceAll("\\D+", "");
    }


    public static boolean invalidInfo(WifiConfiguration wifiConfiguration) {
        return wifiConfiguration != null && wifiConfiguration.SSID != null &&
                (wifiConfiguration.SSID.contains(INVALID_SSID_0)
                        || wifiConfiguration.SSID.contains(INVALID_SSID_1));
    }

//    public static void finalRecoverWifi(AContext binderHandler, WifiManager wifiManager) {
//        if (binderHandler != null) {
//            Object o = binderHandler.getCache(AContext.KEY_DISABLED_WIFI_CONFIGS);
//            if (o != null && o instanceof AContext.DisabledWifiConfig) {
//                List<WifiConfiguration> wifiConfigurations = ((AContext.DisabledWifiConfig) o).list;
//                if (wifiConfigurations == null) {
//                    DswLog.d("finalRecoverWifi list is null");
//                    return;
//                }
//                DswLog.d("finalRecoverWifi list is not null:" + wifiConfigurations);
//                reEnableTheCacheConfiguration(wifiManager, wifiConfigurations);
//            } else {
//                DswLog.d("finalRecoverWifi disable cached is null");
//            }
//        } else {
//            List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
//            reEnableTheCacheConfiguration(wifiManager, wifiConfigurations);
//        }
//    }

    private static void reEnableTheCacheConfiguration(WifiManager wifiManager,
                                                      List<WifiConfiguration> wifiConfigurations) {
        if (wifiConfigurations != null) {
            for (WifiConfiguration con : wifiConfigurations) {
                if (BindUtils.invalidInfo(con))
                    continue;
                if (NetUtils.removeDoubleQuotes(con.SSID).startsWith("DOG-")) {
                    wifiManager.removeNetwork(con.networkId);
                    continue;
                }
//                wifiManager.enableNetwork(con.networkId, false);
//                DswLog.d("finalRecoverWifi try: " + con.status + " " + con.SSID);
            }
        }
    }
}
