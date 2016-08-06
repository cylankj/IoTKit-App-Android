package com.cylan.jiafeigou.utils;

import android.content.Context;

import net.grandcentrix.tray.AppPreferences;

/**
 * Created by cylan-hunt on 16-8-6.
 */
public class SuperSpUtils {
    // create a preference accessor. This is for global app preferences.
    private AppPreferences appPreferences;

    private static SuperSpUtils superSpUtils;

    public static SuperSpUtils getInstance(Context context) {
        if (superSpUtils == null)
            superSpUtils = new SuperSpUtils(context);
        return superSpUtils;
    }

    private SuperSpUtils(Context context) {
        appPreferences = new AppPreferences(context); // this Preference comes for free from the library
    }

    public AppPreferences getAppPreferences() {
        return appPreferences;
    }
}
