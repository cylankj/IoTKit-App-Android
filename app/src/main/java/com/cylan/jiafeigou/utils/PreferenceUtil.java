package com.cylan.jiafeigou.utils;

import android.content.Context;

public class PreferenceUtil {

    // 保存版本名 用户打开应用版本名不一样 需要清空缓存
    private static final String KEY_VERSIONNAME = "key_versionname";
    // TODO都是假数据
    private static final String PREF_NAME = "config_pref";

    private static final String KEY_PHONE = "PhoneNum";
    private static final String KEY_PSW = "PSW";

    private static final String KEY_GUIDE = "guide";
    private static final String SESSIONID = "sessid";
    // 升级url的host
    private static final String KEY_CHECK_VERSION = "check_version";
    // 升级url的id
    private static final String KEY_CHECK_VERSION_URL = "check_version_url";

    private static final String KEY_IS_LOGOUT = "is_logout";

    // 保存当前的主题
    private static final String KEY_DEFAULT_HOME_COVER = "home_cover";
    // 是否强制升级
    private static final String KEY_IS_UPGRADE = "is_upgrade";
    // 首页消息数
    private static final String KEY_MSG_COUNT = "msg_count";
    // 是否需要更新
    private static final String KEY_IS_NEED_UPGRADE = "is_need_upgrade";
    // 分辨率
    private static final String KEY_RESOLUTION = "key_resolution";

    // set_voice
    private static final String KEY_SET_ISOPEN_VOICE = "key_set_isopne_voice";
    // set_vibrate
    private static final String KEY_SET_ISOPEN_VIBRATE = "key_set_isopen_vibrate";
    // 第一次进入主页
    private static final String KEY_IS_FIRST_INHOME = "key_is_first_inhome";
    // 是否第三方登录或者快捷登陆
    private static final String KEY_IS_OTHER_LOGIN_TYPE = "key_is_other_login_type";
    // 是否第一方登录
    private static final String KEY_IS_LOGIN_TYPE = "key_is_login_type";
    // 是否第三方登录或者快捷登陆 登陆类型 0:qq登陆 1:新浪登录
    private static final String KEY_OTHER_LOGIN_TYPE = "key_other_login_type";

    // 是否安全登录
    private static final String KEY_IS_SAFE_LOGIN = "key_is_safe_login";

    // 第一次进入直播竖屏
    private static final String KEY_ISFIRST_LIVE_VERTICALSCREEN = "key_isfirst_live_verticalscreen";
    // 第三方登录的头像uri
    private static final String KEY_THIRDswLogIN_PICURL = "key_thirDswLogin_picurl";
    // first add
    private static final String KEY_FIRST_ADD_CARAME = "key_first_add_carame";
    // login edit account
    private static final String KEY_LOGIN_ACCOUNT = "key_login_account";
    // first in historyvideo
    private static final String KEY_FIRST_HISTORYVIDEO = "key_first_historyvideo";
    // download apk addresss
    private static final String KEY_DOWNLOAD_ADDRESS = "key_download_address";
    //the first click automatic_video
    private static final String KEY_FIRST_CLICK_AUTOMATIC_VIDEO = "key_first_click_automatic_video";
    // first add doorbell
    private static final String KEY_FIRST_ADD_DOORBELL = "key_first_add_doorbell";
    //efamily update time
    private static final String KEY_EFAMILY_UPDATE_TIME = "key_efamily_update_time";
    //Location Change
    private static final String KEY_LOCATION_IS_CHANGE = "key_location_is_change";
    //第一次点击安全防护
    private static final String KEY_FIRST_PRESS_SAFEPROTECT = "key_first_press_safe";
    //first to click sens
    private static final String KEY_FIRST_PRESS_SENS = "key_first_press_sens";
    //mac address
    private static final String KEY_DEVICE_MAC = "key_device_mac";

    private static final String KEY_HAS_MOBILE = "key_has_mobile";

    private static final String KEY_IP = "KEY_IP";

    private static final String KEY_PORT = "KEY_PORT";

    private static final String KEY_FIRST_BELL_SET = "key_first_bell_set";

    private static final String KEY_NTP_TIME_DIFF = "KEY_NTP_TIME_DIFF";

    private static final String KEY_GET_MAG_WARN = "key_get_mag_warn";
    private static final String KEY_ISFIRST_TO_EFAMILY = "KEY_ISFIRST_TO_EFAMILY";
    //oss url head
    private static final String KEY_OSS_URL_HEADER_KEY = "KEY_OSS_URL_HEADER_KEY";
    private static final String KEY_OSS_TYPE_KEY = "KEY_OSS_TYPE_KEY";

    public static String getVersionName(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_VERSIONNAME, "");
    }

    public static void setVersionName(Context ctx, String version) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_VERSIONNAME, version).commit();
    }


    public static String getBindingPhone(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_PHONE, "");
    }

    public static void setBindingPhone(Context ctx, String tel) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_PHONE, tel).commit();
    }

    public static String getPSW(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_PSW, "");
    }

    public static void setPSW(Context ctx, String mPSW) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_PSW, mPSW).commit();
    }

    public static void setSessionId(Context ctx, String sessionid) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(SESSIONID, sessionid).commit();
    }

    public static String getSessionId(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(SESSIONID, "");
    }

    public static void cleanSessionId(Context ctx) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(SESSIONID, "").commit();
    }

    public static boolean needShowGuide(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_GUIDE, true);
    }

    public static void needShowGuide(Context ctx, boolean newShow) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_GUIDE, newShow).commit();
    }


    public static String getCheckVersion(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_CHECK_VERSION, "0");
    }

    public static void setCheckVersion(Context ctx, String version) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_CHECK_VERSION, version).commit();
    }

    public static String getCheckVersionUrl(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_CHECK_VERSION_URL, "");
    }

    public static void setCheckVersionUrl(Context ctx, String version) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_CHECK_VERSION_URL, version).commit();
    }

    public static Boolean getIsLogout(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_IS_LOGOUT, true);
    }

    public static void setIsLogout(Context ctx, Boolean is) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_IS_LOGOUT, is).commit();
    }

    public static int getHomeCover(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_DEFAULT_HOME_COVER, -1);
    }

    public static void setHomeCover(Context ctx, int pic) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_DEFAULT_HOME_COVER, pic).commit();
    }

    public static int getIsUpgrade(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_IS_UPGRADE, 0);
    }

    public static void setIsUpgrade(Context ctx, int a) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_IS_UPGRADE, a).commit();
    }

    public static int getKeyMsgCount(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_MSG_COUNT, 0);
    }

    public static void setKeyMsgCount(Context ctx, int addr) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_MSG_COUNT, addr).commit();
    }

    public static boolean getIsNeedUpgrade(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_IS_NEED_UPGRADE, false);
    }

    public static void setIsNeedUpgrade(Context ctx, boolean is) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_IS_NEED_UPGRADE, is).commit();
    }

    public static String getResolution(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_RESOLUTION, "");
    }

    public static void setResolution(Context ctx, String res) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_RESOLUTION, res).commit();
    }

    public static Boolean getKeySetIsOpenVoice(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_SET_ISOPEN_VOICE, true);
    }

    public static void setKeySetIsOpenVoice(Context ctx, Boolean res) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_SET_ISOPEN_VOICE, res).commit();
    }

    public static Boolean getKeySetIsOpenVibrate(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_SET_ISOPEN_VIBRATE, true);
    }

    public static void setKeySetIsOpenVibrate(Context ctx, Boolean res) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_SET_ISOPEN_VIBRATE, res).commit();
    }

    public static Boolean getIsFirstInHome(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_IS_FIRST_INHOME, true);
    }

    public static void setIsFirstInHome(Context ctx) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_IS_FIRST_INHOME, false).commit();
    }

    public static Boolean getIsOtherLoginType(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_IS_OTHER_LOGIN_TYPE, false);
    }

    public static void setIsOtherLoginType(Context ctx, boolean is) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_IS_OTHER_LOGIN_TYPE, is).commit();
    }

    public static int getOtherLoginType(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_OTHER_LOGIN_TYPE, -1);
    }

    public static void setIsLoginType(Context ctx, boolean is) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_IS_LOGIN_TYPE, is).commit();
    }

    public static boolean getIsLoginType(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_IS_LOGIN_TYPE, false);
    }

    public static void setOtherLoginType(Context ctx, int is) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_OTHER_LOGIN_TYPE, is).commit();
    }

    public static Boolean getIsSafeLogin(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_IS_SAFE_LOGIN, true);
    }

    public static void setIsSafeLogin(Context ctx, boolean is) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_IS_SAFE_LOGIN, is).commit();
    }

    public static Boolean getIsFirstVerticalScreen(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_ISFIRST_LIVE_VERTICALSCREEN, true);
    }

    public static void setIsFirstVerticalScreen(Context ctx, Boolean str) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_ISFIRST_LIVE_VERTICALSCREEN, str).commit();
    }

    public static String getThirDswLoginPicUrl(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_THIRDswLogIN_PICURL, "");
    }

    public static void setThirDswLoginPicUrl(Context ctx, String str) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_THIRDswLogIN_PICURL, str).commit();
    }

    public static boolean getFirstAddCarame(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_FIRST_ADD_CARAME, true);
    }

    public static void setFirstAddCarame(Context ctx, boolean str) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_FIRST_ADD_CARAME, str).commit();
    }

    public static String getLoginAccount(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_LOGIN_ACCOUNT, "");
    }

    public static void setLoginAccount(Context ctx, String str) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_LOGIN_ACCOUNT, str).commit();
    }

    public static boolean getFirstHistoryVideo(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_FIRST_HISTORYVIDEO, true);
    }

    public static void setFirstHistoryVideo(Context ctx, boolean str) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_FIRST_HISTORYVIDEO, str).commit();
    }

    public static String getDownloadAddressUrl(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_DOWNLOAD_ADDRESS, "");
    }

    public static void setDownloadAddressUrl(Context ctx, String str) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_DOWNLOAD_ADDRESS, str).commit();
    }

    public static boolean getIsFirstClickAutomaticVideo(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_FIRST_CLICK_AUTOMATIC_VIDEO, true);
    }

    public static void setIsFirstClickAutomaticVideo(Context ctx, boolean str) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_FIRST_CLICK_AUTOMATIC_VIDEO, str).commit();
    }


    public static void setIsFirstAddDoorbell(Context ctx, boolean str) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_FIRST_ADD_DOORBELL, str).commit();
    }

    public static boolean getIsFirstAddDoorbell(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_FIRST_ADD_DOORBELL, true);
    }

    public static void setEfamilyUpdateTime(Context ctx, long time) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putLong(KEY_EFAMILY_UPDATE_TIME, time).commit();
    }

    public static long getEfamilyUpdateTime(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getLong(KEY_EFAMILY_UPDATE_TIME, 0);
    }

    public static void setLocationisChange(Context ctx, boolean isChange) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_LOCATION_IS_CHANGE, isChange).commit();
    }

    public static boolean getLocationisChange(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_LOCATION_IS_CHANGE, false);
    }

    public static void setKeyFirstSafe(Context ctx, boolean isFirst) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_FIRST_PRESS_SAFEPROTECT, isFirst).commit();
    }

    public static boolean getKeyFirstSafe(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_FIRST_PRESS_SAFEPROTECT, false);
    }

    public static void setKeyFirstSen(Context ctx, boolean isFirst) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_FIRST_PRESS_SENS, isFirst).commit();
    }

    public static boolean getKeyFirstSen(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_FIRST_PRESS_SENS, false);
    }

    public static void setDeviceMacAddress(Context ctx, String str) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_DEVICE_MAC, str).commit();
    }

    public static String getDeviceMacAddress(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_DEVICE_MAC, "");
    }

    public static void setHasMobile(Context ctx, int value) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_HAS_MOBILE, value).commit();
    }

    public static int getHasMobile(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_HAS_MOBILE, 0);
    }

    public static String getIP(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_IP, "");
    }

    public static void setIP(Context ctx, String str) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_IP, str).commit();
    }

    public static int getPort(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_PORT, 0);
    }

    public static void setPort(Context ctx, int str) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_PORT, str).commit();
    }


    public static boolean getKeyIsFirstClickDoorBellSet(Context context){
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_FIRST_BELL_SET, true);
    }

    public static void setKeyIsFirstClickDoorBellSet(Context context, boolean isFirst){
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_FIRST_BELL_SET, isFirst).commit();
    }

    public static int getKeyNtpTimeDiff(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_NTP_TIME_DIFF, 0);
    }

    public static void setKeyNtpTimeDiff(Context ctx, int diff) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_NTP_TIME_DIFF, diff).commit();
    }

    public static boolean getKeyMagWarnRsp(Context context){
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_GET_MAG_WARN, false);
    }

    public static void setKeyMagWarnRsp(Context context, boolean isChecked){
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_GET_MAG_WARN, isChecked).commit();
    }

    public static boolean getKeyIsFirstToEfamily(Context context){
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_ISFIRST_TO_EFAMILY, true);
    }

    public static void setKeyIsFirstToEfamily(Context context, boolean isFirst){
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_ISFIRST_TO_EFAMILY, isFirst).commit();
    }

    public static void setOssUrl(Context ctx, String str) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_OSS_URL_HEADER_KEY, str).commit();
    }

    public static String getOssUrl(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_OSS_URL_HEADER_KEY, "");
    }

    public static void setOssTypeKey(Context context, int type){
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_OSS_TYPE_KEY, type).commit();
    }

    public static int getOssTypeKey(Context context){
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_OSS_TYPE_KEY, 0);
    }

}
