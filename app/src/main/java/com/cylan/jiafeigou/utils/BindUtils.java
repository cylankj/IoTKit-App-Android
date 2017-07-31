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
import com.cylan.jiafeigou.n.view.activity.BindAnimationActivity;
import com.cylan.jiafeigou.n.view.activity.BindCamActivity;
import com.cylan.jiafeigou.n.view.activity.BindPanoramaCamActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.udpMsgPack.JfgUdpMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.cylan.jiafeigou.misc.JError.ErrorCIDNotExist;
import static com.cylan.jiafeigou.misc.bind.UdpConstant.BIND_TAG;

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


    public static final String TAG_UDP_FLOW = BIND_TAG + "send_udp_msg";
    public static final String TAG_NET_RECOVERY_FLOW = BIND_TAG + "net_recovery";
    public static final String TAG_NET_LOGIN_FLOW = BIND_TAG + "login";
    public static final String TAG_NET_FINAL_FLOW = BIND_TAG + "final";

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
        if (TextUtils.isEmpty(str1)) str1 = "0.0";
        if (TextUtils.isEmpty(str2)) str2 = "0.0";
        Version a = new Version(str1);
        Version b = new Version(str2);
        return a.compareTo(b);
//        if (str1 == null) str1 = "0";
//        if (str2 == null) str2 = "0";
//        Pattern pattern = Pattern.compile("\\d+");
//        List<String> str1Result = new ArrayList<>();
//        List<String> str2Result = new ArrayList<>();
//        Matcher matcher = pattern.matcher(str1);
//        while (matcher.find()) {
//            str1Result.add(matcher.group());
//        }
//        matcher = pattern.matcher(str2);
//        while (matcher.find()) {
//            str2Result.add(matcher.group());
//        }
//        if (str1Result.size() == 0) return -1;
//        for (int i = 0; i < str1Result.size(); i++) {
//            if (str2Result.size() < i || str2Result.size() == 0) return 1;
//            int version1 = Integer.parseInt(str1Result.get(i));
//            int version2 = Integer.parseInt(str2Result.get(i));
//            if (version1 == version2) continue;
//            return Integer.signum(version1 - version2);
//        }
//        return 0;
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
            intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.dog_doby);
            intent.putExtra(JConstant.KEY_SSID_PREFIX, DOG_AP);
            intent.putExtra(JConstant.KEY_BIND_DEVICE, context.getString(R.string.Consumer_Camera));
            intent.putExtra(JConstant.KEY_ANIM_TITLE, context.getString(R.string.RuiShi_Guide));
            intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, context.getString(R.string.Tap1_AddDevice_CameraTips));
            intent.putExtra(JConstant.KEY_NEXT_STEP, context.getString(R.string.BLINKING));
            intent.setClass(context, BindAnimationActivity.class);
        } else if (JFGRules.isCloudCam(pid)) {
            intent.putExtra(JConstant.KEY_ANIM_GIF, R.raw.cloud_cam_android);
            intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.dog_doby);
            intent.putExtra(JConstant.KEY_SSID_PREFIX, DOG_AP);
            intent.putExtra(JConstant.KEY_BIND_DEVICE, context.getString(R.string.Cloud_Camera));
            intent.putExtra(JConstant.KEY_ANIM_TITLE, context.getString(R.string.Tap1_AddDevice_CloudcameraTitle));
            intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, context.getString(R.string.Tap1_AddDevice_CloudcameraTips));
            intent.putExtra(JConstant.KEY_NEXT_STEP, context.getString(R.string.DOOR_BLUE_BLINKING));
            intent.setClass(context, BindAnimationActivity.class);
        } else if (JFGRules.isPanoramaCamera(pid)) {
            intent.putExtra(JConstant.KEY_ANIM_TITLE, context.getString(R.string.Tap1_AddDevice_CameraTipShort));
            intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, context.getString(R.string.Tap1_AddDevice_CameraTips));
            intent.putExtra(JConstant.KEY_NEXT_STEP, context.getString(R.string.BLINKING));
            intent.setClass(context, BindPanoramaCamActivity.class);
        } else if (JFGRules.isCamera(pid)) {
            intent.putExtra(JConstant.KEY_SSID_PREFIX, DOG_AP);
            intent.putExtra(JConstant.KEY_ANIM_TITLE, context.getString(R.string.Tap1_AddDevice_CameraTipsTitle));
            intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, context.getString(R.string.Tap1_AddDevice_CameraTips));
            intent.putExtra(JConstant.KEY_NEXT_STEP, context.getString(R.string.BLINKING));
            intent.setClass(context, BindCamActivity.class);
        } else if (JFGRules.isCatEeyBell(pid)) {
            intent.putExtra(JConstant.KEY_ANIM_GIF, R.raw.eyes_android);
            intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.dog_doby);
            intent.putExtra(JConstant.KEY_SSID_PREFIX, BELL_AP);
            intent.putExtra(JConstant.KEY_BIND_DEVICE, context.getString(R.string.Smart_Door_Viewer));
            intent.putExtra(JConstant.KEY_ANIM_TITLE, context.getString(R.string.Tap1_AddDevice_DoorbellTipsTitle));
            intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, context.getString(R.string.Tap1_AddDevice_CameraTips));
            intent.putExtra(JConstant.KEY_NEXT_STEP, context.getString(R.string.BLINKING));
            intent.setClass(context, BindAnimationActivity.class);
        } else if (JFGRules.isNoPowerBell(pid)) {
            intent.putExtra(JConstant.KEY_ANIM_GIF, R.raw.door_android);
            intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.bell_doby);
            intent.putExtra(JConstant.KEY_SSID_PREFIX, DOG_AP);
            intent.putExtra(JConstant.KEY_BIND_DEVICE, context.getString(R.string.Smart_bell_Power));
            intent.putExtra(JConstant.KEY_ANIM_TITLE, context.getString(R.string.Tap1_AddDevice_CloudcameraTitle));
            intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, context.getString(R.string.Tap1_AddDevice_CloudcameraTips));
            intent.putExtra(JConstant.KEY_NEXT_STEP, context.getString(R.string.DOOR_BLUE_BLINKING));
            intent.setClass(context, BindAnimationActivity.class);
        } else if (JFGRules.isBell(pid)) {
            intent.putExtra(JConstant.KEY_ANIM_GIF, R.raw.door_android);
            intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.dog_doby);
            intent.putExtra(JConstant.KEY_SSID_PREFIX, BELL_AP);
            intent.putExtra(JConstant.KEY_BIND_DEVICE, context.getString(R.string.Smart_bell_Battery));
            intent.putExtra(JConstant.KEY_ANIM_TITLE, context.getString(R.string.Tap1_AddDevice_DoorbellTipsTitle));
            intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, context.getString(R.string.Tap1_AddDevice_DoorbellTips));
            intent.putExtra(JConstant.KEY_NEXT_STEP, context.getString(R.string.DOOR_BLUE_BLINKING));
            intent.setClass(context, BindAnimationActivity.class);
        }
        return intent;
    }

    public static final String BELL_AP = "BELL-**-******";
    public static final String DOG_AP = "DOG-**-******";

    private static class Version implements Comparable<Version> {

        private String version;

        public final String get() {
            return this.version;
        }

        public Version(String version) {
            if (version == null)
                throw new IllegalArgumentException("Version can not be null");
            if (!version.matches("[0-9]+(\\.[0-9]+)*"))
                throw new IllegalArgumentException("Invalid version format");
            this.version = version;
        }

        @Override
        public int compareTo(Version that) {
            if (that == null)
                return 1;
            String[] thisParts = this.get().split("\\.");
            String[] thatParts = that.get().split("\\.");
            int length = Math.max(thisParts.length, thatParts.length);
            for (int i = 0; i < length; i++) {
                int thisPart = i < thisParts.length ?
                        Integer.parseInt(thisParts[i]) : 0;
                int thatPart = i < thatParts.length ?
                        Integer.parseInt(thatParts[i]) : 0;
                if (thisPart < thatPart)
                    return -1;
                if (thisPart > thatPart)
                    return 1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object that) {
            if (this == that)
                return true;
            if (that == null)
                return false;
            if (this.getClass() != that.getClass())
                return false;
            return this.compareTo((Version) that) == 0;
        }

    }
}