package com.cylan.jiafeigou.utils;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.udpMsgPack.JfgUdpMsg;

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

    private static final int SECURITY_NONE = 0;
    private static final int SECURITY_WEP = 1;
    private static final int SECURITY_PSK = 2;
    private static final int SECURITY_EAP = 3;

    /**
     * cid不全是数字.
     * 提取后6位
     *
     * @param string
     * @return
     */
    public static String filterCylanDeviceShortCid(String string) {
        if (TextUtils.isEmpty(string))
            return "";
        return string.replace("DOG-", "").replace("ML-", "").replace("\"", "");
//        return string.replaceAll("\\D+", "");
    }

    public static String getDigitsString(String content) {
        return content.replaceAll("\\D+", "");
    }

    public static boolean invalidInfo(WifiConfiguration wifiConfiguration) {
        return wifiConfiguration != null && wifiConfiguration.SSID != null &&
                (wifiConfiguration.SSID.contains(INVALID_SSID_0)
                        || wifiConfiguration.SSID.contains(INVALID_SSID_1));
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
}