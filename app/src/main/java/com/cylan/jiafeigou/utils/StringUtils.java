package com.cylan.jiafeigou.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;

import cylan.log.DswLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串操作工具包
 */
public class StringUtils {
    private final static Pattern emailer = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
    private final static Pattern emailer1 = Pattern.compile("[a-zA-Z0-9_\\-][a-zA-Z0-9_\\.\\-]{1,18}[a-zA-Z0-9_\\-]@[a-zA-Z0-9_\\-][a-zA-Z0-9\\._\\-]*[a-zA-Z0-9_\\-]");
    private final static SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final static SimpleDateFormat dateFormater2 = new SimpleDateFormat("yyyy-MM-dd");
    private final static SimpleDateFormat dateFormater3 = new SimpleDateFormat("HH:mm");
    private final static SimpleDateFormat dateFormater4 = new SimpleDateFormat("M-dd");
    // 获取当前时间：
    long currTime = System.currentTimeMillis();

    // Calendar实例：
    private static Calendar mCalendar = Calendar.getInstance();

    // 24 hours x 60 minutes x 60 seconds = 86400 seconds = 86400,000
    // milliseconds
    private static Long mHourInMillis = (long) 86400000;

    /**
     * 将字符串转位日期类型
     *
     * @param sdate
     * @return
     */
    public static Date toDate(String sdate) {
        try {
            return dateFormater.parse(sdate);
        } catch (ParseException e) {
            try {
                return dateFormater.parse(new Date(toLong(sdate)).toString());
            } catch (ParseException e1) {
                return null;
            }
        }
    }

    /**
     * 以友好的方式显示时间
     *
     * @param sdate
     * @return
     */
    public static String friendly_time(String just, String sdate) {
        Calendar post = Calendar.getInstance();
        post.setTimeInMillis(toLong(sdate));

        Date time = post.getTime();

        String ftime = "";
        Calendar cal = Calendar.getInstance();

        // 判断是否是同一天
        String curDate = dateFormater2.format(cal.getTime());
        String paramDate = dateFormater2.format(time);
        if (curDate.equals(paramDate)) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0) {
                if ((cal.getTimeInMillis() - time.getTime()) / 60000 < 5) {
                    ftime = just;
                } else {
                    ftime = dateFormater3.format(time.getTime());
                }
                // else {
                // ftime = Math.max((cal.getTimeInMillis() - time.getTime()) /
                // 60000, 1) + "分钟前";
                // }
            } else {
                // ftime = hour + "小时前";
                ftime = dateFormater3.format(time.getTime());
            }
            return ftime;
        }

        long lt = time.getTime() / 86400000;
        long ct = cal.getTimeInMillis() / 86400000;
        int days = (int) (ct - lt);
        if (days == 0) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0) {

                if ((cal.getTimeInMillis() - time.getTime()) / 60000 < 5) {
                    ftime = just;
                } else {
                    ftime = dateFormater3.format(time.getTime());
                }
                // else {
                // ftime = Math.max((cal.getTimeInMillis() - time.getTime()) /
                // 60000, 1) + "分钟前";
                // }
            } else {
                // ftime = hour + "小时前";
                ftime = dateFormater3.format(time.getTime());
            }
        }
        // else if (days == 1) {
        // ftime = "昨天" + dateFormater3.format(time);
        // } else if (days == 2) {
        // ftime = "前天" + dateFormater3.format(time);
        // } else if (days > 2 && days <= 10) {
        // ftime = days + "天前";
        // } else if (days > 10) {
        // ftime = dateFormater2.format(time);
        // }
        else {
            ftime = dateFormater4.format(time);
        }
        return ftime;
    }

    public static String friendly_num(String str) {
        return friendly_num(toLong(str));
    }

    public static String friendly_num(long l) {
        if (l < 100)
            return l + "";
        else if (l < 1000)
            return l / 100 + "百";
        else if (l < 10000)
            return l / 1000 + "千";
        else if (l < 1000000)
            return l / 10000 + "万";
        else
            return "百万之上";
    }

    /**
     * 判断给定字符串时间是否为今日
     *
     * @param sdate
     * @return boolean
     */
    public static boolean isToday(String sdate) {
        boolean b = false;
        Date time = toDate(sdate);
        Date today = new Date();
        if (time != null) {
            String nowDate = dateFormater2.format(today);
            String timeDate = dateFormater2.format(time);
            if (nowDate.equals(timeDate)) {
                b = true;
            }
        }
        return b;
    }

    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是不是一个合法的电子邮件地址
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        if (email == null || email.trim().length() == 0)
            return false;
        return emailer1.matcher(email).matches() && !StringUtils.isContainsChinese(email);
    }


    /**
     * 字符串转整数
     *
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str.replace("+", ""));
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
        return defValue;
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static int toInt(Object obj) {
        if (obj == null)
            return 0;
        return toInt(obj.toString(), 0);
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static long toLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 字符串转布尔值
     *
     * @param b
     * @return 转换异常返回 false
     */
    public static boolean toBool(String b) {
        try {
            return Boolean.parseBoolean(b);
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * HTML化字符串
     *
     * @param str
     * @return
     */
    public static Spanned fromHtml(String str) {
        if (!isEmpty(str)) {
            return Html.fromHtml(str);
        } else
            return Html.fromHtml("");
    }

    /**
     * 判断两个字符串是否相等
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean isEquals(String a, String b) {
        return a == b || a != null && a.equals(b);
    }

    public static boolean isEmptyOrNull(String value) {
        return value == null || "".equals(value);
    }

    /**
     * 删除input字符串中的html格式
     *
     * @param input
     * @param length
     * @return
     */
    public static String splitAndFilterString(String input, int length) {
        if (input == null || input.trim().equals("")) {
            return "";
        }
        // 去掉所有html元素,
        String str = input.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "");
        str = str.replaceAll("[(/>)<]", "");
        int len = str.length();
        if (len <= length) {
            return str;
        } else {
            str = str.substring(0, length);
            str += "......";
        }
        return str;
    }

    /**
     * 判断是否手机号
     *
     * @param inputStr
     * @return
     */
    public static boolean isPhoneNumber(String inputStr) {
        // TODO Auto-generated method stub
        if (inputStr == null) {
            return false;
        }
        return inputStr.startsWith("1") && inputStr.length() == 11;
    }

    /**
     * bitmap转换成file
     *
     * @param bmp
     * @param filename
     * @return
     */
    public static boolean saveBitmap2file(Bitmap bmp, String filename) {
        CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 100;
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            DswLog.ex(e.toString());
        }

        return bmp.compress(format, quality, stream);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        Bitmap bitmap = Bitmap.createBitmap(

                drawable.getIntrinsicWidth(),

                drawable.getIntrinsicHeight(),

                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888

                        : Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);

        // canvas.setBitmap(bitmap);

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        drawable.draw(canvas);

        return bitmap;

    }

    /**
     * 判断是否是字母
     *
     * @param s
     * @return
     */
    public static boolean isLetters(String s) {
        char c = s.charAt(0);
        int i = (int) c;
        return (i >= 65 && i <= 90) || (i >= 97 && i <= 122);
    }

    public static File saveBitmap(Bitmap bitmap) throws IOException {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/picture/" + "image");
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 70, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            DswLog.ex(e.toString());
        } catch (IOException e) {
            DswLog.ex(e.toString());
        }
        return file;
    }

    /**
     * 去掉多余的.和0 TODO String 上午10:39:36
     */
    public static String subZeroAndDot(String s) {
        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");// 去掉多余的0
            s = s.replaceAll("[.]$", "");// 如最后一位是.则去掉
        }
        return s;
    }

    public static int getScreenWidth(Activity c) {
        DisplayMetrics dm = new DisplayMetrics();
        c.getWindowManager().getDefaultDisplay().getMetrics(dm);

        int screenWidth = dm.widthPixels;

        int screenHeigh = dm.heightPixels;

        return screenWidth;
    }

    public static Boolean isYeaterday(String time, String today) {
        try {
            if (StringUtils.isEmptyOrNull(today) || StringUtils.isEmptyOrNull(time))
                return false;
            String[] str1 = time.split("月");
            String[] str2 = today.split("月");
            if (!str1[0].equals(str2[0]))
                return false;
            int day1 = Integer.parseInt(str1[1].substring(0, 2));
            int day2 = Integer.parseInt(str2[1].substring(0, 2));

            return day2 - day1 == 1;
        } catch (NumberFormatException e) {
            DswLog.ex(e.toString());
            return false;

        }
    }

    /**
     * 获取当前日期是星期几<br>
     *
     * @param dt
     * @return 当前日期是星期几
     */
    public static String getWeekOfDate(String[] str, Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return str[w];
    }

    public static String get(String[] str, Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.MONTH) - 1;
        if (w < 0)
            w = 0;
        return str[w];
    }


    public static boolean isLength6To12(String str) {
        return str.length() >= 6 && str.length() <= 12;
    }


    public static boolean isContainsChinese(String str) {
        String regEx = "[\u4e00-\u9fa5]";
        Pattern pat = Pattern.compile(regEx);
        Matcher matcher = pat.matcher(str);
        boolean flg = false;
        if (matcher.find()) {
            flg = true;
        }
        return flg;
    }
}