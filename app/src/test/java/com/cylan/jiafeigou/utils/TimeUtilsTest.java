package com.cylan.jiafeigou.utils;

import com.cylan.jiafeigou.misc.JFGRules;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by cylan-hunt on 17-2-18.
 */
public class TimeUtilsTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getTodayStartTime() throws Exception {

    }

    @Test
    public void getTodayEndTime() throws Exception {

    }

    @Test
    public void getSpecificDayEndTime() throws Exception {

    }

    @Test
    public void getSpecificDayStartTime() throws Exception {
        System.out.println("" + TimeUtils.getSpecificDayStartTime(1490410868253L));
        long result = 1490371200253L + 24 * 3600 * 1000L;
        System.out.println(result);
        //1490371200253
    }

    @Test
    public void getMediaPicTimeInString() throws Exception {

    }

    @Test
    public void getMediaVideoTimeInString() throws Exception {

    }

    @Test
    public void getTodayString() throws Exception {

    }

    @Test
    public void getDayString() throws Exception {

    }

    @Test
    public void testGetUptime() throws Exception {
        long time = System.currentTimeMillis() / 1000 - RandomUtils.getRandom(50) * 1000;

        time = 0;
        int temp = (int) time / 60;
        int minute = temp % 60;
        temp = temp / 60;
        int hour = temp % 24;
        temp = temp / 24;
        int day = temp;
        if (day > 0 && hour > 0) {
            System.out.println(String.format(Locale.CANADA, "%1$d天%2$d小时%3$d分", day, hour, minute));
        } else if (hour > 0) {
            System.out.println(String.format(Locale.CANADA, "%1$d小时%2$d分", hour, minute));
        } else {
            System.out.println(String.format(Locale.CANADA, "%1$d分", minute));
        }
    }

    @Test
    public void getDayInMonth() throws Exception {

    }

    @Test
    public void getMM_DD() throws Exception {

    }

    @Test
    public void getHH_MM() throws Exception {

    }

    @Test
    public void getHH_MM_Remain() throws Exception {

    }

    @Test
    public void getTestTime() throws Exception {

    }

    @Test
    public void getSpecifiedDate() throws Exception {

    }

    @Test
    public void startOfDay() throws Exception {
//        System.out.println(TimeUtils.getSpecificDayEndTime(1491007607000L));
//        System.out.println(TimeUtils.getSpecificDayEndTime(1490947860000L));
//        System.out.println(TimeUtils.getSpecificDayStartTime(1490975999000L));
    }

    @Test
    public void isToday() throws Exception {

    }

    @Test
    public void getSuperString() throws Exception {

    }

    @Test
    public void getHomeItemTime() throws Exception {

    }

    @Test
    public void getMonthInYear() throws Exception {

    }

    @Test
    public void getBellRecordTime() throws Exception {

        Date now = new Date();
        // EEE gives short day names, EEEE would be full length.
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE", Locale.SIMPLIFIED_CHINESE);
        String asWeek = dateFormat.format(now);
        System.out.println(asWeek);
        System.out.println("星期缩写:");
        for (Locale locale : JFGRules.CONST_LOCALE) {
            DateFormatSymbols symbols = new DateFormatSymbols(locale);
            String[] dayNames = symbols.getShortWeekdays();
            System.out.print("" + locale.getCountry() + ":");
            for (String s : dayNames) {
                System.out.print(s + " ");
            }
            System.out.println("");
        }
        System.out.print("月份缩写:");
        for (Locale locale : JFGRules.CONST_LOCALE) {
            DateFormatSymbols symbols = new DateFormatSymbols(locale);
            String[] dayNames = symbols.getShortMonths();
            System.out.print("" + locale.getCountry() + ":");
            for (String s : dayNames) {
                System.out.print(s + " ");
            }
            System.out.println("");
        }

    }

    @Test
    public void testTimezone() {
        System.out.println(TimeZone.getDefault().getDisplayName());
        System.out.println(TimeZone.getDefault().getID());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm",
                Locale.getDefault());
        System.out.println(-16200000 / 1000 / 60 / 60);//04:30
        System.out.println("GMT" + -36000000 / 60 / 60);

        String a[] = TimeZone.getAvailableIDs(-36000000);
        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i]);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-10:00"));
            System.out.println(dateFormat.format(new Date(System.currentTimeMillis())));
        }
        System.out.println(TimeZone.getDefault().getRawOffset());
        getTimeFormat(-16200000);
        System.out.println(getTimeFormat(36000000));
    }

    private String getTimeFormat(int rawOffset) {
        int hour = Math.abs(rawOffset / 1000 / 60 / 60);
        int minute = Math.abs(rawOffset) - Math.abs(hour) * 1000 * 60 * 60 > 0 ? 30 : 0;
        String factor = rawOffset > 0 ? "+" : "-";
        System.out.println(String.format(Locale.getDefault(), "GMT%s%02d:%02d", factor, hour, minute));
        return hour + ":" + minute;
    }

    @Test
    public void testDate() {
        long[] time = new long[]{
                1500825602000L,
                1500821908000L,
                1500711433000L
        };
        for (int i = 0; i < time.length; i++) {
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(time[i])));
        }
    }

}