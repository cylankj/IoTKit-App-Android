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

    public ZipLog(String dir) {
        logFileName = dir + "/" + logFileName;
    }

    public String ZipLog(String dir,ArrayList<File> lists) {
        logFileName = dir + "/" + logFileName;
        packZip(lists);
        return getZipDir();
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

    public boolean zipExists() {
        File zip = new File(logFileName);
        return zip.exists();
    }

    public File getZipFile() {
        File file = new File(logFileName);
        return file;
    }

    public synchronized void packZip(ArrayList<File> lists) {
        if (zipExists())delZip();
        try {
            toZip(lists);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 压缩list文件/文件夹列表
     * @param lists 要压缩的list目录
     * @throws Exception
     * */
    private void toZip(List<File> lists) throws Exception {
        //创建zip包
        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(logFileName));
        //压缩
        for (File file : lists) { write2Zip(file, outZip);}
        //完成,关闭
        outZip.finish();
        outZip.close();
    }

    /**
     *压缩文件/文件夹压
     * @param file
     * @param zipOutputSteam
     * @throws Exception
     * */
    public void write2Zip(File file, ZipOutputStream zipOutputSteam)throws Exception {

        if ( zipOutputSteam== null)
            return;

        //判断是不是文件
        if (file.isFile()) {
            // 创建一个文件输入流
            FileInputStream inputStream = new FileInputStream(file);
            // 做一个ZipEntry
            ZipEntry entry = new ZipEntry(file.getName());
            // 存储项信息到压缩文件
            zipOutputSteam.putNextEntry(entry);
            // 复制字节到压缩文件
            int bytes_read;
            // 创建复制缓冲区
            byte[] buffer = new byte[4096];
            while ((bytes_read = inputStream.read(buffer)) != -1) {
                zipOutputSteam.write(buffer, 0, bytes_read);
            }
            zipOutputSteam.closeEntry();
            inputStream.close();
        }else {
            //文件夹的方式,获取文件夹下的子文件
            String fileList[] = file.list();
            //如果没有子文件, 则添加进去即可
            if (fileList.length <= 0) {
                ZipEntry zipEntry = new java.util.zip.ZipEntry(file.getName());
                zipOutputSteam.putNextEntry(zipEntry);
                zipOutputSteam.closeEntry();
            }
            //如果有子文件, 遍历子文件
            for (String subString:fileList){
                File subfile = new File(subString);
                write2Zip(subfile, zipOutputSteam);
            }
        }
    }
}
