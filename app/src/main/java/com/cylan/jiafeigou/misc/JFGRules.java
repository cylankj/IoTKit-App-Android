package com.cylan.jiafeigou.misc;

import android.content.Context;
import android.text.TextUtils;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.JFGDPDevice;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.util.Locale;

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

    public static String getDeviceAlias(JFGDPDevice device) {
        if (device == null) return "";
        String alias = device.alias;
        if (!TextUtils.isEmpty(alias))
            return alias;
        return device.uuid;
    }

    public static final int LANGUAGE_TYPE_SIMPLE_CHINESE = 0;
    public static final int LANGUAGE_TYPE_ENGLISH = 1;
    public static final int LANGUAGE_TYPE_RU = 2;
    public static final int LANGUAGE_TYPE_POR = 3;
    public static final int LANGUAGE_TYPE_SPANISH = 4;
    public static final int LANGUAGE_TYPE_JAPAN = 5;
    public static final int LANGUAGE_TYPE_FRENCH = 6;
    public static final int LANGUAGE_TYPE_GERMANY = 7;
    public static final int LANGUAGE_TYPE_ITALIAN = 8;
    public static final int LANGUAGE_TYPE_TURKISH = 9;
    public static final int LANGUAGE_TYPE_TRA_CHINESE = 10;

    private static final Locale[] CONST_LOCALE = {
            Locale.SIMPLIFIED_CHINESE,
            Locale.ENGLISH,
            new Locale("ru", "RU"),
            new Locale("pt", "BR"),
            new Locale("es", "ES"),
            Locale.JAPAN,
            Locale.FRANCE,
            Locale.GERMANY,
            Locale.ITALY,
            new Locale("tr", "TR"),
            Locale.TRADITIONAL_CHINESE};

    private static final Locale LOCALE_HK = new Locale("zh", "HK");

    public static int getLanguageType(Context ctx) {
        Locale locale = ctx.getResources().getConfiguration().locale;
        if (locale.equals(LOCALE_HK))
            return LANGUAGE_TYPE_TRA_CHINESE;
        final int count = CONST_LOCALE.length;

        if (locale.getLanguage().equals("zh")) {
            if (locale.getCountry().equals("CN"))
                return LANGUAGE_TYPE_SIMPLE_CHINESE;
            return LANGUAGE_TYPE_TRA_CHINESE;
        }
        for (int i = 0; i < count; i++) {
            if (locale.equals(CONST_LOCALE[i]))
                return i;
        }
        return LANGUAGE_TYPE_ENGLISH;
    }

    public static boolean isWifiCam(int pid) {
        return pid == JConstant.OS_CAMERA_UCOS ||
                pid == JConstant.OS_CAMERA_UCOS_V2 ||
                pid == JConstant.OS_CAMERA_UCOS_V3;
    }

    public static boolean isPanoramicCam(int pid) {
        return pid == JConstant.OS_CAMERA_PANORAMA_QIAOAN ||
                pid == JConstant.OS_CAMERA_PANORAMA_HAISI ||
                pid == JConstant.OS_CAMERA_PANORAMA_GUOKE;
    }

    public static boolean showBatteryItem(String uuid) {
        JFGDPDevice device = DataSourceManager.getInstance().getJFGDevice(uuid);
        return device != null && (is3GCam(device.pid) || isFreeCam(device.pid));
    }

    public static boolean isMobileNet(int net) {
        return net >= 3;
    }

    public static boolean is3GCam(int pid) {
        return pid == JConstant.OS_CAMERA_ANDROID_3_0;
    }

    public static boolean isFreeCam(int pid) {
        return pid == JConstant.OS_CAMERA_CC3200;
    }

    public static boolean isFreeCam(JFGDPDevice jfgDevice) {
        return jfgDevice != null && jfgDevice.pid == JConstant.OS_CAMERA_CC3200;
    }

    public static boolean showLedIndicator(int pid) {
        return pid == JConstant.OS_CAMERA_ANDROID_3_0 ||
                pid == JConstant.OS_CAMERA_ANDROID ||
                pid == JConstant.OS_CAMERA_PANORAMA_HAISI ||
                pid == JConstant.OS_CAMERA_PANORAMA_QIAOAN ||
                pid == JConstant.OS_CAMERA_PANORAMA_GUOKE;
    }


    /**
     * 显示延时摄影
     *
     * @param pid
     * @return
     */
    public static boolean showDelayRecordBtn(int pid) {
        return pid == JConstant.OS_CAMERA_ANDROID_3_0;
    }

    //freeCam 海思 wifi
    public static boolean showMobileLayout(int pid) {
        switch (pid) {
            case JConstant.OS_CAMERA_UCOS:
            case JConstant.OS_CAMERA_UCOS_V2:
            case JConstant.OS_CAMERA_UCOS_V3:
            case JConstant.OS_CAMERA_CC3200:
            case JConstant.OS_CAMERA_PANORAMA_HAISI:
            case JConstant.OS_CAMERA_PANORAMA_QIAOAN:
            case JConstant.OS_CAMERA_PANORAMA_GUOKE:
                return false;
            default:
                return true;
        }
    }

    public static boolean isCamera(int pid) {
        switch (pid) {
            case JConstant.OS_CAMERA_ANDROID:
            case JConstant.OS_CAMERA_UCOS:
            case JConstant.OS_CAMERA_UCOS_V2:
            case JConstant.OS_CAMERA_UCOS_V3:
            case JConstant.OS_CAMERA_ANDROID_4G:
            case JConstant.OS_CAMERA_CC3200:
            case JConstant.OS_CAMERA_PANORAMA_HAISI:
            case JConstant.OS_CAMERA_PANORAMA_QIAOAN:
            case JConstant.OS_CAMERA_PANORAMA_GUOKE:
            case JConstant.OS_CAMERA_ANDROID_3_0:
            case JConstant.OS_CAMERA_FXXX_LESHI:
            case JConstant.OS_CAMERA_FXXX_LESHI_PID:
                return true;
            default:
                return false;
        }
    }

    public static boolean isBell(int pid) {
        switch (pid) {
            case JConstant.OS_DOOR_BELL:
            case JConstant.OS_DOOR_BELL_V2:
                return true;
        }
        return false;
    }

    /**
     * 判断是否全景
     *
     * @param pid
     * @return
     */
    public static boolean isNeedPanoramicView(int pid) {
        switch (pid) {
            case JConstant.OS_CAMERA_PANORAMA_HAISI:
            case JConstant.OS_CAMERA_PANORAMA_QIAOAN:
            case JConstant.OS_CAMERA_PANORAMA_GUOKE:
                return true;
        }
        return false;
    }

    public static boolean is2WCam(int pid) {
        return pid == JConstant.OS_CAMERA_PANORAMA_HAISI;
    }

    public static class PlayErr {

        public static final int ERR_UNKOWN = -2;
        public static final int ERR_STOP = -1;
        /**
         * 网络
         */
        public static final int ERR_NERWORK = 0;
        /**
         * 没有流量
         */
        public static final int ERR_NOT_FLOW = 1;

        /**
         * 帧率太低
         */
        public static final int ERR_LOW_FRAME_RATE = 2;

        /**
         * 设备离线了
         */
        public static final int ERR_DEVICE_OFFLINE = 3;
        public static final int STOP_MAUNALLY = -3;

    }

    public static boolean isDeviceOnline(DpMsgDefine.DPNet net) {
        return net != null && net.net != 0;
    }

    public static boolean isShareDevice(String uuid) {
        if (TextUtils.isEmpty(uuid)) return false;
        JFGDPDevice device = DataSourceManager.getInstance().getJFGDevice(uuid);
        return device != null && !TextUtils.isEmpty(device.shareAccount);
    }

    public static boolean isShareDevice(JFGDPDevice device) {
        if (device == null) return false;
        return !TextUtils.isEmpty(device.shareAccount);
    }

    /**
     * 基于安全考虑
     * com.cylan.jiafeigou.xx
     *
     * @return xx 可以为空:表示官方包名
     */
    public static String getTrimPackageName() {
        final String packageName = ContextUtils.getContext().getPackageName();
        try {
            return packageName.substring(19, packageName.length()).replace(".", "");
        } catch (Exception e) {
            return "";
        }
    }

    public static void main(String[] args) {
        String t = "com.cylan.jiafeigou.xx";
        System.out.println(t.substring(19, t.length()));

    }
}
