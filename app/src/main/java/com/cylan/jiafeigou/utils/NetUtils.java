package com.cylan.jiafeigou.utils;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

import static android.content.Context.CONNECTIVITY_SERVICE;

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
        if (info != null && info.isAvailable() && info.isConnected()) {
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
     * 返回jfg定义的网络类型。
     *
     * @return
     */
    public static int getJfgNetType() {
        Context c = ContextUtils.getContext();
        return getJfgNetType(c);
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
                getSystemService(CONNECTIVITY_SERVICE);
    }


    public static WifiManager getWifiManager(Context c) {
        return (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
    }

    public static WifiManager getWifiManager() {
        return (WifiManager) ContextUtils.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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

    private static final String[] HOST_RANDOM = {"http://www.qq.com",
            "https://www.ibm.com/us-en",
            "http://www.alibaba.com",
            "http://www.taobao.com",
            "http://www.sina.com.cn",
//            "http://www.weibo.com",
            "http://www.hao123.com",
            "http://www.baidu.com",
//            "http://www.bing.com"
    };

    //https://deviceatlas.com/blog/list-of-user-agent-strings
    public static boolean isInternetAvailable(String host) {
        try {
            URL url = new URL(host);
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestProperty("User-Agent", AGENTS[RandomUtils.getRandom(AGENTS.length)]);
            urlc.setConnectTimeout(5000); // mTimeout is in seconds
            urlc.setReadTimeout(5000);
            urlc.connect();
            int result = urlc.getResponseCode();
            Log.d("isInternetAvailable", "isInternetAvailable: " + result + " ," + host);
            return (urlc.getResponseCode() != 0);
        } catch (Exception e) {
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
        context = ContextUtils.getContext();
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(CONNECTIVITY_SERVICE);
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

//    /**
//     * 检测当的网络（WLAN、3G/2G）状态
//     *
//     * @return true 表示网络可用
//     */
//    public static boolean isNetworkAvailable() {
//        Context context = ContextUtils.getContext();
//        ConnectivityManager connectivity = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (connectivity != null) {
//            NetworkInfo info = connectivity.getActiveNetworkInfo();
//            if (info != null && info.isConnected()) {
//                // 当前网络是连接的
//                if (info.getState() == NetworkInfo.State.CONNECTED) {
//                    // 当前所连接的网络可用
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

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

    public static String getRouterMacAddress() {
        Context context = ContextUtils.getContext();
        WifiManager mWifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        //去进行判断网络是否连接
        if (manager.getActiveNetworkInfo() != null) {
            return manager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }


    public static final boolean ping() {
        try {
            String ip = OptionsImpl.getServer();
            Process p = Runtime.getRuntime().exec("ping -c 2 -w 100 " + ip);// ping网址3次
            // ping的状态
            return p.waitFor() == 0;
        } catch (Exception e) {
            AppLogger.d("获取真实网络连接状态出错:" + e.getMessage());
        }
        return false;
    }

    public static final boolean pingQQ() {
        try {
            final String ip = "www.qq.com";
            Process p = Runtime.getRuntime().exec("ping -c 3 " + ip);// ping网址3次
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

    public static String getReadableIp() {
        WifiManager mWifi = (WifiManager) ContextUtils.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifi.isWifiEnabled()) {
            WifiInfo wifiInfo = mWifi.getConnectionInfo();
            return intToIp(wifiInfo.getIpAddress());
        }
        return "";
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    public static boolean isNetworkAvailable() {
        try {
            String qqDomain = getDomain("qq.com");
            if (TextUtils.isEmpty(qqDomain)) {
//                String qqDomain = getDomain("qq.com");
//                return !TextUtils.isEmpty(qqDomain);
            }
            return !TextUtils.isEmpty(qqDomain);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * yahoo.com
     *
     * @param host
     * @return
     */
    public static String getDomain(String host) {
        try {

            InetAddress[] machines = InetAddress.getAllByName(host);
            StringBuilder builder = new StringBuilder();
            if (machines != null) {
                for (InetAddress address : machines) {
                    builder.append(address.getHostAddress());
                }
            }
            return (builder.toString());
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean isPublicNetwork() {
        return isInternetAvailable(HOST_RANDOM[RandomUtils.getRandom(HOST_RANDOM.length)]);
    }

    /**
     * 不一定适用 所有手机
     *
     * @return
     */
    public static boolean isVPNOn() {
        List<String> networkList = new ArrayList<>();
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isUp())
                    networkList.add(networkInterface.getName());
            }
        } catch (Exception ex) {
            Log.e("", "isVpnUsing Network List didn't received");
        }
        return networkList.contains("tun0") || networkList.contains("ppp0");
    }

    private static final String AGENTS[] = new String[]{
            "Mozilla/5.0 (Linux; Android 6.0.1; SM-G920V Build/MMB29K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.98 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 5.1.1; SM-G928X Build/LMY47X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.83 Mobile Safari/537.36",
            "Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Microsoft; Lumia 950) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Mobile Safari/537.36 Edge/13.10586",
            "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 6P Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.83 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 6.0.1; E6653 Build/32.2.A.0.253) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.98 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 6.0; HTC One M9 Build/MRA58K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.98 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 7.0; Pixel C Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.98 Safari/537.36",
            "Mozilla/5.0 (Linux; Android 6.0.1; SGP771 Build/32.2.A.0.253; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.98 Safari/537.36",
            "Mozilla/5.0 (Linux; Android 5.1.1; SHIELD Tablet Build/LMY48C) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.98 Safari/537.36",
            "Mozilla/5.0 (Linux; Android 5.0.2; SAMSUNG SM-T550 Build/LRX22G) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/3.3 Chrome/38.0.2125.102 Safari/537.36"
    };


    //三星 S6-7.0 S7-7.0(api:24)
    // note3-5.0(api:21)
    // 小米3-4.4(api:19)
    // 魅族Pro6-6.0(api:23)

    //小米6-7.1(api:25) 失败。
    public static boolean createHotSpot(WifiManager wifiManager, final String ssid, final String pwd) {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
        WifiConfiguration netConfig = new WifiConfiguration();
        netConfig.SSID = ssid;
        netConfig.preSharedKey = pwd;
        if (TextUtils.isEmpty(netConfig.preSharedKey)) {
            netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            netConfig.allowedAuthAlgorithms.clear();
            netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        } else {
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            //from system_settings_source
            netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            netConfig.allowedKeyManagement.set(4);
        }
        try {
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
//                //1.save config
//                Object listener = Class.forName("android.net.wifi.WifiManager$ActionListener");
//                Method save = wifiManager.getClass().getMethod("save", WifiConfiguration.class, listener.getClass());
//                save.invoke(wifiManager, netConfig, null);
//                //2.connect
//                Method connect = wifiManager.getClass().getMethod("connect", WifiConfiguration.class, listener.getClass());
//                connect.invoke(wifiManager, netConfig, null);
//                //3.start
//                ConnectivityManager connectivityManager = (ConnectivityManager) ContextUtils.getContext().getSystemService(CONNECTIVITY_SERVICE);
//                listener = Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
//                Method startTethering = connectivityManager.getClass().getMethod("startTethering", Integer.TYPE, Boolean.TYPE, listener.getClass());
//                startTethering.invoke(connectivityManager, 0, true, null);
//                return true;
//            }

            Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean apstatus = (Boolean) setWifiApMethod.invoke(wifiManager, netConfig, true);
            Method isWifiApEnabledmethod = wifiManager.getClass().getMethod("isWifiApEnabled");
            while (!(Boolean) isWifiApEnabledmethod.invoke(wifiManager)) {
            }
            Method getWifiApStateMethod = wifiManager.getClass().getMethod("getWifiApState");
            int apstate = (Integer) getWifiApStateMethod.invoke(wifiManager);
            Method getWifiApConfigurationMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            netConfig = (WifiConfiguration) getWifiApConfigurationMethod.invoke(wifiManager);
            Log.e("CLIENT", "\nSSID:" + netConfig.SSID + "\nPassword:" + netConfig.preSharedKey + "\n");
            return true;
        } catch (Exception e) {
            Log.e("ConfigAp", "", e);
            return false;
        }
    }

    public static boolean createHotSpot(final String ssid, final String pwd) {
        WifiManager wifiManager = (WifiManager) ContextUtils.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return createHotSpot(wifiManager, ssid, pwd);
    }


    public static class ClientScanResult {
        private String IpAddr;
        private String HWAddr;
        private String Device;
        private boolean isReachable;

        public ClientScanResult(String ipAddr, String hWAddr, String device, boolean isReachable) {
            super();
            this.IpAddr = ipAddr;
            this.HWAddr = hWAddr;
            this.Device = device;
            this.isReachable = isReachable;
        }

        public String getIpAddr() {
            return IpAddr;
        }

        public void setIpAddr(String ipAddr) {
            IpAddr = ipAddr;
        }


        public String getHWAddr() {
            return HWAddr;
        }

        public void setHWAddr(String hWAddr) {
            HWAddr = hWAddr;
        }


        public String getDevice() {
            return Device;
        }

        public void setDevice(String device) {
            Device = device;
        }


        public boolean isReachable() {
            return isReachable;
        }

        public void setReachable(boolean isReachable) {
            this.isReachable = isReachable;
        }

        @Override
        public String toString() {
            return "ClientScanResult{" +
                    "IpAddr='" + IpAddr + '\'' +
                    ", HWAddr='" + HWAddr + '\'' +
                    ", Device='" + Device + '\'' +
                    ", isReachable=" + isReachable +
                    '}';
        }
    }

    /**
     * Gets a list of the clients connected to the Hotspot
     *
     * @param onlyReachables   {@code false} if the list should contain unreachable (probably disconnected) clients, {@code true} otherwise
     * @param reachableTimeout Reachable Timout in miliseconds
     */
    public static ArrayList<ClientScanResult> getClientList(final boolean onlyReachables,
                                                            final int reachableTimeout) {
        final ArrayList<ClientScanResult> result = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if ((splitted != null) && (splitted.length >= 4)) {
                    // Basic sanity check
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        boolean isReachable = InetAddress.getByName(splitted[0]).isReachable(reachableTimeout);
                        if (!onlyReachables || isReachable) {
                            result.add(new ClientScanResult(splitted[0], splitted[3], splitted[5], isReachable));
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            Log.e("", e.toString());
            return null;
        } finally {
            CloseUtils.closeQuietly(br);
        }
    }

    /**
     * 获取连上hotsSpot
     *
     * @return
     */
    public static Observable<ArrayList<ClientScanResult>> getReachableDevs() {
        return Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(s -> NetUtils.getClientList(true, 1000));
    }
}
