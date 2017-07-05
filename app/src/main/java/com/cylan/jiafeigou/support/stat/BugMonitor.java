package com.cylan.jiafeigou.support.stat;

import android.content.Context;
import android.util.Log;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by hunt on 16-5-20.
 */
public class BugMonitor {

    public static void init(Context context) {
        try {
            CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
            strategy.setAppChannel(PackageUtils.getMetaString(context, "BUGLY_APP_CHANNEL"));
            strategy.setAppVersion(PackageUtils.getAppVersionName(context));
            strategy.setAppPackageName(context.getPackageName());
            strategy.setAppReportDelay(20000);//改为20s
            //...在这里设置strategy的属性,在bugly初始化时传入
            //...
            String appId = PackageUtils.getMetaString(context, "BUGLY_APPID");
            Log.d("BugMonitor", "BugMonitor: " + appId + "," + Thread.currentThread().getName());
            CrashReport.initCrashReport(context, appId, BuildConfig.DEBUG, strategy);
        } catch (Exception e) {

        }
    }
}
