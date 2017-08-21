package com.cylan.jiafeigou.misc;

import java.util.HashMap;

/**
 * Created by hds on 17-8-21.
 */

public class MethodFilter {

    private static HashMap<String, Long> methodMap = new HashMap<>();

    public static boolean run(String methodName, long interval) {
        long lastTime = methodMap.get(methodName);
        if (lastTime == 0) {
            methodMap.put(methodName, System.currentTimeMillis());
            return true;
        }
        if (System.currentTimeMillis() - lastTime > interval) {
            methodMap.put(methodName, System.currentTimeMillis());
            return true;
        }
        return false;
    }
}
