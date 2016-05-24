package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.os.Environment;

import com.cylan.publicApi.Constants;

import java.io.File;

public class PathGetter {


    public static String getRootDirName() {
        return File.separator + Constants.ROOT_DIR + File.separator;
    }

    public static String mkdirs(String dir) {
        File f = android.os.Environment.getExternalStorageDirectory();
        String path = f.getAbsolutePath() + getRootDirName() + dir + File.separator;
        File cache = new File(path);
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return path;
    }

    public static String getDataPath(Context ctx) {
        return Environment.getDataDirectory() + File.separator + "data" + File.separator + ctx.getPackageName();
    }


    public static String mkDataDirs(Context ctx, String dir) {
        String path = getDataPath(ctx) + File.separator + dir + File.separator;
        File cache = new File(path);
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return path;
    }


    public static String getWSLogPath() {
        return mkdirs("log");
    }


    public static String getSmartCallPath() {
        return mkdirs("smartcall");
    }


}