package com.cylan.jiafeigou.support;

import android.content.Context;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.utils.CloseUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by hds on 17-3-30.
 */

public class OptionsImpl {
    private static final String TAG = "iDebugOptions";
    private static final String filePath = JConstant.WORKER_PATH + File.separator + "config.txt";
    private static JsonObject configContent;

    static {
        try {
            load();
        } catch (Exception e) {
        }
    }

    private OptionsImpl() {
    }

    public static void enableCrashHandler(Context context, String dir) {
        Log.d("iDebugOptions", "enableCrashHandler");
        CrashHandler.getInstance().init(context, dir);
    }

    public static void enableStrictMode() {
        Log.d("iDebugOptions", "enableStrictMode");
        StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder()).detectDiskReads().detectDiskWrites().detectNetwork().detectAll().penaltyLog().penaltyDialog().build());
    }


    public static String getServer() {
        try {
            if (configContent != null && configContent.has("server")) {
                String configServer = configContent.get("server").getAsString();
                if (!TextUtils.isEmpty(configServer)) return configServer;
            }
            String server = PackageUtils.getMetaString(ContextUtils.getContext(), "server");
            if (!BuildConfig.DEBUG) {
                return server;
            }
            if (!new File(filePath).exists()) return server;
            load();
            if (configContent != null && configContent.has("server")) {
                String configServer = configContent.get("server").getAsString();
                if (!TextUtils.isEmpty(configServer)) return configServer;
            }
            return server;
        } catch (IOException e) {
            Log.d("IOException", ":" + e.getLocalizedMessage());
            return "";
        }
    }

    private static void load() throws FileNotFoundException {
        if (!new File(filePath).exists()) return;
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        JsonParser parser = new JsonParser();
        configContent = parser.parse(br).getAsJsonObject();
        CloseUtils.close(br);
    }

    public static String getVKey() {
        String vkey = PackageUtils.getMetaString(ContextUtils.getContext(), "vKey");
        if (!BuildConfig.DEBUG) {
            return vkey;
        }
        if (configContent != null && configContent.has("vkey")) {
            return configContent.get("vkey").getAsString();
        }
        return vkey;
    }

    public static String getVid() {
        String vid = PackageUtils.getMetaString(ContextUtils.getContext(), "vId");
        if (!BuildConfig.DEBUG) {
            return vid;
        }
        if (configContent != null && configContent.has("vid")) {
            return configContent.get("vid").getAsString();
        }
        return vid;
    }

}