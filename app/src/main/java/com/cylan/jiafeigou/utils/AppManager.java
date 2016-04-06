package com.cylan.jiafeigou.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import java.util.Stack;

public class AppManager {

    private Stack<Activity> activityStack;
    private static AppManager instance;

    private AppManager() {
    }

    /**
     * according to the element's name ,we can find the same activity on the top
     *
     * @param ActivityName
     * @return
     */
    public boolean isActivityTop(String ActivityName) {
        if (ActivityName == null)
            return false;
        if (activityStack == null)
            return false;
        if (activityStack.empty())
            return false;
        return activityStack.peek().getLocalClassName().equals(ActivityName);
    }

    public static AppManager getAppManager() {
        if (instance == null) {
            instance = new AppManager();
        }
        return instance;
    }

    public int size() {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        return activityStack.size();
    }

    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        final boolean hasThisActivity = activityStack.contains(activity);
        activityStack.add(activity);
    }

    public Activity currentActivity() {
        Activity activity = activityStack.lastElement();
        return activity;
    }

    public void finishActivity() {
        Activity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    public void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    public void finishActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    public void finishAllActivity() {
        if (activityStack != null) {
            for (int i = 0, size = activityStack.size(); i < size; i++) {
                if (null != activityStack.get(i)) {
                    activityStack.get(i).finish();
                }
            }
            activityStack.clear();
        }
    }

    public void finishAllOtherActivity(Activity activity) {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i) && !activityStack.get(i).getClass().getName().equals(activity.getClass().getName())) {
                activityStack.get(i).finish();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public void finishAllOtherActivity(Class clazz) {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i) && !activityStack.get(i).getClass().getName().equals(clazz.getName())) {
                activityStack.get(i).finish();
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void AppExit(Context context) {
        try {
            finishAllActivity();
            ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            activityMgr.restartPackage(context.getPackageName());
            System.exit(0);
        } catch (Exception e) {
        }
    }

    @SuppressWarnings("deprecation")
    public void AppExit() {
        try {
            finishAllActivity();
            ActivityManager activityMgr = (ActivityManager) instance.currentActivity().getSystemService(Context.ACTIVITY_SERVICE);
            activityMgr.restartPackage(instance.currentActivity().getPackageName());
            System.exit(0);
        } catch (Exception e) {
        }
    }
}