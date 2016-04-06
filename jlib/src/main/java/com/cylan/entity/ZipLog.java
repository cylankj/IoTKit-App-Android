package com.cylan.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 主要是用于压缩打包日志
 * Created by Tim on 2015/6/17.
 */
public class ZipLog {
    private String logFileName = "log.zip"; // 本类输出的日志文件名称
    final int BUFFER = 4096;

    public ZipLog(String dir) {
        logFileName = dir + "/" + logFileName;
    }

    private void toZip(List<File> lists) {
        try {
            ZipOutputStream zo = new ZipOutputStream(new FileOutputStream(logFileName));
            for (File file : lists) {
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    for (File f : files) {
                        write2Zip(f, zo);
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

    public void write2Zip(File file, ZipOutputStream out) {
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

    public void addFile(String fName, List<File> lists) {
        File f = new File(fName);
        if (f.exists()) lists.add(f);
    }

    public String getZipDir() {
        return logFileName;
    }


    public void delZip() {
        File zip = getZipFile();
        if (zip.exists()) zip.delete();
    }

    private File getZipFile() {
        File file = new File(logFileName);
        return file;
    }

    public void packZip(ArrayList<File> lists) {
        File zip = new File(logFileName);
        if (zip.exists()) zip.delete();
        toZip(lists);
    }


}
