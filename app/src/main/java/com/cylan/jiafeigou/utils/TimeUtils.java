package com.cylan.jiafeigou.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by cylan-hunt on 16-7-4.
 */
public class TimeUtils {

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    public static SimpleDateFormat simpleDateFormat_1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * 获取某一天的零点时间戳。
     *
     * @param timeInMilli
     * @return
     */
    public static long getDayMiddleNight(final long timeInMilli) {
        try {
            final String d = simpleDateFormat.format(timeInMilli);
            System.out.println(d);
            Date date = simpleDateFormat.parse(d);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getDateStyle_0(final long time) {
        return simpleDateFormat_1.format(new Date(time));
    }
//    public static void main(String[] args) {
//        System.out.println(getDayMiddleNight(System.currentTimeMillis()));
//    }
}
