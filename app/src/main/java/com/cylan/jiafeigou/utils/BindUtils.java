package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.udpMsgPack.JfgUdpMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.cylan.jiafeigou.misc.JError.ErrorCIDNotExist;

/**
 * Created by cylan-hunt on 16-8-24.
 */
public class BindUtils {

    public static final int BIND_PREPARED = -1;
    public static final int BIND_SUC = 0;//成功
    public static final int BIND_TIME_OUT = 1;//超时
    public static final int BIND_ING = 2;//绑定中
    public static final int BIND_NEED_REBIND = 3;//需要强绑
    public static final int BIND_FAILED = 4;//失败
    public static final int BIND_NULL = ErrorCIDNotExist;//没有cid
    private static final String INVALID_SSID_0 = "<unknown ssid>";
    private static final String INVALID_SSID_1 = "0x";


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
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;

    /**
     * cid不全是数字.
     * 提取后6位
     *
     * @param string
     * @return
     */
    public static String filterCylanDeviceShortCid(String string) {
        if (TextUtils.isEmpty(string) || string.length() < 6) {
            AppLogger.e("bad cid: " + string);
            return "";
        }
        string = string.replace("\"", "");
        return string.substring(string.length() - 6, string.length());
    }

    public static String getDigitsString(String content) {
        return content.replaceAll("\\D+", "");
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

    public static UdpConstant.UdpDevicePortrait assemble(JfgUdpMsg.PingAck pingAck, JfgUdpMsg.FPingAck fPingAck) {
        UdpConstant.UdpDevicePortrait devicePortrait = new UdpConstant.UdpDevicePortrait();
        devicePortrait.uuid = pingAck.cid;
        devicePortrait.mac = fPingAck.mac;
        devicePortrait.version = fPingAck.version;
        devicePortrait.net = pingAck.net;
        return devicePortrait;
    }

    /**
     * Compares two version strings.
     * <p/>
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2.
     * The result is a positive integer if str1 is _numerically_ greater than str2.
     * The result is zero if the strings are _numerically_ equal.
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     */
    public static int versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        return Integer.signum(vals1.length - vals2.length);
    }

    //setDevice by hunt 2016-08-05
    public static boolean isUcos(String cid) {
        //
        if (!TextUtils.isEmpty(cid) && cid.length() == 12 && cid.startsWith("6001"))
            return false;

        return !TextUtils.isEmpty(cid) && cid.length() == 12
                && (cid.startsWith("20")
                || cid.startsWith("21")
                || cid.startsWith("60")
                || cid.startsWith("61"));
    }

    public static Intent getIntentByPid(int pid, Context context) {
        Intent intent = new Intent();
        if (JFGRules.isConsumerCam(pid)) {
            intent.putExtra(JConstant.KEY_ANIM_GIF, R.raw.bind_reset_rs);
            intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.bind_guide);
            intent.putExtra(JConstant.KEY_SSID_PREFIX, "DOG-******");
            intent.putExtra(JConstant.KEY_BIND_DEVICE, context.getString(R.string.Consumer_Camera));
            intent.putExtra(JConstant.KEY_ANIM_TITLE, context.getString(R.string.RuiShi_Guide));
            intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, context.getString(R.string.Tap1_AddDevice_CameraTips));
            intent.putExtra(JConstant.KEY_NEXT_STEP, context.getString(R.string.BLINKING));
        } else if (JFGRules.isCloudCam(pid)) {
            intent.putExtra(JConstant.KEY_ANIM_GIF, R.raw.cloud_cam_android);
            intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.bind_guide);
            intent.putExtra(JConstant.KEY_SSID_PREFIX, "DOG-******");
            intent.putExtra(JConstant.KEY_BIND_DEVICE, context.getString(R.string.Cloud_Camera));
            intent.putExtra(JConstant.KEY_ANIM_TITLE, context.getString(R.string.Tap1_AddDevice_CloudcameraTitle));
            intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, context.getString(R.string.Tap1_AddDevice_CloudcameraTips));
            intent.putExtra(JConstant.KEY_NEXT_STEP, context.getString(R.string.DOOR_BLUE_BLINKING));
        } else if (JFGRules.isPanoramaCamera(pid)) {
            intent.putExtra(JConstant.KEY_ANIM_TITLE, context.getString(R.string.Tap1_AddDevice_CameraTipShort));
            intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, context.getString(R.string.Tap1_AddDevice_CameraTips));
            intent.putExtra(JConstant.KEY_NEXT_STEP, context.getString(R.string.BLINKING));
        } else if (JFGRules.isCamera(pid)) {
            intent.putExtra(JConstant.KEY_SSID_PREFIX, "DOG-******");
            intent.putExtra(JConstant.KEY_ANIM_TITLE, context.getString(R.string.Tap1_AddDevice_CameraTipsTitle));
            intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, context.getString(R.string.Tap1_AddDevice_CameraTips));
            intent.putExtra(JConstant.KEY_NEXT_STEP, context.getString(R.string.BLINKING));
        } else if (JFGRules.isCatEeyBell(pid)) {
            intent.putExtra(JConstant.KEY_ANIM_GIF, R.raw.eyes_android);
            intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.bind_guide);
            intent.putExtra(JConstant.KEY_SSID_PREFIX, "DOG-******");
            intent.putExtra(JConstant.KEY_BIND_DEVICE, context.getString(R.string.Smart_Door_Viewer));
            intent.putExtra(JConstant.KEY_ANIM_TITLE, context.getString(R.string.Tap1_AddDevice_DoorbellTipsTitle));
            intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, context.getString(R.string.Tap1_AddDevice_CameraTips));
            intent.putExtra(JConstant.KEY_NEXT_STEP, context.getString(R.string.BLINKING));
        } else if (JFGRules.isNoPowerBell(pid)) {
            intent.putExtra(JConstant.KEY_ANIM_GIF, R.raw.door_android);
            intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.bind_bell);
            intent.putExtra(JConstant.KEY_SSID_PREFIX, "BELL-******");
            intent.putExtra(JConstant.KEY_BIND_DEVICE, context.getString(R.string.Smart_bell_Power));
            intent.putExtra(JConstant.KEY_ANIM_TITLE, context.getString(R.string.Tap1_AddDevice_CloudcameraTitle));
            intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, context.getString(R.string.Tap1_AddDevice_CloudcameraTips));
            intent.putExtra(JConstant.KEY_NEXT_STEP, context.getString(R.string.DOOR_BLUE_BLINKING));
        } else if (JFGRules.isBell(pid)) {
            intent.putExtra(JConstant.KEY_ANIM_GIF, R.raw.door_android);
            intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.bind_guide);
            intent.putExtra(JConstant.KEY_SSID_PREFIX, "DOG-******");
            intent.putExtra(JConstant.KEY_BIND_DEVICE, context.getString(R.string.Smart_bell_Battery));
            intent.putExtra(JConstant.KEY_ANIM_TITLE, context.getString(R.string.Tap1_AddDevice_DoorbellTipsTitle));
            intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, context.getString(R.string.Tap1_AddDevice_DoorbellTips));
            intent.putExtra(JConstant.KEY_NEXT_STEP, context.getString(R.string.DOOR_BLUE_BLINKING));
        }
        return intent;
    }

}