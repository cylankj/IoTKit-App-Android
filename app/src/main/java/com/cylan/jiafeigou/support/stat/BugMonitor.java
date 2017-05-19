package com.cylan.jiafeigou.support.stat;

import android.content.Context;

import com.cylan.jiafeigou.BuildConfig;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by hunt on 16-5-20.
 */
public class BugMonitor {

    public static void init(Context context) {
        Bugly.enable = BuildConfig.DEBUG;
        CrashReport.initCrashReport(context);
    }
}
