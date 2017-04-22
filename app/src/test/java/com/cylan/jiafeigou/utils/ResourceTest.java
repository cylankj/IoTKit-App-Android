package com.cylan.jiafeigou.utils;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hds on 17-4-21.
 */

public class ResourceTest {

    @Test
    public void test() {
        String pattern = "Yesterday HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        simpleDateFormat.format(new Date(System.currentTimeMillis()));
    }
}
