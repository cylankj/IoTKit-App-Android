package com.cylan.jiafeigou.n.mvp.model;

import java.util.Locale;

/**
 * Created by hunt on 16-6-2.
 */
public class GreetBean {
    public String nickName;
    public String poet;

    public static String formatGreet(String nickName, String prefix) {
        return String.format(Locale.getDefault(), prefix, nickName);
    }
}
