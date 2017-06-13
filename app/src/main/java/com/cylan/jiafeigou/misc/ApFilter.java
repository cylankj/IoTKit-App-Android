package com.cylan.jiafeigou.misc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.regex.Pattern;

/**
 * Created by hds on 17-5-3.
 */

public class ApFilter {
    public static final Pattern JFG_DOG_DEVICE_REG = Pattern.compile("DOG-[a-zA-Z0-9]{6}");
    public static final Pattern JFG_BELL_DEVICE_REG = Pattern.compile("DOG-ML-[a-zA-Z0-9]{6}");
    public static final Pattern JFG_PAN_DEVICE_REG = Pattern.compile("DOG-5W-[a-zA-Z0-9]{6}");
    public static final Pattern JFG_GENERAL_DEVICE = Pattern.compile("DOG-[a-zA-Z0-9]{2}-[a-zA-Z0-9]{6}");
    public static final Pattern RS_GENERAL_DEVICE = Pattern.compile("RS-CAM-[a-zA-Z0-9]{6}");

    public static boolean accept(String ssid) {
        if (!TextUtils.isEmpty(ssid)) {
            return JFG_DOG_DEVICE_REG.matcher(ssid.replace("\"", "")).find()
                    || JFG_BELL_DEVICE_REG.matcher(ssid.replace("\"", "")).find()
                    || JFG_PAN_DEVICE_REG.matcher(ssid.replace("\"", "")).find()
                    || RS_GENERAL_DEVICE.matcher(ssid.replace("\"", "")).find()
                    || JFG_GENERAL_DEVICE.matcher(ssid.replace("\"", "")).find();
        }
        return false;

    }

    public static boolean isAPMode(String ssid, String uuid) {
        String rssid = ssid.replace("\"", "");
        return accept(ssid) && !TextUtils.isEmpty(ssid) && !TextUtils.isEmpty(uuid) && TextUtils.equals(rssid.substring(rssid.length() - 6), uuid.substring(uuid.length() - 6));
    }

    public static boolean isAPMode(String uuid) {
        ConnectivityManager manager = (ConnectivityManager) ContextUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiManager service = (WifiManager) ContextUtils.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = service.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            if (!TextUtils.isEmpty(ssid)) {
                ssid = ssid.replace("\"", "");
                return accept(ssid) && !TextUtils.isEmpty(ssid) && !TextUtils.isEmpty(uuid) && TextUtils.equals(ssid.substring(ssid.length() - 6), uuid.substring(uuid.length() - 6));
            }
        }
        return false;
    }

    public static boolean isApNet() {
        ConnectivityManager manager = (ConnectivityManager) ContextUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiManager service = (WifiManager) ContextUtils.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = service.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            if (!TextUtils.isEmpty(ssid)) {
                ssid = ssid.replace("\"", "");
                return accept(ssid) && !TextUtils.isEmpty(ssid);
            }
        }
        return false;
    }
}
