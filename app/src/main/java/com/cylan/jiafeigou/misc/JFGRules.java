package com.cylan.jiafeigou.misc;

import android.text.TextUtils;

import com.cylan.jiafeigou.utils.TimeUtils;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class JFGRules {

    public static final int NETSTE_SCROLL_COUNT = 4;

    //    public static final int LOGIN = 1;
//    public static final int LOGOUT = 0;
    public static final int RULE_DAY_TIME = 0;
    public static final int RULE_NIGHT_TIME = 1;
    private static final long TIME_6000 = 6 * 60 * 60L;
    private static final long TIME_1800 = 18 * 60 * 60L;

    //6:00 am - 17:59 pm
    //18:00 pm-5:59 am

    /**
     * @return 0白天 1黑夜
     */
    public static int getTimeRule() {
        final long time = (System.currentTimeMillis()
                - TimeUtils.getTodayStartTime()) / 1000L;
        return time >= TIME_1800 || time < TIME_6000
                ? RULE_NIGHT_TIME : RULE_DAY_TIME;
    }

    public static boolean isCylanDevice(String ssid) {
        if (!TextUtils.isEmpty(ssid)) {
            return JConstant.JFG_DOG_DEVICE_REG.matcher(ssid.replace("\"", "")).find()
                    || JConstant.JFG_BELL_DEVICE_REG.matcher(ssid.replace("\"", "")).find();
        }
        return false;
    }

    public static String getDigitsFromString(String string) {
        if (TextUtils.isEmpty(string))
            return "";
        return string.replaceAll("\\D+", "");
    }
}
