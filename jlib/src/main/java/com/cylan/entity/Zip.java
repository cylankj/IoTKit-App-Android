package com.cylan.entity;

import com.cylan.publicApi.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.cylan.support.DswLog;


public class Zip {
    private static String LOGFILENAME = android.os.Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/%1$s/" + "log.zip"; // 本类输出的日志文件名称
    private final static String WSLOG_DIR = android.os.Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/%1$s/WSLog/"; // 日志文件在sdcard中的路径
    private final static String CRASH_DIR = android.os.Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/%1$s/crash/";
    private final static String SMART_DIR = android.os.Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/smartCall_t.txt";
    private final static String SMART_DIR_T_1 = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/smartCall_t_1.txt";
    private final static String SMART_DIR_W = android.os.Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/smartCall_w.txt";
    private final static String SMART_DIR_W_1 = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/smartCall_w_1.txt";
    private final static String URL = "/index.php?mod=client&" + Constants.ACT + "=" + "log"
            + "&sessid=" + "%1$s" + "&time=" + System.currentTimeMillis() / 1000 + "&type=" + 1;
    public final static String BREAKPAD_DIR = android.os.Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/%1$s/breakpad/";
    private static String CRASHFILENAME = android.os.Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/%1$s/" + "crash.zip";

    private final static String JFG_APP_CRASH = "JFG-APP-Crash";
    private final static String SMARTCALL_CRASH = "SmartCall-Crash";


    static final int BUFFER = 4096;

    private static void toZip(String dir, List<File> lists, String fileName) {
        try {
            ZipOutputStream zo = new ZipOutputStream(new FileOutputStream(String.format(fileName, dir)));
            for (File file : lists) {
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            write2Zip(f, zo);
                        }
                    }
                } else {
                    write2Zip(file, zo);
                }
            }
            zo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void write2Zip(File file, ZipOutputStream out) {
        FileInputStream in = null;
        ZipEntry entry = null;
        // 创建复制缓冲区
        byte[] buffer = new byte[BUFFER];
        int bytes_read;
        if (file.isFile()) {
            try {
                // 创建一个文件输入流
                in = new FileInputStream(file);
                // 做一个ZipEntry
                entry = new ZipEntry(file.getName());
                // 存储项信息到压缩文件
                out.putNextEntry(entry);
                // 复制字节到压缩文件
                while ((bytes_read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytes_read);
                }
                out.closeEntry();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean addFile(String fName, List<File> lists) {
        File f = new File(fName);
        if (f.exists()) lists.add(f);
        return f.exists();
    }

    public static void packZip(String fileDirName) {
        File zip = new File(String.format(LOGFILENAME, fileDirName));
        if (zip.exists()) zip.delete();
        ArrayList<File> lists = new ArrayList<File>();
        addFile(String.format(WSLOG_DIR, fileDirName), lists);
        addFile(SMART_DIR, lists);
        addFile(String.format(CRASH_DIR, fileDirName), lists);
        addFile(SMART_DIR_T_1, lists);
        addFile(SMART_DIR_W, lists);
        addFile(SMART_DIR_W_1, lists);
        addFile(String.format(BREAKPAD_DIR, fileDirName), lists);
        toZip(fileDirName, lists, LOGFILENAME);
        DswLog.i("packZip fileDirName-->" + fileDirName);
    }

    public static String packCrashZip(String fileDirName) {
        File zip = new File(String.format(CRASHFILENAME, fileDirName));
        if (zip.exists()) zip.delete();
        ArrayList<File> lists = new ArrayList<File>();
        addFile(String.format(CRASH_DIR, fileDirName), lists);
        boolean exists = addFile(String.format(BREAKPAD_DIR, fileDirName), lists);
        toZip(fileDirName, lists, CRASHFILENAME);
        DswLog.i("packCrashZip fileDirName-->" + fileDirName);
        if (exists) {
            return SMARTCALL_CRASH;
        }
        return JFG_APP_CRASH;
    }

    public static String getZipDir(String fileName) {
        DswLog.i("ZipDir-->" + String.format(LOGFILENAME, fileName));
        return String.format(LOGFILENAME, fileName);
    }

    public static String getCrashZipDir(String fileName) {
        DswLog.i("ZipDir-->" + String.format(CRASHFILENAME, fileName));
        return String.format(CRASHFILENAME, fileName);
    }

    public static boolean deleteCrashZip(String fileDirName) {
        File zip = new File(String.format(CRASHFILENAME, fileDirName));
        return zip.exists() && zip.delete();
    }

    public static String getUrl(String sessid) {
        return String.format(URL, sessid);
    }
}
