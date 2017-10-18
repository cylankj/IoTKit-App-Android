//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.cylan.jiafeigou.support;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class CrashHandler implements UncaughtExceptionHandler {
    private static CrashHandler INSTANCE = new CrashHandler();
    private Context mContext;
    private UncaughtExceptionHandler mDefaultHandler;
    private Map<String, String> infoMap = new HashMap();
    private DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    private static String path = "";

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            Class var0 = CrashHandler.class;
            Class var1 = CrashHandler.class;
            synchronized (CrashHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CrashHandler();
                }
            }
        }

        return INSTANCE;
    }

    private static void setPath(String dir) {
        path = dir;
    }

    public void init(Context context, String dir) {
        if (TextUtils.isEmpty(dir)) {
            throw new NullPointerException("you must define crash dir");
        } else {
            setPath(dir);
            this.mContext = context;
            this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            if (!this.handleException(ex) && this.mDefaultHandler != null) {
                this.mDefaultHandler.uncaughtException(thread, ex);
            } else {
                SystemClock.sleep(3000L);
                Process.killProcess(Process.myPid());
                System.exit(1);
            }
        } catch (UnsupportedEncodingException var4) {
            var4.printStackTrace();
        }

    }

    private boolean handleException(Throwable ex) throws UnsupportedEncodingException {
        if (ex == null) {
            return false;
        } else {
            this.collectDeviceInfo(this.mContext);
            this.saveCrashInfo2File(ex);
            return true;
        }
    }

    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager var10 = ctx.getPackageManager();
            PackageInfo var11 = var10.getPackageInfo(ctx.getPackageName(), 1);
            if (var11 != null) {
                String var12 = var11.versionName == null ? "null" : var11.versionName;
                String var13 = var11.versionCode + "";
                this.infoMap.put("versionName", var12);
                this.infoMap.put("versionCode", var13);
            }
        } catch (NameNotFoundException var9) {
            ;
        }

        Field[] var101 = Build.class.getDeclaredFields();
        Field[] var111 = var101;
        int var121 = var101.length;

        for (int var131 = 0; var131 < var121; ++var131) {
            Field field = var111[var131];

            try {
                field.setAccessible(true);
                this.infoMap.put(field.getName(), field.get((Object) null).toString());
            } catch (Exception var8) {
                ;
            }
        }

    }

    private String saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        Iterator writer = this.infoMap.entrySet().iterator();

        String result;
        while (writer.hasNext()) {
            Entry var14 = (Entry) writer.next();
            String var15 = (String) var14.getKey();
            result = (String) var14.getValue();
            sb.append(var15 + "=" + result + "\n");
        }

        StringWriter var141 = new StringWriter();
        PrintWriter var151 = new PrintWriter(var141);
        ex.printStackTrace(var151);

        for (Throwable var13 = ex.getCause(); var13 != null; var13 = var13.getCause()) {
            var13.printStackTrace(var151);
        }

        var151.close();
        result = var141.toString();
        sb.append(result);

        try {
            String var16 = this.formatter.format(Long.valueOf(System.currentTimeMillis()));
            String fileName = var16 + "_crash.txt";
            if (Environment.getExternalStorageState().equals("mounted")) {
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                FileOutputStream fos = new FileOutputStream(path + "/" + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
                File[] file = dir.listFiles();
                if (file.length > 20) {
                    Arrays.sort(file, new Comparator<File>() {
                        @Override
                        public int compare(File file, File t1) {
                            return file.lastModified() < t1.lastModified() ? -1 : (file.lastModified() > t1.lastModified() ? 1 : 0);
                        }
                    });

                    for (int i = 0; i < file.length / 4; ++i) {
                        file[i].delete();
                    }
                }
            }

            return fileName;
        } catch (Exception var131) {
            var131.printStackTrace();
            return null;
        }
    }
}
