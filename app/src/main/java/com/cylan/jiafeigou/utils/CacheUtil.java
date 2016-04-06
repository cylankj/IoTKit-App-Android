package com.cylan.jiafeigou.utils;

import com.cylan.jiafeigou.base.MyApp;

import java.io.Serializable;

public class CacheUtil {

    private static final String CID_LIST_KEY = "cid_list_key";
    private static final String ADD_DEVICE_CACHE = "add_device_cache";
    private static final String MSG_CENTER_KEY = "msg_center_key";
    private static final String MSG_DETAIL_KEY = "msg_detail_key";
    private static final String MSG_ACCOUNT_KEY = "account_key";
    private static final String MSG_EFAMILY_KEY = "efamily_key";
    private static final String MSG_VIDEO_CONFIG_KEY = "video_config_key";
    private static final String MSG_EFAMILY_CONFIG_KEY = "efamily_config_key";
    private static final String MSG_EFAMILY_WORDSLIST_KEY = "efamily_wordslist_key";
    private static final String CID_RELAYMASKINFO_KEY = "cid_relaymaskinfo_key";
    private static final String MSG_DOORBELLLIS_KEY = "msg_doorbelllis_key";
    private static final String MSG_DOORBELL_LOCATION_KEY = "msg_doorbell_location_key";
    private static final String MSG_EFAMILY_HOME_LIST_KEY = "msg_efamily_home_list_key";
    private static final String MSG_MAGNETIC_LIST_KEY = "msg_magnetic_list_key";


    public static String getCID_LIST_KEY() {
        return getKey(CID_LIST_KEY);
    }

    public static String getADD_DEVICE_CACHE() {
        return getKey(ADD_DEVICE_CACHE);
    }

    public static String getMSG_CENTER_KEY() {
        return getKey(MSG_CENTER_KEY);
    }

    public static String getMSG_DETAIL_KEY(String cid) {
        return getCidKey(cid, MSG_DETAIL_KEY);
    }

    public static String getMSG_ACCOUNT_KEY() {
        return getKey(MSG_ACCOUNT_KEY);
    }

    public static String getMSG_EFAMILY_KEY(String cid) {
        return getCidKey(cid, MSG_EFAMILY_KEY);
    }

    public static String getMSG_VIDEO_CONFIG_KEY(String cid) {
        return getCidKey(cid,MSG_VIDEO_CONFIG_KEY);
    }

    public static String getMSG_EFAMILY_CONFIG_KEY(String cid) {
        return getCidKey(cid,MSG_EFAMILY_CONFIG_KEY);
    }

    public static String getMSG_EFAMILY_WORDSLIST_KEY(String cid) {
        return getCidKey(cid,MSG_EFAMILY_WORDSLIST_KEY);
    }

    public static String getCID_RELAYMASKINFO_KEY(String cid) {
        return getCidKey(cid,CID_RELAYMASKINFO_KEY);
    }

    public static String getMSG_DOORBELLLIS_KEY(String cid) {
        return getCidKey(cid,MSG_DOORBELLLIS_KEY);
    }

    public static String getMsg_DOORBELL_LOCATION_KEY(String cid) {
        return getCidKey(cid,MSG_DOORBELL_LOCATION_KEY);
    }

    public static String getMsgEfamilyHomeListKey(String cid) {
        return getCidKey(cid,MSG_EFAMILY_HOME_LIST_KEY);
    }

    public static String getMsg_MagneticList_Key(String cid){
        return getCidKey(cid,MSG_MAGNETIC_LIST_KEY);
    }

    public static void saveObject(Serializable serializable, String key) {
        ACache mCache = ACache.get(MyApp.getContext());
        mCache.put(key, serializable);
    }

    public static Serializable readObject(String key) {
        ACache mCache = ACache.get(MyApp.getContext());
        return (Serializable) mCache.getAsObject(key);
    }

    public static void remove(String key) {
        ACache mCache = ACache.get(MyApp.getContext());
        mCache.remove(key);
    }

    public static void clear() {
        ACache mCache = ACache.get(MyApp.getContext());
        mCache.clear();
    }

    public static String getKey(String key) {
        StringBuilder builder = new StringBuilder();
        return builder.append(PreferenceUtil.getBindingPhone(MyApp.getContext()))
                .append("_")
                .append(key).toString();
    }

    public static String getCidKey(String cid, String key) {
        StringBuilder builder = new StringBuilder();
        return builder.append(PreferenceUtil.getBindingPhone(MyApp.getContext()))
                .append("_")
                .append(cid)
                .append("_")
                .append(key).toString();
    }
}
