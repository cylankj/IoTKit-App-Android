package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.text.format.DateFormat;

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
    public static final long DAY_TIME = 24 * 60 * 60 * 1000L;
    public static SimpleDateFormat simpleDateFormat_1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat simpleTestDataFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.getDefault());

    private static final ThreadLocal<SimpleDateFormat> getSimpleDateFormat_1 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
        }
    };
    private static final ThreadLocal<SimpleDateFormat> getSimpleDateFormat_2 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
        }
    };
    private static final ThreadLocal<SimpleDateFormat> getSimpleDateFormat_3 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        }
    };
    private static final ThreadLocal<SimpleDateFormat> getSimpleDateFormat_3_12 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy/MM/dd HH:mma", Locale.getDefault());
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

    private static final ThreadLocal<SimpleDateFormat> getSimpleMMSS = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("mm:ss", Locale.getDefault());
        }
    };

    public static String getMM_SS(long time) {
        long second = time / 1000;
        long sec = second % 60;
        long min = second / 60;
        String secText = sec >= 10 ? "" + sec : "0" + sec;
        String minText = min >= 10 ? "" + min : "0" + min;
        return minText + ":" + secText;
    }

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
    private static final ThreadLocal<SimpleDateFormat> SimpleDateFormatYYYYHHMM = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        }
    };

    public static String getYHM(long time) {
        return SimpleDateFormatYYYYHHMM.get().format(new Date(time));
    }

    /**
     * 历史录像,使用0时区
     */
    private static final ThreadLocal<SimpleDateFormat> historyDateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.UK);
            TimeZone zone = TimeZone.getTimeZone("Europe/London");
            format.setTimeZone(zone);
            return format;
        }
    };

    private static final ThreadLocal<SimpleDateFormat> historyDateFormat0 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
            return dateFormat;
        }
    };

    private static final ThreadLocal<SimpleDateFormat> liveDateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
        }
    };

    public static String getHistoryTime(long timeInLong) {
        return historyDateFormat.get().format(new Date(timeInLong));
    }

    public static String getHistoryTime1(long timeInLong) {
        return historyDateFormat0.get().format(new Date(timeInLong));
    }

    public static String getLiveTime(long timeInLong) {
        return liveDateFormat.get().format(new Date(timeInLong));
    }

    /**
     * 摄像头消息顶部时间显示格式
     */
    private static final ThreadLocal<SimpleDateFormat> getDateFormatSuper = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM-dd", Locale.getDefault());
        }
    };

    public static String getTimeSpecial(long time) {
        return getSimpleDateFormat_1.get().format(new Date(time));
    }

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
        boolean isSameYear = sameYear(System.currentTimeMillis(), time);

        if (isSameYear) {
            return getSimpleDateFormat_2.get().format(new Date(time));
        } else {
            return getSimpleDateFormat_3.get().format(new Date(time));
        }
    }

    public static String getYMDHM(final long time) {
        return getSimpleDateFormat_3.get().format(new Date(time));
    }

    private static boolean sameYear(long time1, long time2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(new Date(time1));
        cal2.setTime(new Date(time2));
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
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
        if (time == 0) {
            return ContextUtils.getContext().getString(R.string.STANBY_TIME, 0, 0, 0);
        }
        time = System.currentTimeMillis() / 1000 - time;
        int temp = (int) time / 60;
        int minute = temp % 60;
        temp = temp / 60;
        int hour = temp % 24;
        temp = temp / 24;
        int day = temp;
        return ContextUtils.getContext().getString(R.string.STANBY_TIME, day, hour, minute);
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

    public static String getDatePickFormat(long time, TimeZone timeZone) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd ", Locale.getDefault());
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(new Date(time));
    }

    public static int getWeekNum(long time, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.setTimeZone(timeZone);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static long startOfDay(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        cal.set(Calendar.MINUTE, 0); // set minutes to zero
        cal.set(Calendar.SECOND, 0); //set seconds to zero
        return cal.getTimeInMillis();
    }


    public static final SimpleDateFormat simpleDateFormat1;
    public static final SimpleDateFormat simpleDateFormat2;

    /**
     * 如果改变系统时区,app没有重启,就不能同步更新了.
     */
    static {
        simpleDateFormat1 = new SimpleDateFormat("yyyyMMdd",
                Locale.getDefault());
        simpleDateFormat2 = new SimpleDateFormat("HH:mm",
                Locale.getDefault());
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
        if (time == 0) {
            return "";
        }
        if (System.currentTimeMillis() - time <= 5 * 60 * 1000L)//五分钟内是刚刚
        {
            return context.getString(R.string.JUST_NOW);
        }
        if (startOfDay(System.currentTimeMillis()) < time)//今天的早些时候
        {
            return getSimpleDateFormatHHMM.get().format(new Date(time));
        }
        return getSimpleDateFormatYYYYHHMM.get().format(new Date(time));
    }

    public static String getDay(long time) {
        return getSimpleDateFormatYYYYHHMM.get().format(new Date(time));
    }

    public static String getMonthInYear(long time) {
        SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.FULL);
        format.applyPattern("MMMM");
        return format.format(new Date(time));
    }

    public static String getHHMMSS(long timeMs) {
        int totalSecound = (int) (timeMs / 1000);
        int second = totalSecound % 60;
        totalSecound = totalSecound / 60;
        int minutes = totalSecound % 60;
        int hours = totalSecound / 60;
        String str_hour = hours > 9 ? "" + hours : "0" + hours;
        String str_minute = minutes > 9 ? "" + minutes : "0" + minutes;
        String str_second = second > 9 ? "" + second : "0" + second;
        return str_hour + ":" + str_minute + ":" + str_second;
    }

    public static String AutoHHMMSS(long timeMs) {
        int totalSecound = (int) (timeMs / 1000);
        int second = totalSecound % 60;
        totalSecound = totalSecound / 60;
        int minutes = totalSecound % 60;
        int hours = totalSecound / 60;
        String str_hour = hours > 9 ? "" + hours + ":" : "0" + hours + ":";
        String str_minute = minutes > 9 ? "" + minutes + ":" : "0" + minutes + ":";
        String str_second = second > 9 ? "" + second : "0" + second;
        return str_hour + str_minute + str_second;
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
            if (today.getDate() == provide.getDate()) {//说明是在同一天，则按照今天 时:分显示
                return ContextUtils.getContext().getString(R.string.DOOR_TODAY);
            }
            if (today.getDate() - provide.getDate() == 1) {//说明是在昨天，则按照昨天 时:分显示
                return ContextUtils.getContext().getString(R.string.Yesterday);
            }
        }
        //按照月.日 时：分显示
        format.applyPattern("MM.dd");
        return format.format(provide);
    }

    public static String getWonderTime(long time) {

//        当天内容显示--今天 时：分
//        昨天内容显示--昨天 时：分
//        今年内容显示--月/日 时：分
//        往年内容显示--年/月/日 时：分
//        若昨天为上个月或去年时，仍显示昨天

        Date today = new Date();
        Date provide = new Date(time);
        SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getInstance();
        if (today.getTime() - provide.getTime() < DAY_TIME) {//今天或者昨天
            format.applyPattern(" HH:mm");
            if (today.getDay() == provide.getDay()) {//今天
                return ContextUtils.getContext().getString(R.string.DOOR_TODAY) + format.format(provide);
            } else {//昨天
                return ContextUtils.getContext().getString(R.string.Yesterday) + format.format(provide);
            }
        }

        if (today.getYear() > provide.getYear()) {//说明不是今年，则按照其他显示年.月.日显示
            format.applyPattern("yyyy/MM/dd HH:mm");
            return format.format(provide);
        }
        //按照月.日 时：分显示
        format.applyPattern("MM.dd HH:mm");
        return format.format(provide);
    }

    public static boolean isSameDay(long time1, long time2) {
        time1 = startOfDay(time1);
        time2 = startOfDay(time2);
        return Math.abs(time1 - time2) < 1000;
    }

    public static long wrapToLong(long time) {
        if (time == 0) {
            return 0;
        }
        long c = System.currentTimeMillis();
        if (String.valueOf(c).length() == String.valueOf(time).length()) {
            return time;
        } else {
            return time * 1000L;
        }
    }

    public static String get1224(long l) {
        boolean hourFormat = DateFormat.is24HourFormat(ContextUtils.getContext());
        if (hourFormat) {
            return getSimpleDateFormat_3.get().format(l);
        } else {
            return getSimpleDateFormat_3_12.get().format(l);
        }
    }

    private static Calendar tempCalendar1 = Calendar.getInstance();
    private static Calendar tempCalendar2 = Calendar.getInstance();

    public static String getVisitorTime(Long lastTime) {

        long todayStartTime = getTodayStartTime();
        long currentTimeMillis = System.currentTimeMillis();
        long distance = lastTime - todayStartTime;
        if (distance < 0 && Math.abs(distance) < DAY_TIME * 7) {
            String day = String.valueOf((int) Math.ceil(Math.abs(distance) / (double) DAY_TIME));
            return ContextUtils.getContext().getString(R.string.MESSAGES_TIME_DAY, day);
        }

        long second = (currentTimeMillis - lastTime) / 1000;
        long minute = second / 60;
        if (minute < 60) {
            return ContextUtils.getContext().getString(R.string.MESSAGES_TIME_MIN, String.valueOf(minute));
        }
        long hour = minute / 60;
        if (hour < 24) {
            return ContextUtils.getContext().getString(R.string.MESSAGES_TIME_HOUR, String.valueOf(hour));
        }

        return getYHM(lastTime);
    }

    public static int getDistanceDay(long startTime, long endTime) {
        //计算日期从开始时间于结束时间的0时计算
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTimeInMillis(startTime);
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTimeInMillis(endTime);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);
        return (int) ((toCalendar.getTimeInMillis() - fromCalendar.getTimeInMillis()) / (DAY_TIME)) + 1;
    }
}
