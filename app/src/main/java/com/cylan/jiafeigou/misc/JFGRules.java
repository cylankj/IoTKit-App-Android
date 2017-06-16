package com.cylan.jiafeigou.misc;

import android.content.Context;
import android.text.TextUtils;

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

    public static boolean showItem(int pid, String key) {
        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid,
                key);
    }

    public static boolean isCylanDevice(String ssid) {
        return ApFilter.accept(ssid);
    }

    public static String getDigitsFromString(String string) {
        if (TextUtils.isEmpty(string))
            return "";
        return string.replaceAll("\\D+", "");
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

    /**
     * 软AP
     *
     * @param pid
     * @return
     */
    public static boolean showSoftAp(int pid) {
        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid, "AP");
    }

    public static boolean isPanoramicCam(int pid) {
        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid,
                "ViewAngle");
    }

    public static boolean showNTSCVLayout(int pid) {
        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid,
                "ntsc");
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


    public static boolean isFreeCam(Device jfgDevice) {
        return jfgDevice != null && jfgDevice.pid == JConstant.OS_CAMERA_CC3200;
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


    public static boolean isRS(int pid) {
        return pid == 38;
    }

    public static boolean isCamera(int pid) {
        final String value = BaseApplication.getAppComponent().getProductProperty().getValue(pid);
        return !TextUtils.isEmpty(value) && value.contains("cam");
    }

    public static boolean isBell(int pid) {
        final String value = BaseApplication.getAppComponent().getProductProperty().property(pid,
                "value");
        return !TextUtils.isEmpty(value) && value.contains("bell");
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

    public static boolean isPan720(int pid) {
        return pid == 1089 || pid == 21;
    }


    public static boolean showSight(int pid) {
        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid,
                "sight");
    }

    public static boolean showRotate(int pid) {
        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid,
                "hangup");
    }

    public static boolean showStandbyItem(int pid) {
        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid,
                "standby");
    }


    public static boolean showSdHd(int pid) {
        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid,
                "SD/HD");
    }

    public static boolean showBattery(int pid) {
        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid,
                "battery");
    }

    //freeCam 海思 wifi
    public static boolean showMobileNet(int pid) {
        return is3GCam(pid);
    }

    public static boolean showLedIndicator(int pid) {
        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid,
                "led");
    }

    public static boolean showIp(int pid) {
        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid,
                "ip");
    }

    public static boolean showWiredMode(int pid) {
        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid,
                "wired");
    }

    public static boolean showEnableAp(int pid) {
        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid,
                "enableAP");
    }

    public static boolean showFirmware(int pid) {
        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid,
                "fu");
    }

    public static boolean showSoftWare(int pid) {
        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid,
                "softVersion");
    }

    public static boolean showTimeZone(int pid) {
        return !JFGRules.isPanoramicCam(pid);
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
        boolean normal = !isPanoramicCam(pid);
        return normal ? 0.75f : 1.0f;
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
