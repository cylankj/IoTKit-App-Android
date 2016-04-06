package com.cylan.publicApi;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Printer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * @author download from web, thanks Dsw !
 * @version 1.0
 * @data 2012-2-20
 */
public class DswLog {
    /**
     * 是否输出到控制台
     */
    public static boolean debug = false;
    /**
     * 是否由本类输出到文件
     */
    public static boolean isWrite = true;
    /**
     * 写日志的接口，用于将日志交给JNI写入
     */
    public static Printer printer;
    private static int SDCARD_LOG_FILE_SAVE_DAYS = 2;
    private static String LOGFILENAME = "_DWSLog.txt";
    private static String LOG_PATH_SDCARD_DIR = Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/" + Constants.ROOT_DIR + "/WSLog/";
    private static String prefix = ""; //前缀，通常用于过滤使用。

    /**
     * 设置输出接口
     */
    public static void setPrinter(Printer p) {
        printer = p;
    }

    /**
     * 设置日志文件路径。
     *
     * @param path
     */
    public static void setPath(String path) {
        LOG_PATH_SDCARD_DIR = path;
    }

    /**
     * 前缀
     *
     * @param text
     */
    public static void setPrefix(String text) {
        prefix = text;
    }

    private static Handler mHandler;
    private static final String format = "%s.%s(L:%d)";

    private static SimpleDateFormat LogSdf = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()); //
    private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // one day one log!

    static long clock; // 用来记录一个时间，间隔去删除日志，不必每条消息都去遍历一遍，影响效率。

    static {
        HandlerThread mHandlerThread = new HandlerThread("WSLog-thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        clock = System.currentTimeMillis();
    }

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

    public static void delFileBefore() {
        File file = new File(LOG_PATH_SDCARD_DIR);
        if (!file.exists()) return;
        String needDelFiel = logfile.format(getDateBefore());
        File[] files = file.listFiles();
        if (files == null || files.length == 0) return;

        for (File f : files) {
            if (f.getName().compareTo(needDelFiel + LOGFILENAME) <= 0)
                f.delete();
        }
    }

    /**
     * 获取两天前的日期
     */
    private static Date getDateBefore() {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - SDCARD_LOG_FILE_SAVE_DAYS);
        return now.getTime();
    }


    /**
     * 设置是否写入sd卡。
     *
     * @param isWrite
     */
    public static void setWrite(boolean isWrite) {
        DswLog.isWrite = isWrite;
    }

    /**
     * 2015/03/17 tim
     */
    private static StackTraceElement getStackTraceElement() {
        return Thread.currentThread().getStackTrace()[4];
    }

    /**
     * 添加Log打印时的TAG，设定的TAG不起效果，统一使用当前Log所在的类名、方法名，行数组成
     * <p/>
     * 2015/03/17 tim
     */
    private static String generateTag(StackTraceElement caller) {
        String tag = "";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(format, new Object[]{callerClazzName, caller.getMethodName(), Integer.valueOf(caller.getLineNumber())});
        return getfianlTag(tag);
    }

    public static void v(String msg) {
        String tag = generateTag(getStackTraceElement());
        if (debug) {
            Log.v(tag, msg);
        }
        writeLogtoFile(tag, msg);
    }

    public static void d(String msg) {
        String tag = generateTag(getStackTraceElement());
        if (debug) {
            Log.d(tag, msg);
        }
        writeLogtoFile(tag, msg);
    }

    public static void i(String msg) {
        String tag = generateTag(getStackTraceElement());
        if (debug) {
            Log.i(tag, msg);
        }
        writeLogtoFile(tag, msg);
    }

    public static void w(String msg) {
        String tag = generateTag(getStackTraceElement());
        if (debug) {
            Log.w(tag, msg);
        }
        writeLogtoFile(tag, msg);
    }

    public static void e(String msg) {
        String tag = generateTag(getStackTraceElement());
        if (debug) {
            Log.e(tag, msg);
        }
        writeLogtoFile(tag, msg);
    }

    private static final String ExceptionPrefix = "Exception: ";

    /**
     * @param msg : 记录异常信息
     */
    public static void ex(String msg) {
        String tag = generateTag(getStackTraceElement());
        if (debug) {
            Log.e(tag, msg);
        }
        writeLogtoFile(tag, ExceptionPrefix + msg);
    }

    private static void writeLogtoFile(final String tag, final String msg) {
        if (isWrite) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    long nowtime = System.currentTimeMillis();
                    String needWriteFiel = logfile.format(nowtime);
                    String needWriteMessage = LogSdf.format(nowtime) + "  " + tag + " " + msg;
                    isPathValid(LOG_PATH_SDCARD_DIR);
                    File dir = new File(LOG_PATH_SDCARD_DIR);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, needWriteFiel + LOGFILENAME);
                    try {
                        FileWriter filerWriter = new FileWriter(file, true);//
                        BufferedWriter bufWriter = new BufferedWriter(filerWriter);
                        bufWriter.write(needWriteMessage);
                        bufWriter.newLine();
                        bufWriter.close();
                        filerWriter.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (nowtime - clock > 3600000 * 3) {
                        //运行3个小时后去检查是否需要删除日志。提升效率。
                        clock = nowtime;
                        delFileBefore();
                    }
                }
            });
        } else {
            //交给JNI写入
            if (null != printer) {
                printer.println(tag + "  " + msg);
            }
        }
    }

    /**
     * 取得最后的TAG
     *
     * @param tag
     * @return 前缀+TAG
     */
    private static String getfianlTag(String tag) {
        return prefix + " " + tag;
    }


}