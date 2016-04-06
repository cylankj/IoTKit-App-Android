package com.cylan.publicApi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

/**
 * wifi工具类
 * Created by Tim on 2015/4/14.
 */
public class WifiUtils {
    public static final int IS_OPENING = 1, IS_CLOSING = 2, IS_OPENED = 3, IS_CLOSED = 4;
    public static final int JFG_WIFI_OPEN = 0, JFG_WIFI_WEP = 1, JFG_WIFI_WPA = 2, JFG_WIFI_WPA2 = 3;

    public static WifiManager getWifiManager(Context context) {
        return (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }


    /**
     * 打开Wifi网卡，能打开就返回true，无法打开返回false
     */
    public static void openWifi(Context context) {
        getWifiManager(context).setWifiEnabled(true);
    }

    /**
     * 关闭Wifi网卡，能关闭返回true，不能关就返回false
     */
    public static void closeWifi(Context context) {
        getWifiManager(context).setWifiEnabled(false);
    }

    /**
     * 检查当前Wifi网卡状态，返回四种状态，如果出错返回-1
     */
    public static int getWifitate(Context context) {
        int result;
        switch (getWifiManager(context).getWifiState()) {
            case 0:
                result = IS_CLOSING;
                break;
            case 1:
                result = IS_CLOSED;
                break;
            case 2:
                result = IS_OPENING;
                break;
            case 3:
                result = IS_OPENED;
                break;
            default:
                result = -1;
                break;
        }
        return result;
    }


    /**
     * 得到附近wifi的扫描结果，是ScanResult对象
     * 得到的是附近网络的结果集，没有就返回null
     */
    public static ArrayList<ScanResult> getScanResult(Context context) {
        WifiManager wm = getWifiManager(context);
        if (!wm.isWifiEnabled()) {
            wm.setWifiEnabled(true);
            SystemClock.sleep(1000);
        }
        wm.startScan();    // 开始扫描网络
        SystemClock.sleep(1000);
        return (ArrayList<ScanResult>) wm.getScanResults();
    }


    /**
     * 得到指定网络的index（从0开始计数），找不到就返回-1
     */
    public static int getTagWifiId(Context con, String netName) {
        // 开始扫描网络
        ArrayList<ScanResult> list = getScanResult(con);
        if (list != null) {
            ScanResult sr;
            for (int i = 0; i < list.size(); i++) {
                sr = list.get(i);
                if (sr.SSID.equals(netName)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 断开当前连接的网络
     */
    public static void disconnectWifi(Context con) {
        int netId = getNetworkId(con);
        WifiManager wm = getWifiManager(con);
        wm.disableNetwork(netId);
        wm.disconnect();
    }


    /**
     * 生成一个wifi配置
     */
    public static WifiConfiguration CreateWifiConfig(String SSID, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        String hex = Function.b2hex(SSID.getBytes());
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


    /**
     * 连接到WIFI
     */
    public static boolean connect2Wifi(Context context, WifiConfiguration config) {
        openWifi(context);
        WifiManager wm = getWifiManager(context);
        wm.saveConfiguration();
        config.networkId = wm.addNetwork(config);
        wm.disconnect();
        wm.enableNetwork(config.networkId, true);
        return wm.reconnect();
    }


    /**
     * 检查当前网络状态
     * 如果有wifi链接，返回true，如果没有就返回false
     */
    public static boolean getWifiConnectState(Context con) {
        return getWifiInfo(con) != null;
    }

    /**
     * @return 当前网络的名字，如果没有就返回null，否则返回string
     */
    public static String getSSID(Context con) {
        String str;
        try {
            str = getWifiInfo(con).getSSID().replace("\"", "");
        } catch (Exception e) {
            str = "";
        }
        return str;
    }

    /**
     * 得到连接的ID，如果没有就返回0，否则返回正确的id
     */
    public static int getNetworkId(Context con) {
        int i;
        try {
            i = getWifiInfo(con).getNetworkId();
        } catch (Exception e) {
            i = -1;
        }
        return i;
    }

    /**
     * 得到IP地址，出错时返回0
     */
    public static int getIPAddress(Context con) {
        try {
            return getWifiInfo(con).getIpAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 得到MAC地址
     *
     * @return 出錯了返回 ""
     */
    public static String getMacAddress(Context con) {
        String str;
        try {
            str = getWifiInfo(con).getMacAddress();
        } catch (Exception e) {
            str = "";
        }
        return str;
    }

    /**
     * 得到接入点的BSSID
     *
     * @return 出錯返回null
     */
    public static String getBSSID(Context con) {
        String bssid;
        try {
            bssid = getWifiInfo(con).getBSSID();
        } catch (Exception e) {
            bssid = "";
        }
        return bssid;

    }

    /**
     * 得到WifiInfo的所有信息包
     *
     * @return 出错了返回null
     */
    public static WifiInfo getWifiInfo(Context con) {
        return getWifiManager(con).getConnectionInfo();
    }


    // 得到配置好的网络
    public static List<WifiConfiguration> getConfiguration(Context con) {
        return getWifiManager(con).getConfiguredNetworks();

    }


    /**
     * @param wifi_SSID
     * @return 没有连接到返回false，正在连接则返回true
     */
    public static boolean connectConfiguratedWifi(Context con, String wifi_SSID) {
        WifiManager wm = getWifiManager(con);
        //如果当前网络不是想要链接的网络，要连接的网络是配置过的，并且要连接的网络能够被扫描到
        if (!getSSID(con).contains(wifi_SSID)) {
            int id = getWifiConfigurated(con, wifi_SSID);
            if (id != -1 && getTagWifiId(con, wifi_SSID) != -1) {
                wm.enableNetwork(id, true);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断要连接的wifi名是否已经配置过了
     *
     * @return 返回要连接的wifi的ID，如果找不到就返回-1
     */
    public static int getWifiConfigurated(Context c, String wifi_SSID) {
        List<WifiConfiguration> list = getConfiguration(c);
        if (list != null) {
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j).SSID.equals("\"" + wifi_SSID + "\"")) {
                    //如果要连接的wifi在已经配置好的列表中，那就设置允许链接，并且得到id
                    return list.get(j).networkId;
                }
            }
        }
        return -1;
    }


    /**
     * 清空WIFI配置
     *
     * @param ctx context
     */
    public static void cleanWifi(Context ctx) {
        WifiManager wm = getWifiManager(ctx);
        List<WifiConfiguration> list = wm.getConfiguredNetworks();
        if (list == null || list.size() == 0) return;
        for (WifiConfiguration exist : list) {
            if (exist == null) continue;
            wm.removeNetwork(exist.networkId);
        }
        wm.saveConfiguration();
    }

}