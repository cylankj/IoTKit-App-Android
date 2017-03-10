package com.cylan.jiafeigou.utils;

import android.content.Context;

import com.cylan.jiafeigou.misc.JConstant;

import java.util.Locale;

/**
 * Created by cylan-hunt on 16-10-24.
 */

public class LocaleUtils {
    private static final Locale LOCALE_HK = new Locale("zh", "HK");
    /**
     * 对应
     */
    private static final Locale[] CONST_LOCALE = {
            Locale.SIMPLIFIED_CHINESE,
            Locale.ENGLISH,
            new Locale("ru", "RU"),
            new Locale("pt", "BR"),
            new Locale("es", "ES"),
            Locale.JAPAN,
            Locale.FRANCE,
            Locale.GERMANY,
            Locale.ITALY,
            new Locale("tr", "TR"),
            Locale.TRADITIONAL_CHINESE};

    public static int getLanguageType(Context ctx) {
        if (ctx == null) return 0;
        Locale locale = ctx.getResources().getConfiguration().locale;
        final String c = locale.toString();
        if (c.contains("zh")) {
            if ((c.contains("TW") || c.contains("HK"))) {
                return JConstant.LOCALE_T_CN;
            } else return JConstant.LOCALE_SIMPLE_CN;//simple chinese
        }

        final int count = CONST_LOCALE.length;
        for (int i = 0; i < count; i++) {
            if (locale.equals(CONST_LOCALE[i]))
                return i;
        }
        return 1;
    }
}
