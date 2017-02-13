package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * 该类用于操作wifi的连接，配置等
 */
public class Function {
    public static final int JFG_WIFI_OPEN = 0;
    public static final int JFG_WIFI_WEP = 1;
    public static final int JFG_WIFI_WPA = 2;
    public static final int JFG_WIFI_WPA2 = 3;


    public static String getIPByDns(String dns) {
        try {
            return InetAddress.getByName(dns).getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static WifiConfiguration configWifi(WifiManager wm, String ssid, String pwd, int type) {
        if (!wm.isWifiEnabled()) {
            wm.setWifiEnabled(true);
        }
        // return configWifi(wm, initWifiConfiguration(ssid, pwd));
        WifiConfiguration config = CreateWifiInfo(ssid, pwd, type);
        configWifi(wm, config);
        return config;
    }

    public static WifiConfiguration initWifiConfiguration(String ssid, String pwd) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = "\"" + ssid + "\"";
        configuration.preSharedKey = "\"" + pwd + "\"";
        configuration.hiddenSSID = true;
        configuration.status = WifiConfiguration.Status.ENABLED;
        return configuration;
    }

    public static boolean configWifi(WifiManager wm, WifiConfiguration configuration) {
        boolean isExist = isWifiConfigExist(wm, configuration);
        configuration.networkId = wm.addNetwork(configuration);
        wm.saveConfiguration();
        wm.disconnect();
        boolean ret = wm.enableNetwork(configuration.networkId, true);
        boolean connect = wm.reconnect();
        Log.i("wifi confing ", ret + "--ssid-->" + configuration.SSID + "--netID-->"
                + configuration.networkId + "--isExist-->" + isExist + "--isSuccess-->" + connect);
        return ret;
    }

    public static String getVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static WifiConfiguration CreateWifiInfo(String SSID, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        String hex = b2hex(SSID.getBytes());
        WifiSsid ssid = WifiSsid.createFromHex(hex);
        config.SSID = "\"" + ssid.toString() + "\"";
        if (type == JFG_WIFI_OPEN) {
            config.hiddenSSID = true;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        } else if (type == JFG_WIFI_WEP) {
            config.hiddenSSID = true;
            int length = password.length();
            // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
            if ((length == 10 || length == 26 || length == 58) && password.matches("[0-9A-Fa-f]*")) {
                config.wepKeys[0] = password;
            } else {
                config.wepKeys[0] = '"' + password + '"';
            }
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.wepTxKeyIndex = 0;
        } else if (type == JFG_WIFI_WPA || type == JFG_WIFI_WPA2) {
            if (password.matches("[0-9A-Fa-f]{64}")) {
                config.preSharedKey = password;
            } else {
                config.preSharedKey = '"' + password + '"';
            }
            config.hiddenSSID = true;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        }
        return config;
    }

    public static String b2hex(final byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; ++i) {
            ret += String.format("%02x", b[i]);
        }
        return ret;
    }

    public static boolean isWifiConfigExist(final WifiManager wm, WifiConfiguration config) {
        boolean isExist = false;
        String ssid = b2hex(config.SSID.getBytes());
        List<WifiConfiguration> list = wm.getConfiguredNetworks();
        if (list == null) return false;
        for (WifiConfiguration exist : list) {
            if (exist == null || exist.SSID == null) continue;
            String tmp;
            tmp = b2hex(exist.SSID.getBytes());
            if (ssid.equals(tmp)) {
                isExist = true;
                boolean ret = wm.removeNetwork(exist.networkId);
                wm.saveConfiguration();
            }
        }
        return isExist;
    }

    public static void cleanWifi(final WifiManager wm) {
        List<WifiConfiguration> list = wm.getConfiguredNetworks();
        for (WifiConfiguration exist : list) {
            if (exist == null) continue;
            wm.removeNetwork(exist.networkId);
        }
        wm.saveConfiguration();
    }


}
