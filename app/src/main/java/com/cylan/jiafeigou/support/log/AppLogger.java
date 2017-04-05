package com.cylan.jiafeigou.support.log;

/**
 * Created by cylan on 2015/1/19.
 */


import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.misc.JConstant;

import java.io.File;
import java.util.Locale;
import java.util.UnknownFormatConversionException;

/**
 * Wrapper API for sending log output.
 */
public class AppLogger {
    public static boolean permissionGranted = false;
    private static final String tagLeft = "(";
    private static final String tagRight = "):";
    private static final String tagL = "L:";
    private static final StringBuilder builder = new StringBuilder();
    protected static final String TAG = "CYLAN_TAG";
    public static boolean DEBUG = BuildConfig.DEBUG;

    private static final String DEFAULT_LOG = JConstant.LOG_PATH + File.separator + "log.txt";

    private static NLogger getLogger(String filePath) {
        try {
            return NLoggerManager.getLogger(TextUtils.isEmpty(filePath) ? DEFAULT_LOG : filePath);
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("AppLogger", "AppLogger: " + e.getLocalizedMessage());
        }
        return null;
    }

    private static void logFile(String filePath, String content) {
        NLogger logger = getLogger(filePath);
        if (logger != null)
            logger.write(content);
    }

    private AppLogger() {
    }

    public static void enableDebug(boolean debug) {
        DEBUG = debug;
    }

    /**
     * Send activity_cloud_live_mesg_call_out_item VERBOSE log message.
     *
     * @param msg The message you would like logged.
     */
    public static void v(String msg) {
        final String content = buildMessage(msg);
        if (DEBUG)
            android.util.Log.v(TAG, content);
        if (permissionGranted)
            logFile(null, content);
    }

//    /**
//     * Send activity_cloud_live_mesg_call_out_item VERBOSE log message.
//     *
//     * @param filePath :作为一个标志
//     * @param msg The message you would like logged.
//     */
//    public static void v(String filePath, String msg) {
//        final String content = buildMessage(msg);
//        if (DEBUG)
//            android.util.Log.v(TAG, content);
//        logFile(filePath, content);
//    }

//    /**
//     * Send activity_cloud_live_mesg_call_out_item VERBOSE log message and log the exception.
//     *
//     * @param msg The message you would like logged.
//     * @param thr An exception to log
//     */
//    public static void v(String msg, Throwable thr) {
//        final String content = buildMessage(msg);
//        if (DEBUG)
//            android.util.Log.v(TAG, buildMessage(msg), thr);
//        logFile(null, content);
//    }

    /**
     * Send activity_cloud_live_mesg_call_out_item DEBUG log message.
     *
     * @param msg
     */
    public static void d(String msg) {
        final String content = buildMessage(msg);
        if (DEBUG)
            android.util.Log.d(TAG, buildMessage(msg));
        if (permissionGranted)
            logFile(null, content);
    }

//    /**
//     * Send activity_cloud_live_mesg_call_out_item DEBUG log message.
//     *
//     * @param msg
//     */
//    public static void d(String filePath, String msg) {
//        final String content = buildMessage(msg);
//        if (DEBUG)
//            android.util.Log.d(TAG, buildMessage(msg));
//        logFile(filePath, content);
//    }

//    /**
//     * Send activity_cloud_live_mesg_call_out_item DEBUG log message and log the exception.
//     *
//     * @param msg The message you would like logged.
//     * @param thr An exception to log
//     */
//    public static void d(String msg, Throwable thr) {
//        final String content = buildMessage(msg, thr);
//        if (DEBUG)
//            android.util.Log.d(TAG, content, thr);
//        logFile(null, content);
//    }

    /**
     * Send an INFO log message.
     *
     * @param msg The message you would like logged.
     */
    public static void i(String msg) {
        final String content = buildMessage(msg);
        if (DEBUG)
            android.util.Log.i(TAG, buildMessage(msg));
        if (permissionGranted)
            logFile(null, content);
    }

//    /**
//     * Send an INFO log message.
//     *
//     * @param msg The message you would like logged.
//     */
//    public static void i(String filePath, String msg) {
//        final String content = buildMessage(msg);
//        if (DEBUG)
//            android.util.Log.i(TAG, content);
//        logFile(filePath, content);
//    }

//    /**
//     * Send activity_cloud_live_mesg_call_out_item INFO log message and log the exception.
//     *
//     * @param msg The message you would like logged.
//     * @param thr An exception to log
//     */
//    public static void i(String msg, Throwable thr) {
//        final String content = buildMessage(msg);
//        if (DEBUG)
//            android.util.Log.i(TAG, buildMessage(msg), thr);
//    }

    /**
     * Send an ERROR log message.
     *
     * @param msg The message you would like logged.
     */
    public static void e(String msg) {
        final String content = buildMessage(msg);
        if (DEBUG)
            android.util.Log.e(TAG, content);
        if (permissionGranted)
            logFile(null, content);
    }

//    /**
//     * Send an ERROR log message.
//     *
//     * @param msg The message you would like logged.
//     */
//    public static void e(String filePath, String msg) {
//        final String content = buildMessage(msg);
//        if (DEBUG)
//            android.util.Log.e(TAG, content);
//        logFile(filePath, msg);
//    }

    /**
     * Send activity_cloud_live_mesg_call_out_item WARN log message
     *
     * @param msg The message you would like logged.
     */
    public static void w(String msg) {
        final String content = buildMessage(msg);
        if (DEBUG)
            android.util.Log.w(TAG, buildMessage(msg));
    }

//    /**
//     * Send activity_cloud_live_mesg_call_out_item WARN log message
//     *
//     * @param msg The message you would like logged.
//     */
//    public static void w(String filePath, String msg) {
//        final String content = buildMessage(msg);
//        if (DEBUG)
//            android.util.Log.w(TAG, content);
//        logFile(filePath, content);
//    }

//    /**
//     * Send activity_cloud_live_mesg_call_out_item WARN log message and log the exception.
//     *
//     * @param msg The message you would like logged.
//     * @param thr An exception to log
//     */
//    public static void w(String msg, Throwable thr) {
//        final String content = buildMessage(msg);
//        if (DEBUG)
//            android.util.Log.w(TAG, buildMessage(msg), thr);
//    }

    /**
     * Send an empty WARN log message and log the exception.
     *
     * @param thr An exception to log
     */
    public static void w(Throwable thr) {
        if (DEBUG)
            android.util.Log.w(TAG, buildMessage(""), thr);
    }

    //    /**
//     * Send an ERROR log message and log the exception.
//     *
//     * @param msg The message you would like logged.
//     * @param thr An exception to log
//     */
//    public static void e(String msg, Throwable thr) {
//        final String content = buildMessage(msg, thr);
//        if (DEBUG)
//            android.util.Log.e(TAG, buildMessage(msg), thr);
//    }
//
//    public static void e(String message, Object... args) {
//        final String content = buildMessage(message, args);
//        if (DEBUG)
//            android.util.Log.e(TAG, buildMessage(message, args));
//    }
    private static final String content = "%s-%s(L:%s):%s";

    /**
     * Building Message
     *
     * @param msg The message you would like logged.
     * @return Message String
     */
    protected static String buildMessage(String msg, Object... args) {
        StackTraceElement caller = new Throwable().fillInStackTrace()
                .getStackTrace()[2];
        return String.format(Locale.getDefault(), content, Thread.currentThread().toString(),
                caller.getFileName(), caller.getLineNumber(), createMessage(msg, args));
    }

    private static String createMessage(String message, Object... args) throws UnknownFormatConversionException {
        return args.length == 0 ? message : String.format(message, args);
    }
}

