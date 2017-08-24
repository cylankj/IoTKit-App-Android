package com.cylan.jiafeigou.utils;

import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.File;

public class PathGetter {

    public static String createPath(final String path) {
        File file = new File(path);
        if (!file.exists()) {
            boolean mk = file.mkdirs();
            AppLogger.w("mk: " + mk + " " + path);
        }
        return path;
    }

}