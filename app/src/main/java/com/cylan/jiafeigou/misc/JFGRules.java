package com.cylan.jiafeigou.misc;

import android.content.Context;
import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.BaseDeviceInformationFetcher;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.DeviceInformation;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.misc.pty.IProperty;
import com.cylan.jiafeigou.misc.pty.PropertiesLoader;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;

import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

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

//    public static boolean showItem(int pid, String key) {
//        return BaseApplication.getAppComponent().getProductProperty().hasProperty(pid,
//                key);
//    }

    public static boolean isCylanDevice(String ssid) {
        return ApFilter.accept(ssid);
    }

    public static String getDigitsFromString(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        return string.replaceAll("\\D+", "");
    }

    public static String getDeviceAlias(Device device) {
        if (device == null) {
            return "";
        }
        String alias = device.alias;
        if (!TextUtils.isEmpty(alias)) {
            return alias;
        }
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
    public static final int LANGUAGE_TYPE_VI = 11;
    public static final int LANGUAGE_TYPE_IN_ = 12;

    public static final Locale[] CONST_LOCALE = {
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
            Locale.TRADITIONAL_CHINESE,
            new Locale("vi", "VN"),
            new Locale("in", "ID"),
    };

    private static final Locale LOCALE_HK = new Locale("zh", "HK");

    public static int getLanguageType() {
        return getLanguageType(ContextUtils.getContext());
    }

    public static int getLanguageType(Context ctx) {
        Locale locale = ctx.getResources().getConfiguration().locale;
        if (locale.equals(LOCALE_HK)) {
            return LANGUAGE_TYPE_TRA_CHINESE;
        }
        final int count = CONST_LOCALE.length;

        if (locale.getLanguage().equals("zh")) {
            if (locale.getCountry().equals("CN")) {
                return LANGUAGE_TYPE_SIMPLE_CHINESE;
            }
            return LANGUAGE_TYPE_TRA_CHINESE;
        }
        for (int i = 0; i < count; i++) {
            if (locale.equals(CONST_LOCALE[i])) {
                return i;
            }
        }
        return LANGUAGE_TYPE_ENGLISH;
    }

    /**
     * 软AP
     *
     * @param pid
     * @param share
     * @return
     */
    public static boolean showSoftAp(int pid, boolean share) {
        return PropertiesLoader.getInstance().hasProperty(pid, "AP", share);
    }

    public static boolean isPanoramaCamera(int pid) {
        final String p = PropertiesLoader.getInstance().property(pid,
                "DEVICE");
        return p != null && p.contains("DOG-5W");
    }

    public static boolean showNTSCVLayout(int pid, boolean share) {
        return PropertiesLoader.getInstance().hasProperty(pid, "ntsc", share);
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
        final String value = PropertiesLoader.getInstance().property(pid, "value");
        return !TextUtils.isEmpty(value) && value.contains("RS");//不是下划线,直接去掉
    }

    /**
     * @param pid
     * @return
     */
    public static boolean isCamera(int pid) {
        return PropertiesLoader.getInstance().isSerial("cam", pid);
    }

    public static boolean isBell(int pid) {
        return PropertiesLoader.getInstance().isSerial("bell", pid);
    }

    /**
     * 判断是否全景
     *
     * @param pid
     * @return
     */
    public static boolean isNeedPanoramicView(int pid) {
        return isRoundRadio(pid);
    }


    public static boolean isPan720(int pid) {
        return pid == 1089 || pid == 21;
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showSdcard(int pid) {
        return PropertiesLoader.getInstance().hasProperty(pid,
                "SD");
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showSdcard(Device device) {
        return PropertiesLoader.getInstance().hasProperty(device.pid,
                "SD") && !JFGRules.isShareDevice(device);
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showSight(int pid, boolean share) {
        return PropertiesLoader.getInstance().hasProperty(pid,
                "VIEWANGLE", share);
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showRotate(int pid, boolean share) {
        return PropertiesLoader.getInstance().hasProperty(pid,
                "hangup", share);
    }

    public static Observable<Boolean> switchApModel(String mac, String uuid, int model) {
        if (TextUtils.isEmpty(mac)) {
            mac = PreferencesUtils.getString(JConstant.KEY_DEVICE_MAC + uuid);
        }
        if (TextUtils.isEmpty(mac)) {
            AppLogger.d("mac为空");
            return Observable.just(false);
        }
        String finalMac = mac;
        return Observable.just(model)
                .subscribeOn(Schedulers.io())
                .flatMap(s -> {
                    try {
                        for (int i = 0; i < 3; i++) {
                            JfgUdpMsg.UdpSetApReq req = new JfgUdpMsg.UdpSetApReq(uuid, finalMac);
                            req.model = s;
                            Command.getInstance().sendLocalMessage(UdpConstant.IP,
                                    UdpConstant.PORT, req.toBytes());
                        }
                        AppLogger.d("send UdpSetApReq :" + uuid + "," + finalMac);
                    } catch (JfgException e) {
                    }
                    return Observable.just(s);
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                        .subscribeOn(Schedulers.io())
                        .timeout(10, TimeUnit.SECONDS))//原型说10s
                .timeout(10, TimeUnit.SECONDS)
                .flatMap(localUdpMsg -> {
                    try {
                        JfgUdpMsg.UdpHeader header = DpUtils.unpackData(localUdpMsg.data, JfgUdpMsg.UdpHeader.class);
                        if (header != null && TextUtils.equals(header.cmd, "set_ap_rsp")) {
                            return Observable.just(localUdpMsg.data);
                        }
                    } catch (Exception e) {
                    }
                    return Observable.just(null);
                })
                .filter(ret -> ret != null)
                .filter(ret -> {
                    try {
                        UdpConstant.SetApRsp rsp = DpUtils.unpackData(ret, UdpConstant.SetApRsp.class);
                        return (rsp != null && TextUtils.equals(rsp.cid, uuid));
                    } catch (IOException e) {
                        return false;
                    }
                }).take(1)
                .flatMap(bytes -> Observable.just(true));
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showStandbyItem(int pid, boolean share) {
        return PropertiesLoader.getInstance().hasProperty(pid,
                "standby", share);
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showSdHd(int pid, String version, boolean share) {
        String sdContent = PropertiesLoader.getInstance().property(pid, "SD/HD");
        if (TextUtils.isEmpty(sdContent) || TextUtils.equals(sdContent, "0")) {
            return false;
        }
        if (TextUtils.equals("1", sdContent)) {
            return true;
        }
        String[] ret = sdContent.split(",");
        if (ret.length < 2 || !sdContent.contains(".")) {
            return false;
        }
        try {
            // TODO: 2017/8/18 可能会有问题
            return BindUtils.versionCompare(version, ret[1]) >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showBattery(int pid, boolean share) {
        return PropertiesLoader.getInstance().hasProperty(pid,
                "battery", share);
    }

    /**
     * 非常坑    /**
     *
     * @param pid
     * @return
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean popPowerDrainOut(int pid) {
        String pContent = PropertiesLoader.getInstance().property(pid, "POWER");
        if (TextUtils.isEmpty(pContent) || TextUtils.equals("0", pContent)) {
            return false;
        }
        String[] ret = pContent.split(",");
        if (ret.length < 2) {
            return false;
        }
        return TextUtils.equals(ret[0], "1");
    }

    /**
     * 电量 弹窗
     *
     * @param pid
     * @return
     */

    public static int popPowerDrainOutLevel(int pid) {
        String pContent = PropertiesLoader.getInstance().property(pid, "POWER");
        if (TextUtils.isEmpty(pContent) || TextUtils.equals("0", pContent)) {
            return -1;
        }
        String[] ret = pContent.split(",");
        if (ret.length < 2) {
            return -1;
        }
        try {
            return Integer.parseInt(ret[1].replace(" ", ""));
        } catch (Exception e) {
            return -1;
        }
    }


    //freeCam 海思 wifi

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showMobileNet(int pid, boolean share) {
        return is3GCam(pid);
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showLedIndicator(int pid, boolean share) {
        return PropertiesLoader.getInstance().hasProperty(pid,
                "led", share);
    }

    /**
     * 内部会 自动转成大写
     * /**
     *
     * @param pid
     * @param share
     * @return
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showIp(int pid, boolean share) {
        return PropertiesLoader.getInstance().hasProperty(pid,
                "IP", share);
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showWiredMode(int pid, boolean share) {
        return PropertiesLoader.getInstance().hasProperty(pid,
                "WIREDMODE", share);
    }

    public static boolean hasProperty(int pid, final String tag) {
        return PropertiesLoader.getInstance().hasProperty(pid,
                tag, false);
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showEnableAp(int pid, boolean share) {
        return PropertiesLoader.getInstance().hasProperty(pid,
                "enableAP", share);
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showFirmware(int pid, boolean share) {
        return PropertiesLoader.getInstance().hasProperty(pid,
                "fu", share);
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showSoftWare(int pid, boolean share) {
        return PropertiesLoader.getInstance().hasProperty(pid,
                "softVersion", share);
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showTimeZone(int pid, boolean share) {
        return PropertiesLoader.getInstance().hasProperty(pid,
                "tz", share);
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean isNeedNormalRadio(int pid) {
        return !isNeedPanoramicView(pid);
    }

    public static boolean isRuiShiCam(int pid) {
        return false;
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean hasProtection(int pid, boolean share) {
        IProperty productProperty = PropertiesLoader.getInstance();
        return productProperty.hasProperty(pid, "PROTECTION", false);
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean hasHistory(int pid, boolean share) {
        IProperty productProperty = PropertiesLoader.getInstance();
        return productProperty.hasProperty(pid, "AUTORECORD", share);
    }

    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean showHistoryBtn(Device device) {
        //1.sd卡 2.非分享用户 3.设备在线 4.设备没有待机 5.手机不是无网络状态
        return !JFGRules.isShareDevice(device) &&
                hasSdcard(device.$(204, new DpMsgDefine.DPSdStatus())) &&
                device.$(201, new DpMsgDefine.DPNet()).net > 0 &&
                !device.$(508, new DpMsgDefine.DPStandby()).standby &&
                NetUtils.getNetType(ContextUtils.getContext()) != -1;
    }

    public static boolean hasBatteryNotify(int pid) {
        return true;
    }


    /**
     * @deprecated 需要一并传入是否为共享账号
     */
    public static boolean hasAutoRecord(int pid) {
        return true;
    }

    public static long getCallTimeOut(Device device) {
        long timeOut = 30;//default is 30
        if (isCatEeyBell(device.pid)) {
            timeOut = 20;//猫眼呼叫时20 秒超时
        }
        return timeOut;
    }

    public static int getOSType(String content) {
        IProperty productProperty = PropertiesLoader.getInstance();
        return productProperty.getOSType(content);
    }

    public static boolean hasWarmSound(int pid) {
        IProperty productProperty = PropertiesLoader.getInstance();
        return productProperty.hasProperty(pid, "WARMSOUND");
    }

    public static boolean hasSDFeature(int pid) {
        IProperty productProperty = PropertiesLoader.getInstance();
        return productProperty.hasProperty(pid, "SD");
    }

    public static boolean isNeedTankView(int pid) {
        IProperty productProperty = PropertiesLoader.getInstance();
        String property = productProperty.property(pid, "VIEW");
        return !TextUtils.isEmpty(property) && property.contains("切掉上下部分");
    }

    public static boolean showSwitchModeButton(int pid) {
        PropertiesLoader loader = PropertiesLoader.getInstance();
        String view_mode = loader.property(pid, "VIEW_MODE");
        String view = loader.property(pid, "VIEW");
        return !TextUtils.isEmpty(view_mode) && TextUtils.equals(view_mode, "1");
    }

    public static boolean hasViewAngle(int pid) {
        PropertiesLoader loader = PropertiesLoader.getInstance();
        String view_mode = loader.property(pid, "VIEWANGLE");
        return !TextUtils.isEmpty(view_mode) && TextUtils.equals(view_mode, "1");
    }

    public static boolean isFaceFragment(int pid) {
        boolean hasFaceFeature = (pid == 83 || pid == 84 || pid == 92);
        if (!hasFaceFeature) {
            PropertiesLoader loader = PropertiesLoader.getInstance();
            String os = loader.property(pid, "OS");
            try {
                Integer intOS = Integer.valueOf(os);
                hasFaceFeature = (intOS == 83 || intOS == 84 || intOS == 92);
            } catch (Exception e) {
            }
        }
        return hasFaceFeature;
    }

    public static boolean hasMicFeature(int pid) {
        boolean hasMicFeature = pid != 84;
        if (!hasMicFeature) {
            PropertiesLoader loader = PropertiesLoader.getInstance();
            String os = loader.property(pid, "OS");
            try {
                Integer intOS = Integer.valueOf(os);
                switch (intOS) {
                    case 83:
                    case 84:
                    case 92:
                        hasMicFeature = false;
                        break;
                    default:
                        hasMicFeature = true;
                }
//                hasMicFeature = intOS != 84;
            } catch (Exception e) {
            }
        }
        return hasMicFeature;
    }

    public static boolean hasDoorLock(int pid) {
        boolean hasDoorLockFeature = false;
        PropertiesLoader loader = PropertiesLoader.getInstance();
        String door_lock = loader.property(pid, "DOOR_LOCK");
        hasDoorLockFeature = TextUtils.equals(door_lock, "1");
        return hasDoorLockFeature;
    }

    public static boolean shouldObserverAP() {
        //just for test
        return true;
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

    public static boolean isDeviceOnline(String uuid) {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        String mac = device.$(DpMsgMap.ID_202_MAC, "");
        if (TextUtils.isEmpty(mac)) {
            DeviceInformation information = BaseDeviceInformationFetcher.getInstance().getDeviceInformation();
            if (information != null && information.mac != null) {
                mac = information.mac;
            }
        }
        DpMsgDefine.DPNet net = device.$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
        boolean apMode = JFGRules.isAPDirect(uuid, mac);
        boolean isOnline = net.net > 0;
        return apMode || isOnline;
    }

    public static boolean hasSdcard(DpMsgDefine.DPSdStatus sdStatus) {
        return sdStatus != null && sdStatus.err == 0 && sdStatus.hasSdcard;
    }

    public static boolean hasSdcard(DpMsgDefine.DPSdcardSummary sdStatus) {
        return sdStatus != null && sdStatus.errCode == 0 && sdStatus.hasSdcard;
    }

    public static boolean isDeviceStandBy(Device device) {
        if (device == null) {
            return false;
        }
        DpMsgDefine.DPStandby standby = device.$(508, new DpMsgDefine.DPStandby());
        return standby != null && standby.standby;
    }

    public static boolean isShareDevice(String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            return false;
        }
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        return device != null && device.available() && !TextUtils.isEmpty(device.shareAccount);
    }

    public static boolean isShareDevice(Device device) {
        if (device == null) {
            return false;
        }
        return !TextUtils.isEmpty(device.shareAccount);
    }

    public static float getDefaultPortHeightRatio(int pid) {
        boolean normal = !isRoundRadio(pid);
        return normal ? 0.75f : 1.0f;
    }

    public static boolean isRoundRadio(int pid) {
        IProperty property = PropertiesLoader.getInstance();
        String view = property.property(pid, "VIEW");
        return !TextUtils.isEmpty(view) && (view.contains("圆形") || view.contains("鱼缸"));
    }


    public static TimeZone getDeviceTimezone(Device device) {
        if (device == null) {
            return TimeZone.getDefault();
        }
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
        if (TextUtils.isEmpty(mac)) {
            mac = PreferencesUtils.getString(JConstant.KEY_DEVICE_MAC + uuid);
        }
        return MiscUtils.isAPDirect(mac);
    }

    /**
     * 睿视
     *
     * @param pid
     * @return
     */
    public static boolean isConsumerCam(int pid) {
        final String value = PropertiesLoader.getInstance().property(pid, "value");
        return !TextUtils.isEmpty(value) && value.startsWith("RS_CAM");//不是下划线,直接去掉
    }

    public static boolean isCloudCam(int pid) {
        final String value = PropertiesLoader.getInstance().property(pid, "value");
        return !TextUtils.isEmpty(value) && value.startsWith("house");//不是下划线,直接去掉
    }

    public static boolean isCatEeyBell(int pid) {
        final String value = PropertiesLoader.getInstance().property(pid, "product");
        return !TextUtils.isEmpty(value) && value.contains("猫眼");//不是下划线,直接去掉
    }

    /**
     * 无电池
     *
     * @param pid
     * @return
     */
    public static boolean isNoPowerBell(int pid) {
        final String value = PropertiesLoader.getInstance().property(pid, "product");
        return !TextUtils.isEmpty(value) && value.contains("有源");//不是下划线,直接去掉
    }

}
