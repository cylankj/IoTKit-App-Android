package com.cylan.jiafeigou.utils;

import android.content.Context;

import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.IOException;
import java.io.InputStream;

;

public class OEMConf {

    public final static String KEY_OEM = "OEM";
    public final static String KEY_QQ_LOGIN = "QQ_Login";
    public final static String KEY_XL_LOGIN = "XL_Login";
    public final static String KEY_SHOW_MODEL = "Model";
    public final static String KEY_COPYRIGHT = "CopyRight";
    public final static String KEY_WEB = "WEB";
    public final static String KEY_SERVER_TEL = "SERVICE_TEL";
    public final static String KEY_TREAY_URL = "TREAY_URL";

    public static String UseDefaultOEM = "cylan";

    public static String UseOEM = "cylan";
    public static boolean UseDefaultQQLogin = false;
    public static boolean UseDefaultXLLogin = false;
    public static boolean UseDefaultShowModel = false;
    public static boolean UseDefaultCopyright = false;
    public static boolean UseDefaultWeb = false;
    public static boolean UseDefaultServerTel = false;
    public static String UseDefaultTreayUrl = "http://www.jfgou.com/app/treaty.html";

    private static boolean hasInit = false;

    public static void LoadConf(Context context) {
        if (hasInit) {
            return;
        }
        InputStream is = null;
        try {
            is = context.getAssets().open("config.ini");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            Parser(new String(buffer, "UTF-8"));

        } catch (IOException e) {
            AppLogger.e(e.toString());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    AppLogger.e(e.toString());
                }
            }
        }
        hasInit = true;
    }

    private static String getString(String text) {
        return text.replace("\n", "").trim();
    }

    private static boolean getBoolean(String text) {
        return text.replace("\n", "").trim().compareTo("1") == 0;
    }

    private static int getInt(String text) {
        return Integer.parseInt(text.replace("\n", "").trim());
    }

    private static void Parser(String text) {
        String[] separated = text.split("\n");
        String[] sepas;
        String key;
        for (int i = 0; i < separated.length; i++) {
            if (separated[i].startsWith("#") == false) {
                sepas = separated[i].split("=");
                key = sepas[0].trim();
                if (key.compareTo(KEY_OEM) == 0) {
                    UseOEM = getString(sepas[1]);
                } else if (key.compareTo(KEY_QQ_LOGIN) == 0) {
                    UseDefaultQQLogin = getBoolean(sepas[1]);
                } else if (key.compareTo(KEY_XL_LOGIN) == 0) {
                    UseDefaultXLLogin = getBoolean(sepas[1]);
                } else if (key.compareTo(KEY_SHOW_MODEL) == 0) {
                    UseDefaultShowModel = getBoolean(sepas[1]);
                } else if (key.compareTo(KEY_COPYRIGHT) == 0) {
                    UseDefaultCopyright = getBoolean(sepas[1]);
                } else if (key.compareTo(KEY_WEB) == 0) {
                    UseDefaultWeb = getBoolean(sepas[1]);
                } else if (key.compareTo(KEY_SERVER_TEL) == 0) {
                    UseDefaultServerTel = getBoolean(sepas[1]);
                } else if (key.compareTo(KEY_TREAY_URL) == 0) {
                    UseDefaultTreayUrl = getString(sepas[1]);
                }

            }
        }
    }

    public static String getOEM() {
        return UseOEM;
    }

    public static boolean showQQLogin() {
        return UseDefaultQQLogin;
    }

    public static boolean showXLLogin() {
        return UseDefaultXLLogin;
    }

    public static boolean showModel() {
        return UseDefaultShowModel;
    }

    public static boolean showCopyright() {
        return UseDefaultCopyright;
    }

    public static boolean showWeb() {
        return UseDefaultWeb;
    }

    public static boolean showServelTel() {
        return UseDefaultServerTel;
    }

    public static String showTreayUrl() {
        return UseDefaultTreayUrl;
    }
}