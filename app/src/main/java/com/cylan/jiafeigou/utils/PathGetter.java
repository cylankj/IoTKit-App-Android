package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.os.Environment;

import com.cylan.publicApi.Constants;
import com.cylan.jiafeigou.engine.ClientConstants;

import java.io.File;

public class PathGetter {


    public static String getRootDirName() {
        return "/" + Constants.ROOT_DIR + "/";
    }

    public static String mkdirs(String dir) {
        File f = android.os.Environment.getExternalStorageDirectory();
        String path = f.getAbsolutePath() + getRootDirName() + dir + "/";
        File cache = new File(path);
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return path;
    }

    public static String getDataPath(Context ctx) {
        return Environment.getDataDirectory() + "/data/" + ctx.getPackageName();
    }


    public static String mkDataDirs(Context ctx, String dir) {
        String path = getDataPath(ctx) + "/" + dir + "/";
        File cache = new File(path);
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return path;
    }

    public static String getCoverPath(Context ctx, String cid) {
        return getSkinsPath(ctx) + "." + PreferenceUtil.getBindingPhone(ctx) + "-" + cid + ".png";
    }

    public static String getScreenShotPath() {
        return mkdirs("ScreenShot");
    }

    public static String getBreakPadPath() {
        return mkdirs("breakpad");
    }

    public static String getWslogPath() {
        return mkdirs("wslog");
    }

    public static String getCrashPath() {
        return mkdirs("crash");
    }

    public static String getImgPath() {
        return mkdirs("IMG");
    }


    public static String getSmartCallPath() {
        return mkdirs("smartcall");
    }


    public static String getRootPath() {
        File f = android.os.Environment.getExternalStorageDirectory();
        String path = f.getAbsolutePath() + getRootDirName() +"/";
        File cache = new File(path);
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return path;
    }

    public static String getJiaFeiGouPhotos() {
        return mkdirs(OEMConf.getOEM().equals(OEMConf.UseDefaultOEM) ? "Cleverdog" : "DoBy");
    }

    public static String getSpecialPicPath(Context ctx) {
        return getScreenShotPath() + "." + PreferenceUtil.getBindingPhone(ctx) + "-" + "undeter.png";
    }

    public static String getThemePicPath(Context ctx) {
        return getScreenShotPath() + "." + PreferenceUtil.getBindingPhone(ctx) + ".png";
    }

    public static String getSkinsPath(Context ctx) {
        return mkDataDirs(ctx, "skins");
    }

    public static String getUpgradePath() {
        return mkdirs("Upgrade");
    }

    public static String getBgTitleBarPath(Context ctx) {
        return getSkinsPath(ctx) + PreferenceUtil.getBindingPhone(ctx) + "-" + ClientConstants.TITLE_BAR + ".png";
    }


    public static String getRecordAudioDirPath(Context ctx, String cid) {
        String path = mkdirs("RecordAudio") + PreferenceUtil.getBindingPhone(ctx) + "/" + cid + "/";
        Utils.isPathValid(path);
        return path;
    }

    public static final String FILE_SUFFIX = ".wav";

    public static String getRecordAudioPath(Context ctx, String cid, String name) {
        return getRecordAudioDirPath(ctx, cid) + name;//FILE_SUFFIX
    }



}
