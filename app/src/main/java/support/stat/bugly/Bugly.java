package support.stat.bugly;

import android.content.Context;
import android.util.Log;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by hunt on 16-4-27.
 */
public class Bugly {

    public static void init(Context context, boolean debug, final String appVersion) {
        CrashReport.UserStrategy userStrategy = new CrashReport.UserStrategy(context);
        userStrategy.setAppChannel("official");
        userStrategy.setAppVersion(appVersion);
        CrashReport.initCrashReport(context, "J5DWTk0tvyDYhJVw", true, userStrategy);
        com.tencent.bugly.Bugly.enable = debug;
        Log.d("Bugly", "Bugly: " + CrashReport.getAppChannel());
        Log.d("Bugly", "Bugly: " + CrashReport.getAppID());
        Log.d("Bugly", "Bugly: " + CrashReport.getAppVer());
    }
}
