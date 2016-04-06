package com.cylan.publicApi;

public class CheckUpdateUrl {

    public static final String IPAD = "iPad";
    public static final String IPHONE = "iPhone";
    public static final String ANDROIDPAD = "androidPad";
    public static final String ANDROIDPHONE = "androidPhone";

    public static String getCheckUpdateUrl(String host, String id, String device, String packname) {
        DswLog.d(host + "/app?act=check_version&id=" + id + "&platform=" + device
                + "&appid=" + packname);
        return host + "/app?act=check_version&id=" + id + "&platform=" + device + "&appid="
                + packname;
    }
}
