package com.cylan.jiafeigou.support;

import android.content.Context;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by hds on 17-3-30.
 */

public class OptionsImpl {
    private static final String TAG = "iDebugOptions";
    private static String server;
    private static final String filePath = JConstant.LOG_PATH + File.separator + "config.txt";

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
        if (!TextUtils.isEmpty(server)) return server;
        try {
            server = Security.getServerPrefix(JFGRules.getTrimPackageName()) + ".jfgou.com:443";
            if (!BuildConfig.DEBUG) {
                return server;
            }
            if (!new File(filePath).exists()) return server;
            FileInputStream fStream = new FileInputStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fStream));
            String strLine = "";
            String content = "";
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                content = strLine;
                Log.d("getServer", "getServer:" + strLine);
            }
            //Close the input stream
            br.close();
            fStream.close();
            return content;
        } catch (IOException e) {
            Log.d("IOException", ":" + e.getLocalizedMessage());
            return "";
        }
    }
}