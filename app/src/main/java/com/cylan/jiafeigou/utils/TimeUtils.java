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
    private static final SimpleDateFormat simpleTestDataFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.getDefault());

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

    private static final SimpleDateFormat getSimpleDateFormat_1 = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());

    public static String getMediaPicTimeInString(final long time) {
        return getSimpleDateFormat_1.format(new Date(time));
    }

    private static final SimpleDateFormat getSimpleDateFormatVideo = new SimpleDateFormat("MM.dd-HH:mm", Locale.getDefault());

    public static String getMediaVideoTimeInString(final long time) {
        return getSimpleDateFormatVideo.format(new Date(time));
    }

    public static String getTodayString() {
        return new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                .format(new Date(System.currentTimeMillis()));
    }

    public static String getDayString(long time) {
        return new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                .format(time);
    }

    public static String getTestTime(long time) {
        return simpleTestDataFormat.format(new Date(time));
    }
}
