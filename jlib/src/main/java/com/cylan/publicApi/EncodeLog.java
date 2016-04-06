package com.cylan.publicApi;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 带日志文件输入的，又可控开关的日志调试
 *
 * @author download from web, thanks Dsw !
 * @version 1.0
 * @data 2012-2-20
 */
public class EncodeLog {
    // private static char LOG_TYPE = 'v'; // 输入日志类型，w代表只输出告警信息等，v代表输出所有信息
    private static Boolean LOG_SWITCH = true; // 日志文件总开关
    public static Boolean LOG_WRITE_TO_FILE = true; // 日志写入文件开关

    private static int SDCARD_LOG_FILE_SAVE_DAYS = 2; // sd卡中日志文件的最多保存天数

    private static String LOGFILENAME = "EncodeLog.txt"; // 本类输出的日志文件名称
    private static String LOG_PATH_SDCARD_DIR = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/jiafeigou/EncodeLog/"; // 日志文件在sdcard中的路径

    private static boolean isPathValid(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private static Handler mHandler;
    private static SimpleDateFormat LogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 日志的输出格式
    private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd"); // 日志文件格式

    static {
        HandlerThread mHandlerThread = new HandlerThread("Tmp-thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public static void w(String tag, String text) {
        log(tag, text, 'w');
    }

    public static void e(String tag, String text) {
        log(tag, text, 'e');
    }

    public static void d(String tag, String text) {
        log(tag, text, 'd');
    }

    public static void i(String tag, String text) {
        log(tag, text, 'i');
    }

    public static void v(String tag, String text) {
        log(tag, text, 'v');
    }

    /**
     * with exception
     */
    public static void w(String tag, String msg, Throwable tr) {
        w(tag, msg + '\n' + getStackTraceString(tr));
    }

    public static void e(String tag, String msg, Throwable tr) {
        e(tag, msg + '\n' + getStackTraceString(tr));
    }

    public static void d(String tag, String msg, Throwable tr) {
        d(tag, msg + '\n' + getStackTraceString(tr));
    }

    public static void i(String tag, String msg, Throwable tr) {
        i(tag, msg + '\n' + getStackTraceString(tr));
    }

    public static void v(String tag, String msg, Throwable tr) {
        v(tag, msg + '\n' + getStackTraceString(tr));
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 根据tag, msg和等级，输出日志
     *
     * @param tag
     * @param msg
     * @param level
     * @return void
     * @since v 1.0
     */
    // public static Object WriteLock = new Object();
    private static void log(final String tag, final String msg, final char level) {
        final long threadId = Thread.currentThread().getId();
        if (LOG_SWITCH) {
            if ('i' == level) {
                Log.e(tag, msg + "Thread ID-->" + threadId);
            } else if ('e' == level) {
                Log.i(tag, msg + "Thread ID-->" + threadId);
            } else if ('w' == level) {
                Log.w(tag, msg + "Thread ID-->" + threadId);
            } else if ('d' == level) {
                Log.d(tag, msg + "Thread ID-->" + threadId);
            } else {
                Log.v(tag, msg + "Thread ID-->" + threadId);
            }
            if (LOG_WRITE_TO_FILE) {
                decreaseRunTimes++;
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (decreaseRunTimes > 1000) {
                            decreaseRunTimes = 0;
                            delFileBefore();
                        }
                        writeLogtoFile(String.valueOf(level), tag, msg + "Thread ID-->" + threadId);
                    }
                });
            }
        }
    }

    private static String needWriteFiel;
    private static String needWriteMessage;

    /**
     * 打开日志文件并写入日志
     *
     * @return *
     */
    private static void writeLogtoFile(String mylogtype, String tag, String text) {
        Date nowtime = new Date();
        needWriteFiel = logfile.format(nowtime);
        needWriteMessage = LogSdf.format(nowtime) + " " + mylogtype + " " + tag + " " + text;
        isPathValid(LOG_PATH_SDCARD_DIR);
        File file = new File(LOG_PATH_SDCARD_DIR, needWriteFiel + LOGFILENAME);

        try {
            FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(needWriteMessage);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除制定的日志文件
     */
    public static void delFile() {
        String needDelFiel = logfile.format(getDateBefore());
        File file = new File(LOG_PATH_SDCARD_DIR, needDelFiel + LOGFILENAME);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void delFileBefore() {
        File file = new File(LOG_PATH_SDCARD_DIR);
        if (!file.exists()) return;
        String needDelFiel = logfile.format(getDateBefore());
        File[] files = file.listFiles();
        if (files == null || files.length == 0) return;

        for (File f : files) {
            if (f.getName().compareTo(needDelFiel + LOGFILENAME) <= 0) f.delete();
        }
    }

    private static int decreaseRunTimes = 0;

    /**
     * 得到现在时间前的几天日期，用来得到需要删除的日志文件名
     */
    private static Date getDateBefore() {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - SDCARD_LOG_FILE_SAVE_DAYS);
        return now.getTime();
    }
}