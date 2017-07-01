package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.cylan.jiafeigou.n.view.adapter.item.HomeItem;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.bind.UdpConstant.BIND_TAG;
import static com.cylan.jiafeigou.utils.ContextUtils.getContext;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public class MiscUtils {

    private static final int BYTE = 1024;
    private static final int MEGA_BYTE = 1024 * 1024;
    private static final int GIGA_BYTE = 1024 * 1024 * 1024;

    public static boolean isInRange(int start, int end, int dst) {
        return dst >= start && dst <= end;
    }

    public static boolean isBad(List<Integer> list, int level, int count) {
        if (list == null || list.size() < count)
            return false;
        final int size = list.size();
        int result = 0;
        for (int i = 0; i < size; i++) {
            if (list.get(i) < level)
                result++;
        }
        return result >= count;
    }

    public static int parseTime(String time) {
        String[] times = time.split(":");
        final String h = Integer.toBinaryString(times[0].length() > 1 ? Integer.parseInt(times[0].substring(times[0].length() - 2, times[0].length())) : Integer.parseInt(times[0].substring(times[0].length() - 1, times[0].length())));
        final String m = String.format(Locale.getDefault(), "%08d",
                Integer.parseInt(Integer.toBinaryString(Integer.parseInt(times[1]))));
        return Integer.parseInt(h + m, 2);
    }

    public static String parse2Time(int value) {
        return String.format(Locale.getDefault(), "%02d", value >> 8)
                + String.format(Locale.getDefault(), ":%02d", (((byte) value << 8) >> 8));
    }

    public static String getByteFromBitRate(long bitRate) {
        bitRate = bitRate / 8;
        return getFlowResult(bitRate);
    }

    public static String getFlowResult(long byteData) {
        if (byteData < BYTE)
            return byteData + "K/account";
        if (byteData >= BYTE && byteData < MEGA_BYTE) {
            return (byteData >>> 10) + "M/account";
        }
        if (byteData >= MEGA_BYTE && byteData < GIGA_BYTE) {
            return (byteData >>> 20) + "G/account";
        }
        if (byteData >= GIGA_BYTE && byteData < 1024L * GIGA_BYTE) {
            return (byteData >>> 30) + "T/account";
        }
        return "";
    }

//    public static void main(String[] args) {
//        System.out.println(getFlowResult(1024 * 1024 + 1));
//        System.out.println(getFlowResult(1024 * 1024));
//        System.out.println(getFlowResult(1024 * 1024 - 1));
//        System.out.println(getFlowResult(1025));
//        System.out.println(getFlowResult(1024));
//        System.out.println(getFlowResult(1023));
//        System.out.println(getCount(1));
//        System.out.println(getCount(1));
//        System.out.println(getCount(1));
//        System.out.println(getCount(1));
//    }

    public static int getCount(int sum) {
        int count = 0;
        for (int i = 0; i < 3; i++) {
            if ((sum >> i & 0x01) == 1) count++;
        }
        return count == 0 ? 1 : count;
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object object, T defaultValue) {
        try {
            return object == null ? defaultValue : (T) object;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static Observable<List<TimeZoneBean>> loadTimeZoneList() {
        return Observable.just(R.xml.timezones)
                .subscribeOn(Schedulers.computation())
                .flatMap(new Func1<Integer, Observable<List<TimeZoneBean>>>() {
                    @Override
                    public Observable<List<TimeZoneBean>> call(Integer integer) {
                        XmlResourceParser xrp = getContext().getResources().getXml(integer);
                        List<TimeZoneBean> list = new ArrayList<>();
                        try {
                            final String tag = "timezone";
                            while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                                if (xrp.getEventType() == XmlResourceParser.START_TAG) {
                                    TimeZoneBean bean = new TimeZoneBean();
                                    final String name = xrp.getName();
                                    if (TextUtils.equals(name, tag)) {
                                        final String timeGmtName = xrp.getAttributeValue(0);
                                        bean.setGmt(timeGmtName);
                                        final String timeIdName = xrp.getAttributeValue(1);
                                        bean.setId(timeIdName);
                                        String region = xrp.nextText().replace("\n", "");
                                        bean.setName(region);
                                        int factor = timeGmtName.contains("+") ? 1 : -1;
                                        String digitGmt = BindUtils.getDigitsString(timeGmtName);
                                        int offset = factor * Integer.parseInt(digitGmt.substring(0, 2)) * 3600 +
                                                factor * (timeGmtName.contains(":30") ? 3600 / 2 : 0);
                                        bean.setOffset(offset);
                                        list.add(bean);
                                    }
                                }
                                xrp.next();
                            }
                            Log.d(tag, "timezone: " + list);
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                        }
                        return Observable.just(list);
                    }
                });
    }

    public static <T> T getValue(Object o, T t) {
        if (o == null) return t;
        try {
            return (T) o;
        } catch (Exception e) {
            return t;
        }
    }


    public static String getBeautifulString(String temp, int len) {
        if (!TextUtils.isEmpty(temp) && temp.length() > len) {
            temp = temp.substring(0, len) + "...";
        }
        return temp;
    }

    public static ArrayList<String> getSharedList(List<Device> devices) {
        if (devices == null)
            return new ArrayList<>();
        ArrayList<String> arrayList = new ArrayList<>();
        for (Device device : devices) {
            if (device != null && !TextUtils.isEmpty(device.shareAccount)) {
                arrayList.add(device.uuid);
            }
        }
        return arrayList;
    }

    public static ArrayList<String> getNoneSharedList(List<Device> devices) {
        if (devices == null)
            return new ArrayList<>();
        ArrayList<String> arrayList = new ArrayList<>();
        for (Device device : devices) {
            if (device != null && TextUtils.isEmpty(device.shareAccount)) {
                arrayList.add(device.uuid);
            }
        }
        return arrayList;
    }

    /**
     * 还需要判断 2.0版本的设备，3.0版本的设备
     *
     * @param device
     * @return
     */
    public static ArrayList<Long> createGetCameraWarnMsgDp(Device device) {
        ArrayList<Long> list = new ArrayList<>();
        boolean isV2 = TextUtils.isEmpty(device.vid);
        list.add(isV2 ? 505L : 512L);
        list.add(222L);
        return list;
    }

    public static boolean checkWriteExternalPermission() {
        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }


    public static int setBit(int x, int n, int flag) {
        if (flag == 1) {
            x |= (1 << (n));
        } else if (flag == 0) {
            x &= ~(1 << (n));
        }
        return x;
    }

    public static int getBit(int n, int k) {
        return (n >> k) & 1;
    }

    public static <T> T safeGet(DpMsgDefine.DPPrimary<? extends T> value, T defaultValue) {
        if (value != null && value.value != null) {
            try {
                return value.value;
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }


    /**
     * @param set
     * @param min ： max or min
     * @param <T>
     * @return
     */
    public static <T extends DataPoint> long getVersion(List<T> set, boolean min) {
        if (set != null && set.size() > 0) {
            Collections.sort(set);
            if (min)
                return set.get(set.size() - 1).getVersion();
            else {
                return set.get(0).getVersion();
            }
        }
        return 0L;
    }

    public static JFGDPMsg getMessageByVersion(long id, long version) {
        JFGDPMsg msg = new JFGDPMsg();
        msg.id = id;
        msg.version = version;
        return msg;
    }


    public static boolean checkGpsAvailable(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static <T> T getObjectFromSP(String key, Class<T> tClass) {
        try {
            String content = PreferencesUtils.getString(key);
            if (TextUtils.isEmpty(content)) return tClass.newInstance();
            return new Gson().fromJson(content, tClass);
        } catch (Exception e) {
            return null;
        }
    }

//    public static long[] getChaosDpList(boolean isV2) {
//        return isV2 ? new long[]{505L,222L,512} : new long[]{222L, 512L};
//    }

    /**
     * desc:转换文件的大小
     *
     * @param fileS
     * @return
     */
    public static String FormatSdCardSize(long fileS) {
        return FormatSdCardSizeSpec(fileS, "B");
    }

    /**
     * desc:转换文件的大小
     *
     * @param fileS
     * @return
     */
    public static String FormatSdCardSizeSpec(long fileS, String unit) {
        DecimalFormat df = new DecimalFormat("#######0.00");
        switch (unit) {
            case "B":
                if (fileS < 1024) return df.format((double) fileS) + "B";
            case "K":
                if (fileS < 1048576) return df.format((double) fileS / 1024) + "K";
            case "M":
                if (fileS < 1073741824) return df.format((double) fileS / 1048576) + "M";
            case "G":
                return df.format((double) fileS / 1073741824) + "G";
            default:
                return "";
        }
    }


    public static String getChaosTime(Context context, DpMsgDefine.DPAlarmInfo info, boolean off) {
        if (!off) {
            return context.getString(R.string.MAGNETISM_OFF);
        }
        //每一天
        if (info.day == 127) {
            if (info.timeStart == 0 && info.timeEnd == 5947)
                return context.getString(R.string.EVERY_DAY) + " " + context.getString(R.string.HOURS);
            return context.getString(R.string.EVERY_DAY) + " " + getTime(context, info.timeStart, info.timeEnd);
        }
        //工作日
        if (info.day == 124) {
            if (info.timeStart == 0 && info.timeEnd == 5947)
                return context.getString(R.string.WEEKDAYS) + " " + context.getString(R.string.HOURS);
            return context.getString(R.string.WEEKDAYS) + " " + getTime(context, info.timeStart, info.timeEnd);
        }
        //周末
        if (info.day == 3) {
            if (info.timeStart == 0 && info.timeEnd == 5947)
                return context.getString(R.string.WEEKEND) + " " + context.getString(R.string.HOURS);
            return context.getString(R.string.WEEKEND) + " " + getTime(context, info.timeStart, info.timeEnd);
        }
        //零散
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (((info.day >> (7 - 1 - i)) & 0x01) == 1) {
                //hit
                builder.append(context.getString(periodResId[i]));
                builder.append(",");
            }
        }
        if (builder.length() > 1)
            builder.replace(builder.length() - 1, builder.length(), "");
        if (info.timeStart == 0 && info.timeEnd == 5947) {
            builder.append(" ");
            return builder.append(context.getString(R.string.HOURS)).toString();
        }
        return builder.append(getTime(context, info.timeStart, info.timeEnd)).toString();
    }

    private static String getTime(Context context, int timeStart, int timeEnd) {
        if (timeStart >= timeEnd) {
            StringBuilder builder = new StringBuilder(parse2Time(timeStart));
            return builder.append("-")
                    .append(context.getString(R.string.TOW))
                    .append(parse2Time(timeEnd)).toString();
        }
        return parse2Time(timeStart) + "-" + parse2Time(timeEnd);
    }

    private static final int[] periodResId = {R.string.MON_1, R.string.TUE_1,
            R.string.WED_1, R.string.THU_1,
            R.string.FRI_1, R.string.SAT_1, R.string.SUN_1};


    /**
     * 文件名，命名格式[timestamp].jpg 或[timestamp]_[secends].avi，timestamp是文件生成时间的unix时间戳，secends是视频录制的时长,单位秒。根据后缀区分是图片或视频。
     * 提取时间戳
     * 1489756095
     *
     * @param content
     * @return
     */
    public static int getValueFrom(String content) {
        if (TextUtils.isEmpty(content) || content.length() < 10)
            return 0;
        try {
            return Integer.valueOf(content.substring(0, 10));
        } catch (Exception e) {
            return 0;
        }
    }

    public static List<JFGDPMsg> getCamDateVersionList(long startTime) {
        ArrayList<JFGDPMsg> list = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            list.add(makeMsg(startTime - 24 * 3600 * 1000L * i, 222));
            list.add(makeMsg(startTime - 24 * 3600 * 1000L * i, 505));
            list.add(makeMsg(startTime - 24 * 3600 * 1000L * i, 512));
        }
        return list;
    }

    private static JFGDPMsg makeMsg(long version, long msgId) {
        return new JFGDPMsg(msgId, version);
    }

    /**
     * 从{@link android.support.v4.app.FragmentPagerAdapter}中抽出来
     *
     * @param viewId
     * @param id
     * @return
     */
    public static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }


    public static DPEntity getMaxVersionEntity(DPEntity... entities) {
        if (entities == null) return null;
        List<DPEntity> list = new ArrayList<>();
        for (DPEntity entity : entities) {
            if (entity != null)
                list.add(entity);
        }
        if (list.size() == 0) return null;
        Collections.sort(list);
        return list.get(0);//降序的
    }

    public static ArrayList<IDPEntity> msgList(DBAction dbAction, String uuid, String account, String server, List<JFGDPMsg> list) {
        ArrayList<IDPEntity> idpEntities = new ArrayList<>();
        for (JFGDPMsg jfgdpMsg : list) {
            DPEntity dpEntity = new DPEntity();
            dpEntity.setUuid(uuid);
            dpEntity.setVersion(jfgdpMsg.version);
            dpEntity.setBytes(jfgdpMsg.packValue);
            dpEntity.setMsgId((int) jfgdpMsg.id);
            dpEntity.setAction(dbAction);
            dpEntity.setAccount(account);
            dpEntity.setServer(server);
            idpEntities.add(dpEntity);
        }
        return idpEntities;
    }

    public static List<IDPEntity> buildEntity(String uuid, long msgId, long version, boolean asc) {
        List<IDPEntity> list = new ArrayList<>();
        list.add(new DPEntity()
                .setMsgId((int) msgId)
                .setUuid(uuid)
                .setVersion(version)
                .setOption(new DBOption.SimpleMultiDpQueryOption(1, asc))
                .setAccount(BaseApplication.getAppComponent().getSourceManager().getJFGAccount().getAccount()));
        return list;
    }

    public static class DPEntityBuilder {
        private List<IDPEntity> list = new ArrayList<>();

        public DPEntityBuilder() {
        }

        public DPEntityBuilder add(DBAction action, String uuid, long msgId, long version, boolean asc) {
            list.add(new DPEntity()
                    .setMsgId((int) msgId)
                    .setUuid(uuid)
                    .setAction(action)
                    .setVersion(version)
                    .setOption(new DBOption.SimpleMultiDpQueryOption(1, asc))
                    .setAccount(BaseApplication.getAppComponent().getSourceManager().getJFGAccount().getAccount()));
            return this;
        }

        public List<IDPEntity> build() {
            return list;
        }
    }

    public static String getErr(Throwable throwable) {
        if (throwable == null) return null;
        return "err:" + throwable.getLocalizedMessage();
    }

    public static boolean isLand() {
        return ContextUtils.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static List<String> getUUidListFromItem(List<HomeItem> list) {
        if (list == null || list.size() == 0) return new ArrayList<>();
        List<String> list1 = new ArrayList<>(list.size());
        for (HomeItem item : list) {
            list1.add(item.getUUid());
        }
        return list1;
    }

    public static List<HomeItem> getHomeItemListFromDevice(List<Device> list) {
        if (list == null || list.size() == 0) return new ArrayList<>();
        List<HomeItem> list1 = new ArrayList<>(list.size());
        for (Device item : list) {
            Log.d("FromDevice", "item:" + item.pid);
            list1.add(new HomeItem().withUUID(item.uuid, item));
        }
        return list1;
    }

    public static boolean insertImage(String filePath, String fileName) {
        // 其次把文件插入到系统图库
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            if (sdcard != null) {
                File mediaDir = new File(sdcard, "DCIM/Camera");
                if (!mediaDir.exists()) {
                    mediaDir.mkdirs();
                }
            }
            MediaStore.Images.Media.insertImage(ContextUtils.getContext().getContentResolver(),
                    filePath + File.separator + fileName, fileName, null);
            // 最后通知图库更新
            ContextUtils.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath + File.separator + fileName)));
            return true;
        } catch (Exception e) {
            AppLogger.e("insertImage err: " + MiscUtils.getErr(e));
            return false;
        }
    }

    public static String getFileNameWithoutExn(String url) {
//        String url = "http://oss-cn-hangzhou.aliyuncs.com/jiafeigou-test/package/camera/JFG1W-2.4.6.28-V1-SENSOR_8330.bin?Expires=1521872297&Signature=dbu%2F0nQ3aNGz0wGIAieB7opsNiI%3D&OSSAccessKeyId=xjBdwD1du8lf2wMI";
        String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public static boolean isDeviceInWLAN(String uuid) {
        if (TextUtils.isEmpty(uuid) && BuildConfig.DEBUG)
            throw new IllegalArgumentException("uuid is  null");
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        boolean isApDirect = JFGRules.isAPDirect(uuid, device.$(202, ""));
        if (isApDirect) {//Ap
            return true;
        } else {//局域网
            //在线
            String appSSID = NetUtils.getNetName(ContextUtils.getContext());
            return TextUtils.equals(appSSID, device.$(201, new DpMsgDefine.DPNet()).ssid);
        }
    }

    /**
     * 属于绑定过程,恢复公网的wifi
     */
    public static void recoveryWiFi() {
        WifiManager wifiManager = (WifiManager) ContextUtils.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> list =
                wifiManager.getConfiguredNetworks();
        WifiInfo info = wifiManager.getConnectionInfo();
        AppLogger.d("当前连接的网络:" + info.getSSID() + ":" + info.getNetworkId());
        boolean disconnect = wifiManager.disconnect();
        AppLogger.d("断开网络是否成功:" + disconnect);
        boolean disableNetwork = wifiManager.disableNetwork(info.getNetworkId());
        AppLogger.d("禁用网络是否成功:" + disableNetwork);
        if (list != null) {
            int highPriority = -1;
            int index = -1;
            for (int i = 0; i < list.size(); i++) {
                String ssid = list.get(i).SSID;
                if (!JFGRules.isCylanDevice(ssid)) {
                    //恢复之前连接过的wifi
                    if (highPriority < list.get(i).priority) {
                        highPriority = list.get(i).priority;
                        index = i;
                    }
                } else {
                    WifiConfiguration configuration = list.get(i);
                    boolean s = wifiManager.disableNetwork(configuration.networkId);
                    boolean b = wifiManager.removeNetwork(configuration.networkId);
                    AppLogger.d("禁用加菲狗 Dog:" + s + "移除加菲狗 dog:" + b);
                }
            }
            if (index != -1) {
                boolean enableNetwork = wifiManager.enableNetwork(list.get(index).networkId, false);
                AppLogger.d("re enable ssid: " + list.get(index).SSID + "success:" + enableNetwork);
                boolean reconnect = wifiManager.reconnect();
                AppLogger.d("re connect :" + reconnect);
            }
        }
    }

    public static Observable<String> getAppVersionFromGooglePlay() {
        return Observable.just("getVersion")
                .subscribeOn(Schedulers.newThread())
                .filter(ret -> {
                    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
                    int resultCode = apiAvailability.isGooglePlayServicesAvailable(ContextUtils.getContext());
                    return resultCode == ConnectionResult.SUCCESS;//可用的时候,检查
                })
                .flatMap(s -> {
                    try {
                        String newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + "com.cylan.jiafeigou" + "&hl=en")
                                .timeout(10000)
                                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                                .referrer("http://www.google.com")
                                .get()
                                .select("div[itemprop=softwareVersion]")
                                .first()
                                .ownText();
                        return Observable.just(newVersion);
                    } catch (Exception e) {
                        return Observable.just("");
                    }
                });
    }

    public static boolean isGooglePlayServiceAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(ContextUtils.getContext());
        return resultCode == ConnectionResult.SUCCESS;//可用的时候,检查
    }

    public static boolean isAPDirect(String mac) {
        //没有连接公网.//必须是连接状态
        WifiInfo info = NetUtils.getWifiManager(ContextUtils.getContext()).getConnectionInfo();
        boolean state = info != null && info.getSupplicantState() == SupplicantState.COMPLETED;
        return TextUtils.equals(NetUtils.getRouterMacAddress(), mac) && state;
    }

    public static long getFileSizeFromUrl(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = new OkHttpClient().newCall(request).execute();
            return response.body().contentLength();
        } catch (IOException e) {
            return 0;
        }
    }

    public static String verticalString(String str) {
        if (TextUtils.isEmpty(str)) return "\n";
        StringBuilder builder = new StringBuilder(str.length() * 2);
        for (char c : str.toCharArray()) {
            builder.append(c).append("\n");
        }
        return builder.toString();
    }

    /**
     * 看看是否安装了 doby cell_c
     */
    public static void checkJFGLikeApp() {
        Observable.just("go")
                .subscribeOn(Schedulers.newThread())
                .subscribe(ret -> {
                    final PackageManager pm = ContextUtils.getContext().getPackageManager();
                    //get a list of installed apps.
                    List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                    for (ApplicationInfo packageInfo : packages) {
                        final String pname = packageInfo.packageName;
                        if (!TextUtils.isEmpty(pname) && pname.contains("com.cylan.jiafeigou")) {
                            AppLogger.e(BIND_TAG + pname);
                        }
                    }
                }, AppLogger::e);
    }

    public static void checkVPNState() {
        Observable.just("check")
                .subscribeOn(Schedulers.newThread())
                .subscribe(ret -> AppLogger.d(BIND_TAG + "vpn is on..." + NetUtils.isVPNOn()), AppLogger::e);
    }

}