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
            String server = PreferencesUtils.getString(KEY_SERVER, "");
            if (!TextUtils.isEmpty(server)) return server;
            // com.mcxiaoke.packer.helper.PackerNg
            final String domain = PackerNg.getMarket(ContextUtils.getContext());
            if (!TextUtils.isEmpty(domain)) {
                Log.d(TAG, "get serverFrom ng: " + domain.trim());
                return domain.trim();
            }
            server = PackageUtils.getMetaString(ContextUtils.getContext(), "server").trim();
            if (!BuildConfig.DEBUG) {
                return server;
            }
            return server;
        } catch (Exception e) {
            Log.d("IOException", ":" + e.getLocalizedMessage());
            return "";
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