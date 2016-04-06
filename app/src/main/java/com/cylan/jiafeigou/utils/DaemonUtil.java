package com.cylan.jiafeigou.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Environment;

import com.cylan.publicApi.DswLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DaemonUtil {

    private DaemonUtil() {
    }

    public static DaemonUtil getInstance() {
        return Holder.instance;
    }

    private static class Holder {
        private static final DaemonUtil instance = new DaemonUtil();
    }

    @SuppressLint("NewApi")
    public void init(Context mContext, boolean isEnableLog, String serviceName) {
        try {
            String filename = "daemon_c";
            copyAssetFile(mContext, filename);
            String daemonPath = getDataFilePath(mContext) + filename;
            new File(daemonPath).setExecutable(true);
            String packName = getAppPackageName(mContext);
            String processName = getCurProcessName(mContext) + ":push";
            String logPath = PathGetter.getWslogPath();
            Process process = new ProcessBuilder()
                    .command(daemonPath, packName, processName, serviceName, isEnableLog ? "1" : "0", logPath).start();
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }

    }

    private void copyAssetFile(Context ctx, String filename) {
        AssetManager assetManager = ctx.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            String newFileName = getDataFilePath(ctx) + filename;
            if (!new File(newFileName).exists()) {
                new File(newFileName).delete();
            }
            in = assetManager.open(filename);
            out = new FileOutputStream(newFileName);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;

        } catch (Exception e) {
        }

    }

    private String getDataFilePath(Context ctx) {

        String path = Environment.getDataDirectory() + "/data/" + ctx.getPackageName() + "/files/";
        File cache = new File(path);
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return path;
    }

    private String getAppPackageName(Context context) {
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            DswLog.ex(e.toString());
        }
        return info.packageName;
    }

    private String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }


}
