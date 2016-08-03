package com.cylan.jiafeigou.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by cylan-hunt on 16-7-4.
 */
public class TimeUtils {

    public static SimpleDateFormat simpleDateFormat_1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static String getDateStyle_0(final long time) {
        return simpleDateFormat_1.format(new Date(time));
    }
//    public static void main(String[] args) {
//        System.out.println(getDayMiddleNight(System.currentTimeMillis()));
//    }


    /**
     * 获取当天0点时间戳
     *
     * @return
     */
    public static long getTodayStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }
}
