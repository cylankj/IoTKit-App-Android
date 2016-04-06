package com.cylan.jiafeigou.activity.main;

import com.cylan.jiafeigou.listener.ActivityIsResume;

/**
 * Created by HeBin on 2015/3/4.
 */
public class ActivityIsResumeManager {

    private static ActivityIsResume listener;

    public static void setActivityIsResumeListener(ActivityIsResume a) {
        listener = a;
    }

    public static ActivityIsResume getActivityIsResumeListener() {
        return listener;
    }
}
