package com.cylan.jiafeigou.support;

import android.content.Context;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.mcxiaoke.packer.helper.PackerNg;

/**
 * Created by hds on 17-3-30.
 */

public class OptionsImpl {
    private static final String TAG = "iDebugOptions";
    private static final String KEY_SERVER = "server";
    private static final String KEY_ROBOT_SERVER = "key_robot_server";
    private static String server;
    private static String robotServer;

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

    public static void setServer(String server) {
        PreferencesUtils.putString(KEY_SERVER, server);
        OptionsImpl.server = server;
    }

    /**
     * 1.debug环境下,先从配置文件中读取.
     * 2.从渠道配置信息中读取
     * 3.从Manifest中读取
     *
     * @return
     */
    public static String getServer() {
        try {
            if (!TextUtils.isEmpty(OptionsImpl.server)) {
                return OptionsImpl.server.replace("_", ":");
            }
            String server = PreferencesUtils.getString(KEY_SERVER, "");
            if (!TextUtils.isEmpty(server)) {
                return OptionsImpl.server = server.replace("_", ":");
            }
            // com.mcxiaoke.packer.helper.PackerNg
            final String domain = PackerNg.getChannel(ContextUtils.getContext());
            if (!TextUtils.isEmpty(domain)) {
                OptionsImpl.server = domain.trim();
                Log.d(TAG, "get serverFrom ng: " + OptionsImpl.server);
                PreferencesUtils.putString(KEY_SERVER, OptionsImpl.server);
                return OptionsImpl.server.replace("_", ":");
            }
            OptionsImpl.server = server = PackageUtils.getMetaString(ContextUtils.getContext(), "server").trim();
            if (!BuildConfig.DEBUG) {
                return server.replace("_", ":");
            }
            return server.replace("_", ":");
        } catch (Exception e) {
            Log.d("IOException", ":" + e.getLocalizedMessage());
            return "";
        }
    }

    /**
     * 1.debug环境下,先从配置文件中读取.
     * 2.从渠道配置信息中读取
     * 3.从Manifest中读取
     *
     * @return
     */
    public static String getRobotServer() {
        try {
            if (!TextUtils.isEmpty(OptionsImpl.robotServer)) {
                return OptionsImpl.robotServer.replace("_", ":");
            }
            String server = PreferencesUtils.getString(KEY_ROBOT_SERVER, "");
            if (!TextUtils.isEmpty(server)) {
                return OptionsImpl.robotServer = server.replace("_", ":");
            }
            // com.mcxiaoke.packer.helper.PackerNg
            final String domain = PackerNg.getChannel(ContextUtils.getContext());
            if (!TextUtils.isEmpty(domain)) {
                OptionsImpl.robotServer = domain.trim();
                Log.d(TAG, "get serverFrom ng: " + OptionsImpl.robotServer);
                PreferencesUtils.putString(KEY_ROBOT_SERVER, OptionsImpl.robotServer);
                return OptionsImpl.robotServer.replace("_", ":");
            }
            OptionsImpl.robotServer = server = PackageUtils.getMetaString(ContextUtils.getContext(), "robot_server").trim();
            if (!BuildConfig.DEBUG) {
                return server.replace("_", ":");
            }
            if (TextUtils.isEmpty(server)) {
                server = "yf.robotscloud.com";
            }
            return server.replace("_", ":");
        } catch (Exception e) {
            Log.d("IOException", ":" + e.getLocalizedMessage());
            return "yf.robotscloud.com";
        }
    }

    public static String getVKey() {
        String vkey = PackageUtils.getMetaString(ContextUtils.getContext(), "vKey");
        if (!BuildConfig.DEBUG) {
            return vkey;
        }
        return vkey;
    }

    public static String getVid() {
        String vid = PackageUtils.getMetaString(ContextUtils.getContext(), "vId");
        if (!BuildConfig.DEBUG) {
            return vid;
        }
        return vid;
    }
}