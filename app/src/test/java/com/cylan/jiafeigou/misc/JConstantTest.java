package com.cylan.jiafeigou.misc;

import org.junit.Test;

import java.util.Locale;

/**
 * Created by hds on 17-4-14.
 */
public class JConstantTest {


    @Test
    public void testUrl() {
        String id = "VRJz6f";
        String packageName = "hell";
        System.out.println(assembleUrl(id, packageName));
    }

    private static final String VERSION_URL = "http://yun.app8h.com/app?act=check_version&id=%s&platform=androidPhone&appid=%s";

    public static String assembleUrl(String id, String packageName) {
        return String.format(Locale.getDefault(), VERSION_URL, id, packageName);
    }
}