package com.cylan.jiafeigou.misc;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseIntArray;

import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.util.Locale;
import java.util.TimeZone;

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
        return ApFilter.accept(ssid);
    }

    public static String getDigitsFromString(String string) {
        if (TextUtils.isEmpty(string))
            return "";
        return string.replaceAll("\\D+", "");
    }

    public static boolean showSight(int pid) {
        return pid == 10 || pid == 18 || pid == 36 || pid == 5 || pid == 1091 || pid == 1092;
    }

    public static String getDeviceAlias(Device device) {
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

    public static boolean showIpAddress(int pid) {
        return pid == 38 || pid == 39;
    }

    /**
     * 有限模式
     *
     * @param pid
     * @return
     */
    public static boolean showWiredMode(int pid) {
//        return pid == 38 || pid == 39;暂时不做有线的.
        return false;
    }

    /**
     * 软AP
     *
     * @param pid
     * @return
     */
    public static boolean showSoftAp(int pid) {
        return pid == 38 || pid == 39;
    }

    public static boolean isWifiCam(int pid) {
        return pid == JConstant.OS_CAMERA_UCOS ||
                pid == JConstant.OS_CAMERA_UCOS_V2 ||
                pid == JConstant.PID_CAMERA_WIFI_G1 ||
                pid == JConstant.OS_CAMERA_UCOS_V3;
    }

    public static boolean isPanoramicCam(int pid) {
        return pid == JConstant.OS_CAMERA_PANORAMA_QIAOAN ||
                pid == JConstant.OS_CAMERA_PANORAMA_HAISI ||
                pid == JConstant.PID_CAMERA_PANORAMA_HAISI_960 ||
                pid == JConstant.PID_CAMERA_PANORAMA_HAISI_1080 ||
                pid == JConstant.OS_CAMERA_PANORAMA_GUOKE ||
                pid == 36 ||
                pid == 38;
    }

    public static boolean show110VLayout(int pid) {
        return isPanoramicCam(pid) || isWifiCam(pid) ||
                isRS(pid) ||
                pid == 21 || pid == 1089;
    }

    public static boolean showHomeBatteryIcon(int pid) {
        return isFreeCam(pid) || is3GCam(pid) || isBell(pid);
    }

    public static boolean showSettingBatteryItem(int pid) {
        if (isRS(pid)) return false;//睿思,不显示电量.
        return is3GCam(pid) || isFreeCam(pid)
                || pid == 1089
                || pid == 21
                || pid == 1088
                || pid == 26
                || pid == 1093
                || pid == 6
                || pid == 1094
                || pid == 25
                || pid == 11
                || pid == 17
                || pid == 1158
                || pid == 1160
                || pid == 27;

    }

    public static boolean isMobileNet(int net) {
        return net >= 2;
    }

    public static boolean is3GCam(int pid) {
        return pid == JConstant.PID_CAMERA_ANDROID_3_0
                || pid == JConstant.OS_CAMERA_ANDROID;
    }

    public static boolean isFreeCam(int pid) {
        return pid == JConstant.OS_CAMERA_CC3200;
    }

    public static boolean showStandbyItem(int pid) {
        if (isRS(pid)) return true;
        return pid == 4
                || pid == 5
                || pid == 7
                || pid == 10
                || pid == 18
                || pid == 26
                || pid == 1152
                || pid == 1090
                || pid == 1071
                || pid == 1092
                || pid == 1091
                || pid == 1088;
    }

    public static boolean isFreeCam(Device jfgDevice) {
        return jfgDevice != null && jfgDevice.pid == JConstant.OS_CAMERA_CC3200;
    }

    public static boolean showLedIndicator(int pid) {
        if (isRS(pid)) return true;
        return pid == 4
                || pid == 5
                || pid == 7
                || pid == 10
                || pid == 18
                || pid == 1090
                || pid == 1091
                || pid == 1092
                || pid == 1071
                || pid == 1152;
    }


    /**
     * 显示延时摄影
     *
     * @param pid
     * @return
     */
    public static boolean showDelayRecordBtn(int pid) {
        return false;
    }

    //freeCam 海思 wifi
    public static boolean showMobileLayout(int pid) {
        return is3GCam(pid);
    }

    public static boolean isRS(int pid) {
        return pid == 38;
    }

    public static boolean isCamera(int pid) {
        if (isRS(pid)) return true;
        switch (pid) {
            case 4:
            case 5:
            case 7:
            case 10:
            case 18:
            case 26:
            case 17:
            case 20:
            case 23:
            case 19:
            case 1152:
            case 1158:
            case 1088:
            case 1091:
            case 1092:
            case 1071:
            case 1090:
//            case 21:
            case 36:
            case 37:
                return true;
            default:
                return false;
        }
    }

    public static boolean isBell(int pid) {
        switch (pid) {
            case 6:
            case 25:
            case 1093:
            case 1094:
            case 1158:
            case 15:
            case 1159:
            case 22://金鑫智慧科技智能猫眼
            case 24://普顺达门铃
            case 1160:
            case 27://乐视猫眼
                return true;
        }
        return false;
    }

    public static boolean isVRCam(int pid) {
        switch (pid) {
            case 21:
            case 1089:
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
        return isPanoramicCam(pid);
    }

    public static boolean needShowFirmware(int pid) {
        switch (pid) {
            case 7:
            case 5:
            case 4:
            case 21:
            case 26:
            case 6:
            case 25:
            case 1089:
            case 1088:
            case 1093:
            case 1094:
            case 1090:
            case 1071:
            case 17:
            case 1152:
            case 1158:
            case 1160:
                return true;
        }
        return false;
    }

    public static boolean showSoftWare(int pid) {
        switch (pid) {
            case 1092:
            case 1091:
            case 10:
            case 18:
            case 36:
            case 37:
            case 38:
            case 39:
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
        public static final int ERR_NETWORK = 0;
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
        return net != null && net.net > 0 && !TextUtils.isEmpty(net.ssid);
    }

    public static boolean hasSdcard(DpMsgDefine.DPSdStatus sdStatus) {
        return sdStatus != null && sdStatus.err == 0 && sdStatus.hasSdcard;
    }

    public static boolean isShareDevice(String uuid) {
        if (TextUtils.isEmpty(uuid)) return false;
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        return device != null && !TextUtils.isEmpty(device.shareAccount);
    }

    public static boolean isShareDevice(Device device) {
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
        System.out.println(1 & 255);

    }

    public static float getDefaultPortHeightRatio(int pid) {
        if (isWifiCam(pid)) return 0.75f;
        if (isPanoramicCam(pid)) return 1.0f;
        return 0.75f;
    }

    public static SparseIntArray VALID_PID = new SparseIntArray();

    static {
        VALID_PID.put(1090, 1090);
        VALID_PID.put(1071, 1071);
        VALID_PID.put(1092, 1092);
        VALID_PID.put(1091, 1091);
        VALID_PID.put(1089, 1089);
        VALID_PID.put(1088, 1088);
        VALID_PID.put(1093, 1093);
        VALID_PID.put(1094, 1094);
        VALID_PID.put(1152, 1152);
        VALID_PID.put(1158, 1158);
        VALID_PID.put(1159, 1159);
        VALID_PID.put(1160, 1160);

        VALID_PID.put(4, 4);
        VALID_PID.put(5, 5);
        VALID_PID.put(6, 6);
        VALID_PID.put(7, 7);
        VALID_PID.put(8, 8);
        VALID_PID.put(10, 10);
        VALID_PID.put(11, 11);
        VALID_PID.put(15, 15);
        VALID_PID.put(17, 17);
        VALID_PID.put(18, 18);
        VALID_PID.put(19, 19);
        VALID_PID.put(20, 20);
        VALID_PID.put(21, 21);
        VALID_PID.put(22, 22);
        VALID_PID.put(23, 23);
        VALID_PID.put(24, 24);
        VALID_PID.put(25, 25);
        VALID_PID.put(26, 26);
        VALID_PID.put(27, 27);
        VALID_PID.put(36, 36);
        VALID_PID.put(37, 37);
        VALID_PID.put(38, 38);
    }

    public static TimeZone getDeviceTimezone(Device device) {
        if (device == null) return TimeZone.getDefault();
        DpMsgDefine.DPTimeZone timeZone = device.$(214, new DpMsgDefine.DPTimeZone());
        return TimeZone.getTimeZone(getGMTFormat(timeZone.offset * 1000));
    }

    private static String getGMTFormat(int rawOffset) {
        int hour = Math.abs(rawOffset / 1000 / 60 / 60);
        int minute = Math.abs(rawOffset) - Math.abs(hour) * 1000 * 60 * 60 > 0 ? 30 : 0;
        String factor = rawOffset > 0 ? "+" : "-";
        return String.format(Locale.getDefault(), "GMT%s%02d:%02d", factor, hour, minute);
    }

    public static boolean isAPDirect(String uuid, String mac) {
        if (!TextUtils.isEmpty(uuid) && !TextUtils.isEmpty(mac)) {
            //做一个缓存,这个putString是内存操作,可以再UI现在直接调用
            PreferencesUtils.putString(JConstant.KEY_DEVICE_MAC + uuid, mac);
        }
        if (TextUtils.isEmpty(mac))
            mac = PreferencesUtils.getString(JConstant.KEY_DEVICE_MAC + uuid);
        return MiscUtils.isAPDirect(mac);
    }
}
