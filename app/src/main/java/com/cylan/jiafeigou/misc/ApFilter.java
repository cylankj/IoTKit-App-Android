package com.cylan.jiafeigou.misc;

import android.text.TextUtils;

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
}
