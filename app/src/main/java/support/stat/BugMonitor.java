package support.stat;

import android.content.Context;

import com.cylan.jiafeigou.BuildConfig;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by hunt on 16-5-20.
 */
public class BugMonitor {
    /**
     * 这里的Scene_tag是根据bugly平台上生成的标签的id.
     */
    public static final int SCENE_DOORBELLACTIVITY = 15544;

    public static void init(Context context) {
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setAppChannel(BuildConfig.DEBUG ? "DEBUG" : "OFFICIAL_RELEASE");
        CrashReport.initCrashReport(context, "900026046", false, strategy);
        com.tencent.bugly.Bugly.enable = BuildConfig.DEBUG;
    }
}
