package com.cylan.jiafeigou.support.log;

import android.os.Environment;
import android.util.Log;

import com.cylan.jiafeigou.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cylan-hunt on 16-8-17.
 */
public class NLogger implements IWriter {

    private File file;
    private FileWriter fileWriter;
    private StringBuilder stringBuilder = new StringBuilder();
    private BufferedWriter bufWriter;
    private NLoggerConfigurator configurator;
    private ThreadSafeDateFormat threadSafeDateFormat;
    public static final String EXP = "file_not_found";
    private static final String TAG = "NLogger";
    private ExecutorService executorService;

    private void checkLoggerConfigurator() {
        if (this.configurator == null)
            throw new NullPointerException("NLoggerConfigurator is null");
    }

    NLogger(NLoggerConfigurator configurator) throws IOException {
        executorService = Executors.newSingleThreadExecutor();
        this.configurator = configurator;
        checkLoggerConfigurator();
        threadSafeDateFormat = new ThreadSafeDateFormat(configurator.getMessagePattern());
        resetFileWriter();
    }

    private boolean resetFileWriter() throws IOException {
        file = new File(this.configurator.getModuleDirPath(),
                this.configurator.getModuleName());
        close();
        File fileDir = new File(this.configurator.getModuleDirPath());
        boolean mkdirResult = fileDir.mkdirs();
        boolean createFile = file.createNewFile();
        fileWriter = new FileWriter(file, true);//
        bufWriter = new BufferedWriter(fileWriter);
        Log.d(TAG, "resetFileWriter: " + (mkdirResult && createFile));
        return mkdirResult && createFile;
    }

    public void close() throws IOException {
        if (bufWriter != null) {
//            bufWriter.flush();
            bufWriter.close();
        }
        if (fileWriter != null) {
//            fileWriter.flush();
            fileWriter.close();
        }
    }


    private String parseMessage(final String message) throws Exception {
        stringBuilder.setLength(0);
        stringBuilder.append(threadSafeDateFormat.convertString());
        stringBuilder.append(message);
        return stringBuilder.toString();
    }

    @Override
    public void write(final String message) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    bufWriter.write(parseMessage(message));
                    bufWriter.newLine();
                    bufWriter.flush();
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "NLogger log failed: " + e.getLocalizedMessage());
                    }
                } finally {
                    try {
                        createNewFile();
                    } catch (IOException e) {
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "NLogger create File failed: " + e.getLocalizedMessage());
                        }
                    }
                }
            }
        });
    }

    private void createNewFile() throws IOException {
        final long pTime = System.currentTimeMillis();
        synchronized (NLogger.class) {
            if (file != null && !file.exists()) {
                resetFileWriter();
            }
            if (file != null && file.length() > configurator.getMaxFileSize()) {
                NLogSdCardInfo NLogSdCardInfo = new NLogSdCardInfo(Environment.getExternalStorageDirectory().getAbsolutePath());
                if (NLogSdCardInfo.getFreeSize() > configurator.getMaxFileSize() * 2) {
                    final long time = System.currentTimeMillis();
                    close();
                    NLogUtils.copyFile(getModuleFilePath(),
                            getModuleFilePath() + "_b");
                    NLogUtils.deleteFile(getModuleFilePath());
                    Log.d(TAG, "handle new file: " + (System.currentTimeMillis() - time));
                    resetFileWriter();
                } else {
                    throw new IOException("there is not enough storage space");
                }
                Log.d(TAG, "file is too large: " + (System.currentTimeMillis() - pTime));
            }
        }
    }

    private String getModuleFilePath() {
        return configurator.getModuleDirPath() + File.separator + configurator.getModuleName();
    }

    private static class ThreadSafeDateFormat {

        private static String msgPattern;
        private Date date = new Date();

        private ThreadSafeDateFormat(String pattern) {
            msgPattern = pattern;
        }

        private static final ThreadLocal<SimpleDateFormat> df = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat(msgPattern, Locale.getDefault());
            }
        };

        public String convertString() {
            date.setTime(System.currentTimeMillis());
            return df.get().format(date);
        }
    }
}
