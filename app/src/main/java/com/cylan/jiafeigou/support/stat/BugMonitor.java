package com.cylan.jiafeigou.support.stat;

import android.content.Context;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by hunt on 16-5-20.
 */
public class BugMonitor {

    public static void init(Context context) {
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setAppChannel(BuildConfig.DEBUG ? "DEBUG" : "CLEVER_DOG");
        CrashReport.initCrashReport(context, strategy);
        com.tencent.bugly.Bugly.enable = BuildConfig.DEBUG;
    }
}
