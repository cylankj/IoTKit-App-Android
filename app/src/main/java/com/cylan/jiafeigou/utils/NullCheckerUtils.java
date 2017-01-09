package com.cylan.jiafeigou.utils;

import com.cylan.jiafeigou.BuildConfig;

/**
 * Created by cylan-hunt on 16-7-8.
 */
public class NullCheckerUtils {

    public static void checkObject(Object o) {
        if (o == null && BuildConfig.DEBUG)
            throw new NullPointerException("this object is null");
    }
}
