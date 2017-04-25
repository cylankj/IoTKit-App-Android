package com.cylan.jiafeigou.utils;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 网络工具类
 * <p/>
 * Created by Tim on 2015/4/24.
 */
public class NetUtils {
    private static final int NETWORK_TYPE_UNAVAILABLE = -1;
    private static final int NETWORK_CLASS_UNAVAILABLE = -1;
    /**
     * Unknown network class.
     */
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    /**
     * Class of broadly defined "2G" networks.
     */
    public static final int NETWORK_CLASS_2_G = 2;
    /**
     * Class of broadly defined "3G" networks.
     */
    public static final int NETWORK_CLASS_3_G = 3;
    /**
     * Class of broadly defined "4G" networks.
     */
    public static final int NETWORK_CLASS_4_G = 4;

    /**
     * 取得当前网络类型
     *
     * @param c c
     * @return 网络类型
     */
    public static int getNetType(Context c) {
        ConnectivityManager cm = getConnectivityManager(c);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            return info.getType();
        }
        return -1;
    }

    /**
     * 返回jfg定义的网络类型。
     *
     * @return
     */
    public static int getJfgNetType(Context c) {
        switch (getNetType(c)) {
            case ConnectivityManager.TYPE_WIFI:
                return 1;
            case ConnectivityManager.TYPE_MOBILE:
                return 2;
            default:
                return 0;
        }
    }


    /**
     * 获取网络名，没有网络为 offLine，
     * wifi , ssid;
     * 中国境内的卡，显示三大运营商。
     * 其他的为 网络名。
     *
     * @param c
     * @return
     */
    public static String getNetName(Context c) {
        int netType = getNetType(c);
        String netName = "";
        if (netType == -1) {
            netName = "offLine";
        } else if (netType == ConnectivityManager.TYPE_WIFI) {
            netName = WifiUtils.getSSID(c);//ssid
        } else {
            String operator = getTelephonyManager(c).getSimOperator();
            if ("46000".equals(operator) || "46002".equals(operator)
                    || "46007".equals(operator)) {
                return "中国移动";
            } else if ("46001".equals(operator)) {
                return "中国联通";
            } else if ("46003".equals(operator)) {
                return "中国电信";
            } else {
                netName = getTelephonyManager(c).getNetworkOperatorName();
            }
        }
        return netName;
    }


    /**
     * 打开或者关闭移动网络数据
     *
     * @param isEnable 开关
     * @param context  c
     */
    public static void setMobileDataEnabled(boolean isEnable, Context context) {
        final ConnectivityManager cm = getConnectivityManager(context);
        Method setMobileDataEnabled = null;
        try {
            setMobileDataEnabled = cm.getClass().getMethod(
                    "setMobileDataEnabled", boolean.class);
            setMobileDataEnabled.invoke(cm, isEnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取MAC地址
     *
     * @param context
     * @return
     */
    public static String getMacAddress(Context context) {
        String mac;
        WifiManager wm = getWifiManager(context);
        try {
            mac = wm.getConnectionInfo().getMacAddress();
        } catch (Exception e) {
            mac = "";
        }
        return mac;
    }

    public static ConnectivityManager getConnectivityManager(Context context) {
        return (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
    }


    public static WifiManager getWifiManager(Context c) {
        return (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
    }


    /**
     * 手机卡运营商名称
     */
    public static String getSimName(Context c) {
        return getTelephonyManager(c).getSimOperatorName();
    }

    /**
     * 检查sim卡状态
     *
     * @param ctx
     * @return
     */
    public static boolean checkimCard(Context ctx) {
        return getTelephonyManager(ctx).getSimState() == TelephonyManager.SIM_STATE_READY;
    }


    /**
     * 检查sim卡状态
     *
     * @param ctx
     * @return 没有卡返回和其他状态返回0.1为正常，2为pin锁，3为puk锁。
     */
    public static int getSimCardState(Context ctx) {
        switch (getTelephonyManager(ctx).getSimState()) {
            case TelephonyManager.SIM_STATE_READY:
                return 1;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                return 2;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                return 3;
            default:
                return 0;
        }
    }

    /***
     * 获取手机管理器
     *
     * @param ctx
     * @return
     */
    public static TelephonyManager getTelephonyManager(Context ctx) {
        return (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * 返回网络制式 （2G，3G，4G）
     *
     * @param networkType
     * @return
     */
    public static int getNetworkClassByType(int networkType) {
        switch (networkType) {
            case NETWORK_TYPE_UNAVAILABLE:
                return NETWORK_CLASS_UNAVAILABLE;
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_CLASS_2_G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NETWORK_CLASS_3_G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NETWORK_CLASS_4_G;
            default:
                return NETWORK_CLASS_UNKNOWN;
        }
    }

    public static int getMobileNetWorkType(Context ctx) {
        return getNetworkClassByType(getTelephonyManager(ctx).getNetworkType());
    }

    /**
     * 除3G，4G外都返回慢速网络。
     *
     * @param ctx
     * @return
     */
    public static boolean isFastMobileData(Context ctx) {
        switch (getMobileNetWorkType(ctx)) {
            case NETWORK_CLASS_3_G:
            case NETWORK_CLASS_4_G:
                return true;
            case NETWORK_TYPE_UNAVAILABLE:
            case NETWORK_CLASS_UNKNOWN:
            case NETWORK_CLASS_2_G:
            default:
                return false;
        }
    }

    public static int pingNetwork(String ip) {
        return pingNetwork(ip, 1);
    }

    /**
     * must assign internet permission
     *
     * @param ip
     * @param pingNum
     * @return
     */
    public static int pingNetwork(String ip, int pingNum) {
        try {
            Process p = Runtime.getRuntime().exec("ping -c " + pingNum + " " + ip);
            return p.waitFor();//status 只能获取是否成功，无法获取更多的信息
        } catch (Exception e) {
            return 1;
        }
    }

    public static String sPingNetwork(String ip) {
        try {
            Process p = Runtime.getRuntime().exec("ping -c 2 " + ip);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String result = "";
            String res;
            while ((res = stdInput.readLine()) != null) {
                if (res.contains(ip))
                    continue;
                result += res;
            }
            p.destroy();
            stdInput.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isInternetAvailable(String host) {
        try {
            URL url = new URL(host);
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestProperty("User-Agent", "test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1000); // mTimeout is in seconds
            urlc.connect();
            return (urlc.getResponseCode() == 200);
        } catch (IOException e) {
            Log.e("warning", "Error checking internet connection", e);
            return false;
        }

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
        if (result == null)
            return SECURITY_UNKNOWN;
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    public static final int SECURITY_UNKNOWN = -1;
    public static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP = 1;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_EAP = 3;

    public static String getRouterMacAddress(Application context) {
        WifiManager mWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (mWifi.isWifiEnabled()) {
            WifiInfo wifiInfo = mWifi.getConnectionInfo();
            String netMac = wifiInfo.getBSSID(); //获取被连接网络的mac地址
            return netMac == null ? "" : netMac.toUpperCase();
        }
        return "";
    }

    /**
     * 检测网络是否连接
     *
     * @return
     */

    private boolean isNetworkAvailable(Application context) {
        //得到网络连接信息
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //去进行判断网络是否连接
        if (manager.getActiveNetworkInfo() != null) {
            return manager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }


    public static final boolean ping() {
        try {
            String ip = OptionsImpl.getServer();
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
            // ping的状态
            return p.waitFor() == 0;
        } catch (Exception e) {
            AppLogger.d("获取真实网络连接状态出错:" + e.getMessage());
        }
        return false;
    }

    public static final boolean isWiFiConnected(Context context) {
        ConnectivityManager manager = getConnectivityManager(context);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }
}
