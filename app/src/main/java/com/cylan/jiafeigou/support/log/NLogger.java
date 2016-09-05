package com.cylan.jiafeigou.support.log;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by cylan-hunt on 16-8-17.
 */
public class NLogger implements IWriter {

    private File file;
    private Date date = new Date();
    private FileWriter fileWriter;
    private StringBuilder stringBuilder = new StringBuilder();
    private BufferedWriter bufWriter;
    private SimpleDateFormat simpleDateFormat;
    private NLoggerConfigurator configurator;

    public static final String EXP = "file_not_found";
    private static final String TAG = "NLogger";


    private void checkLoggerConfigurator() {
        if (this.configurator == null)
            throw new NullPointerException("NLoggerConfigurator is null");
    }

    NLogger(NLoggerConfigurator configurator) throws IOException {
        this.configurator = configurator;
        checkLoggerConfigurator();
        simpleDateFormat = new SimpleDateFormat(configurator.getMessagePattern(), Locale.getDefault());
        resetFileWriter();
    }

    private void resetFileWriter() throws IOException {
        file = new File(this.configurator.getFilePath());
        fileWriter = new FileWriter(file, true);//
        bufWriter = new BufferedWriter(fileWriter);
        Log.d(TAG, "resetFileWriter");
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


    private String parseMessage(final String message) {
        stringBuilder.setLength(0);
        date.setTime(System.currentTimeMillis());
        stringBuilder.append(simpleDateFormat.format(date));
        stringBuilder.append(message);
        return stringBuilder.toString();
    }

    @Override
    public void write(String message) throws IOException {
        createNewFile();
        bufWriter.write(parseMessage(message));
        bufWriter.newLine();
        bufWriter.flush();
    }

    private void createNewFile() throws IOException {
        if (file != null && !file.exists()) {
            resetFileWriter();
        }
        if (file != null && file.length() > configurator.getMaxFileSize()) {
            NLogSdCardInfo NLogSdCardInfo = new NLogSdCardInfo(Environment.getExternalStorageDirectory().getAbsolutePath());
            if (NLogSdCardInfo.getFreeSize() > configurator.getMaxFileSize() * 2) {
                final long time = System.currentTimeMillis();
                close();
                NLogUtils.copyFile(configurator.getFilePath(), configurator.getFilePath() + "_b");
                NLogUtils.deleteFile(configurator.getFilePath());
                Log.d(TAG, "handle new file: " + (System.currentTimeMillis() - time));
                resetFileWriter();
            } else {
                throw new IOException("there is not enough storage space");
            }
            Log.d(TAG, "file is too large");
        }
    }
}
