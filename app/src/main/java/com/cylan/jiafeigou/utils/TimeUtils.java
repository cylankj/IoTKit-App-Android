package com.cylan.jiafeigou.utils;

import android.content.Context;

import com.cylan.jiafeigou.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by cylan-hunt on 16-7-4.
 */
public class TimeUtils {

    public static SimpleDateFormat simpleDateFormat_1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat simpleTestDataFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.getDefault());

    private static final ThreadLocal<SimpleDateFormat> getSimpleDateFormat_1 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
        }
    };

    private static final ThreadLocal<SimpleDateFormat> sSimpleDateFormatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat();
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

    private static final ThreadLocal<SimpleDateFormat> getSimpleDateFormatYYYYHHMM = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yy/MM/dd", Locale.getDefault());
        }
    };

    /**
     * 摄像头消息顶部时间显示格式
     */
    private static final ThreadLocal<SimpleDateFormat> getDateFormatSuper = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM-dd", Locale.getDefault());
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

    public static long getTodayEndTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTimeInMillis();
    }

    public static long getSpecificDayEndTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTimeInMillis();
    }

    public static long getSpecificDayStartTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
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

    public static String getUptime(long time) {
        if (time == 0) return ContextUtils.getContext().getString(R.string.STANBY_TIME_M, 0);
        time = System.currentTimeMillis() / 1000 - time;
        int temp = (int) time / 60;
        int minute = temp % 60;
        temp = temp / 60;
        int hour = temp % 24;
        temp = temp / 24;
        int day = temp;
        if (day > 0 && hour > 0) {
            return ContextUtils.getContext().getString(R.string.STANBY_TIME_D_H_M, day, hour, minute);
        } else if (hour > 0) {
            return ContextUtils.getContext().getString(R.string.STANBY_TIME_H_M, hour, minute);
        } else {
            return ContextUtils.getContext().getString(R.string.STANBY_TIME_M, minute);
        }
    }

    public static String getDayInMonth(long time) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(time);
        int i = instance.get(Calendar.DAY_OF_MONTH);
        return i + "";
    }

    public static String getMM_DD(long time) {
        return new SimpleDateFormat("MM月dd日", Locale.getDefault())
                .format(new Date(time));
    }

    public static String getHH_MM(long time) {
        return getSimpleDateFormatHHMM.get().format(new Date(time));
    }

    public static String getHH_MM_Remain(long timeMs) {
        int totalMinutes = (int) (timeMs / 1000 / 60);
        int minutes = totalMinutes % 60;
        int hours = totalMinutes / 60;
        String str_hour = hours > 9 ? "" + hours : "0" + hours;
        String str_minute = minutes > 9 ? "" + minutes : "0" + minutes;
        return str_hour + ":" + str_minute;
    }

    public static String getTestTime(long time) {
        return simpleTestDataFormat.format(new Date(time));
    }

//    public static long getTimeStart(long time) {
//        return (time / 3600 / 24 / 1000) * 1000L * 3600 * 24;
//    }

    public static String getSpecifiedDate(long time) {
        return simpleDateFormat_1.format(new Date(time));
    }

    public static long startOfDay(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        cal.set(Calendar.MINUTE, 0); // set minutes to zero
        cal.set(Calendar.SECOND, 0); //set seconds to zero
        return cal.getTimeInMillis();
    }


    public static final SimpleDateFormat simpleDateFormat0;
    public static final SimpleDateFormat simpleDateFormat1;
    public static final SimpleDateFormat simpleDateFormat2;

    /**
     * 如果改变系统时区,app没有重启,就不能同步更新了.
     */
    static {
        simpleDateFormat0 = new SimpleDateFormat("MM/dd HH:mm",
                Locale.getDefault());
        simpleDateFormat1 = new SimpleDateFormat("yyyyMMdd",
                Locale.getDefault());
        simpleDateFormat2 = new SimpleDateFormat("HH:mm",
                Locale.getDefault());
        simpleDateFormat0.setTimeZone(TimeZone.getTimeZone("GMT"));
        simpleDateFormat1.setTimeZone(TimeZone.getTimeZone("GMT"));
        simpleDateFormat2.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static boolean isToday(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(new Date(System.currentTimeMillis()));
        return calendar.get(Calendar.YEAR) == calendar1.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == calendar1.get(Calendar.DAY_OF_YEAR);
    }

    public static String getSuperString(long time) {
        return getDateFormatSuper.get().format(new Date(time));
    }

    public static String getHomeItemTime(Context context, long time) {
        if (time == 0) return "";
        if (System.currentTimeMillis() - time <= 5 * 60 * 1000L)
            return context.getString(R.string.JUST_NOW);
        if (startOfDay(System.currentTimeMillis()) < time)//今天的早些时候
            return getSimpleDateFormatHHMM.get().format(new Date(time));
        return getSimpleDateFormatYYYYHHMM.get().format(new Date(time));
    }

    public static String getMonthInYear(long time) {
        SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.FULL);
        format.applyPattern("MMMM");
        return format.format(new Date(time));
    }


    public static String getBellRecordTime(long time) {
        Date today = new Date();
        Date provide = new Date(time);
        SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getInstance();
        if (today.getYear() > provide.getYear()) {//说明不是今年，则按照其他显示年.月.日显示
            format.applyPattern("yyyy.MM.dd");
            return format.format(provide);
        }
        if (today.getMonth() == provide.getMonth()) {
            if (today.getDay() == provide.getDay()) {//说明是在同一天，则按照今天 时:分显示
//                format.applyPattern("今天");
                return ContextUtils.getContext().getString(R.string.DOOR_TODAY);
            }
            if (today.getDay() - provide.getDay() == 1) {//说明是在昨天，则按照昨天 时:分显示
                return ContextUtils.getContext().getString(R.string.Yesterday);
            }
        }
        //按照月.日 时：分显示
        format.applyPattern("MM.dd");
        return format.format(provide);
    }
}
