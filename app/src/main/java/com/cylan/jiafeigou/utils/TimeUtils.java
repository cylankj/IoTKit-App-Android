package com.cylan.jiafeigou.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by cylan-hunt on 16-7-4.
 */
public class TimeUtils {


    private static final ThreadLocal<SimpleDateFormat> getSimpleDateFormat_1 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
        }
    };

    private static final ThreadLocal<SimpleDateFormat> getSimpleDateFormatVideo = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM.dd-HH:mm", Locale.getDefault());
        }
    };

    private static final ThreadLocal<SimpleDateFormat> getSimpleDateFormatHHMM = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm", Locale.getDefault());
        }
    };

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

    public static String getMediaPicTimeInString(final long time) {
        return getSimpleDateFormat_1.get().format(new Date(time));
    }


    public static String getMediaVideoTimeInString(final long time) {
        return getSimpleDateFormatVideo.get().format(new Date(time));
    }

    public static String getTodayString() {
        return new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                .format(new Date(System.currentTimeMillis()));
    }

    public static String getDayString(long time) {
        return new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                .format(new Date(time));
    }

    public static String getHH_MM(long time) {
        return getSimpleDateFormatHHMM.get().format(new Date(time));
    }
}
